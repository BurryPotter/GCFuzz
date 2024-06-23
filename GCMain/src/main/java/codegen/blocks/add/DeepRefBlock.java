package codegen.blocks.add;

import codegen.blocks.BasicBlock;
import codegen.blocks.ClassInfo;
import codegen.providers.GCElementsProvider;
import codegen.providers.GCValueProvider;
import codegen.providers.NameProvider;
import config.FuzzingConfig;
import soot.*;
import soot.jimple.*;
import soot.toolkits.scalar.FlowSet;
import utils.ClassUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class DeepRefBlock extends BasicBlock {
    private int deep;

    public DeepRefBlock(int deep) {
        this.deep = deep;
    }

    @Override
    public Boolean insertBlock(ClassInfo clazz, SootMethod sootMethod) {
        SootClass sootClass = ClassUtils.loadClass("GCObj");
        FlowSet<Local> initAndNonNull = GCElementsProvider.getAvailableVars(clazz, sootMethod.getSignature(), target);

        Value instance = create(sootClass, deep, initAndNonNull);
        Body sootMethodBody = sootMethod.retrieveActiveBody();
        sootMethodBody.getLocals().addAll(localVars);
        sootMethodBody.getUnits().insertBefore(stmts, target);
        return instance != null;
    }

    private Value create(SootClass gcObj, int level, FlowSet<Local> locals) {
        if (level < 0) {
            return NullConstant.v();
        }
        RefType instanceType = gcObj.getType();
        Local newLocal = null;
        if (locals != null) {
            List<Local> gcobjs = locals.toList().stream().filter(local -> {
                        Type localType = local.getType();
                        if (localType instanceof RefType) {
                            return ((RefType) localType).getClassName().equals("GCObj");
                        }
                        return false;
                    }
            ).collect(Collectors.toList());
            if (!gcobjs.isEmpty()) {
                newLocal = gcobjs.get(FuzzingConfig.nextChoice(gcobjs.size(),FuzzingConfig.RandomType.OPERATOR_POOL));
            }
        }
        if (newLocal == null) {
            newLocal = Jimple.v().newLocal(NameProvider.genVarName() + "_gcobj_" + level, instanceType);
            localVars.add(newLocal);
        }
        List<SootMethod> initMethods = instanceType.getSootClass().getMethods()
                .stream()
                .filter(s -> s.isConstructor() && s.isPublic())
                .collect(Collectors.toList());
        if (initMethods.isEmpty()) {
            return null;
        }
        SootMethod init = initMethods.get(0); // GCObj has only one constructor
        List<Value> params = new ArrayList<>();
        List<Type> paramTypes = init.getParameterTypes();
        int count = FuzzingConfig.nextChoice(paramTypes.size() - 1,FuzzingConfig.RandomType.OPERATOR_POOL) + 1; // Non-null Ref count
        LinkedList<Integer> indexes = new LinkedList<>();
        for (int i = 0; i < paramTypes.size(); i++) {
            indexes.add(i);
        }
        ArrayList<Integer> arrayList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int index = FuzzingConfig.nextChoice(indexes.size(),FuzzingConfig.RandomType.OPERATOR_POOL);
            arrayList.add(indexes.get(index));
            indexes.remove(index);
        }
        for (int i = 0; i < paramTypes.size(); i++) {
            Type paramType = paramTypes.get(i);

            if (paramType instanceof RefType) {
                if (arrayList.contains(i)) {
                    params.add(create(gcObj, level - 1, null));
                } else {
                    params.add(NullConstant.v());
                }
            } else if (paramType instanceof IntType) {
                params.add(IntConstant.v(GCValueProvider.nextObjectSize()));
            } else {
                throw new RuntimeException("should not reach here. DeepRefBlock not support this type " + paramType);
            }
        }
        AssignStmt assign = Jimple.v().newAssignStmt(newLocal, Jimple.v().newNewExpr(instanceType));
        Stmt invoke = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(newLocal, init.makeRef(), params));
        stmts.add(assign);
        stmts.add(invoke);
        for (Value param : params) {
            if (param.getType() instanceof RefType) {
                AssignStmt assignNull = Jimple.v().newAssignStmt(param, NullConstant.v());
                stmts.add(assignNull);
            }
        }
        return newLocal;
    }

}
