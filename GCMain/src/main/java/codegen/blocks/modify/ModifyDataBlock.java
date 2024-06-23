package codegen.blocks.modify;

import codegen.blocks.ClassInfo;
import codegen.blocks.add.LocalRefBlock;
import codegen.providers.GCValueProvider;
import codegen.providers.NameProvider;
import codegen.providers.PrimitiveValueProvider;
import config.FuzzingConfig;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.JArrayRef;
import soot.jimple.internal.JAssignStmt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ModifyDataBlock implements ModifyBlock {

    @Override
    public boolean modify(ClassInfo clazz, SootMethod sootMethod, RefType type, Local target, List<Local> localVars, List<Stmt> stmts,Unit insertTarget) {
        return invokeFunc(type, target, localVars, stmts,clazz,sootMethod.getSignature(),insertTarget);
    }

    @Override
    public boolean modify(ClassInfo clazz, SootMethod sootMethod, ArrayType type, List<Value> dimSize, Local target, List<Local> localVars, List<Stmt> stmts,Unit insertTarget) {
        return modifyArrayData(sootMethod, type, target, localVars, stmts,clazz,insertTarget);
    }

    @Override
    public boolean modify(ClassInfo clazz, SootMethod sootMethod, RefType type, FieldRef target, List<Local> localVars, List<Stmt> stmts,Unit insertTarget) {
        Local local = Jimple.v().newLocal(NameProvider.genVarName(), type);
        localVars.add(local);
        Stmt assign = Jimple.v().newAssignStmt(local, target);
        stmts.add(assign);
        return invokeFunc(type, local, localVars, stmts,clazz,sootMethod.getSignature(),insertTarget);
    }

    @Override
    public boolean modify(ClassInfo clazz, SootMethod sootMethod, ArrayType type, List<Value> dimSize, FieldRef target, List<Local> localVars, List<Stmt> stmts,Unit insertTarget) {
        Local local = Jimple.v().newLocal(NameProvider.genVarName(), type);
        localVars.add(local);
        Stmt assign = Jimple.v().newAssignStmt(local, target);
        stmts.add(assign);
        return modifyArrayData(sootMethod, type, local, localVars, stmts,clazz,insertTarget);
    }

    private boolean modifyArrayData(SootMethod sootMethod, Type varArrType, Local variable, List<Local> localVars, List<Stmt> stmts,ClassInfo classInfo,Unit insertTarget) {
        // create Random instance.
        // Random xx_random = new Random(xxL);
        RefType randomRefType = RefType.v("java.util.Random");
        Local random = Jimple.v().newLocal(NameProvider.genVarName() + "_random", randomRefType);
        SootMethod initMethod = randomRefType.getSootClass().getMethod("<init>", Collections.singletonList(LongType.v()));
        AssignStmt assign = Jimple.v().newAssignStmt(random, Jimple.v().newNewExpr(randomRefType));
        List<Value> params = new ArrayList<>();
        params.add(PrimitiveValueProvider.next(LongType.v()));
        Stmt initStmt = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(random, initMethod.makeRef(), params));
        stmts.add(assign);
        stmts.add(initStmt);
        // prepare to invoke random.nextInt
        SootMethod nextIntMethod = randomRefType.getSootClass().getMethod("nextInt", Collections.singletonList(IntType.v()), IntType.v());
        Local length = Jimple.v().newLocal(NameProvider.genVarName() + "_length", IntType.v());
        Local index = Jimple.v().newLocal(NameProvider.genVarName() + "_index", IntType.v());
        int dimensions = ((ArrayType) variable.getType()).numDimensions;
        List<Local> arrLocals = new ArrayList<>();
        arrLocals.add(variable);
        while (dimensions > 1) {
            dimensions--;
            Local local = Jimple.v().newLocal(NameProvider.genVarName() + "_subArr", ArrayType.v(((ArrayType) variable.getType()).baseType, dimensions));
            arrLocals.add(local);
        }
        localVars.add(random);
        localVars.add(length);
        localVars.add(index);
        localVars.addAll(arrLocals.subList(1, arrLocals.size()));
        // resolve array
        Stmt gotoTarget = Jimple.v().newNopStmt();
        for (int i = 0; i < arrLocals.size(); i++) {


            Stmt assignLen = Jimple.v().newAssignStmt(length, Jimple.v().newLengthExpr(arrLocals.get(i)));
            Expr cond = Jimple.v().newLeExpr(length, IntConstant.v(0));
            IfStmt headStmt = Jimple.v().newIfStmt(cond, gotoTarget);
            Stmt nextIntStmt = Jimple.v().newAssignStmt(index, Jimple.v().newVirtualInvokeExpr(random, nextIntMethod.makeRef(), length));
            stmts.add(assignLen);
            stmts.add(headStmt);
            stmts.add(nextIntStmt);
            JArrayRef arrayRef = (JArrayRef) Jimple.v().newArrayRef(arrLocals.get(i), index);
            if (i == arrLocals.size() - 1) {
                Value value = null;
                Type baseType = ((ArrayType) varArrType).baseType;
                if (baseType instanceof RefType) {
                    LocalRefBlock localRefBlock = new LocalRefBlock((RefType) baseType);
                    value = localRefBlock.createValue(localVars, stmts,classInfo,sootMethod.getSignature(),insertTarget);
                } else if (PrimitiveValueProvider.isPrimitiveOrString(baseType)) {
                    value = PrimitiveValueProvider.next(baseType);
                }
                if (value == null) {
                    return false;
                }
                Stmt assignArr = Jimple.v().newAssignStmt(arrayRef, value);
                stmts.add(assignArr);
            } else {
                JAssignStmt assignArr = (JAssignStmt) Jimple.v().newAssignStmt(arrLocals.get(i + 1), arrayRef);
                stmts.add(assignArr);
            }
        }
        stmts.add(gotoTarget);
        return true;
    }

    private boolean invokeFunc(RefType type, Local var, List<Local> localVars, List<Stmt> stmts,ClassInfo clazz, String methodSign, Unit targetUnit) {
        List<SootMethod> methodList = type.getSootClass().getMethods()
                .stream()
                .filter(method -> method.isPublic() && !method.isConstructor() && !method.isStaticInitializer())
                .collect(Collectors.toList());
        if (methodList.size() == 0) {
            return false;
        }
        SootMethod method = methodList.get(FuzzingConfig.nextChoice(methodList.size(),FuzzingConfig.RandomType.OPERATOR_POOL));
        List<Value> params = GCValueProvider.paramValue(method.getParameterTypes(), localVars, stmts,clazz,methodSign,targetUnit);
        if (params == null) {
            return false;
        }
        Stmt invoke;
        if (method.isStatic()) {
            invoke = Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(method.makeRef(), params));
        } else if (method.makeRef().getDeclaringClass().isInterface()) {
            invoke = Jimple.v().newInvokeStmt(Jimple.v().newInterfaceInvokeExpr(var, method.makeRef(), params));
        } else {
            invoke = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(var, method.makeRef(), params));
        }
        stmts.add(invoke);
        return true;
    }
}
