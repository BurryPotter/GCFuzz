package codegen.utils;

import codegen.blocks.BasicBlock;
import codegen.blocks.ClassInfo;
import codegen.blocks.add.LocalRefBlock;
import codegen.providers.GCElementsProvider;
import codegen.providers.NameProvider;
import config.FuzzingConfig;
import soot.*;
import soot.JastAddJ.ForStmt;
import soot.JastAddJ.WhileStmt;
import soot.jimple.*;
import soot.jimple.internal.JArrayRef;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JNewExpr;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.FlowSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SootHelper {
    public static Value findInstance(SootClass sootClass, SootMethod method) {
        return findInstance(sootClass, method, true);
    }

    public static Value findInstance(SootClass sootClass, SootMethod method, boolean addInstance) {
        String className = sootClass.getName();
        UnitPatchingChain unitChain = method.retrieveActiveBody().getUnits();
        if (!method.isStatic()) {
            for (Unit unit : new ArrayList<>(unitChain)) {
                if (unit instanceof JIdentityStmt) {
                    Type type = ((JIdentityStmt) unit).getRightOp().getType();
                    if (type instanceof RefType) {
                        String name = ((RefType) type).getClassName();
                        if (name.equals(className)) {
                            // find the instance of SootClass
                            return ((JIdentityStmt) unit).getLeftOp();
                        }
                    }
                }
            }
        } else {
            if (FuzzingConfig.flipCoin()) {
                List<Unit> list = new ArrayList<>(unitChain).stream().filter(unit -> {
                    if (unit instanceof JAssignStmt && ((JAssignStmt) unit).getRightOp() instanceof JNewExpr) {
                        Type type = ((JAssignStmt) unit).getRightOp().getType();
                        if (type instanceof RefType) {
                            String name = ((RefType) type).getClassName();
                            return name.equals(className);
                        }
                    }
                    return false;

                }).collect(Collectors.toList());
                if (!list.isEmpty()) {
                    Unit unit = list.get(FuzzingConfig.nextChoice(list.size()));
                    return ((JAssignStmt) unit).getLeftOp();
                }
            }

        }

        if (!addInstance) {
            return null;
        }
        if (!method.isStatic()) {
            Local instance = Jimple.v().newLocal(NameProvider.genVarName() + "_instance", sootClass.getType());
            Stmt identity = Jimple.v().newIdentityStmt(instance, Jimple.v().newThisRef(sootClass.getType()));
            Body sootMethodBody = method.retrieveActiveBody();
            sootMethodBody.getLocals().add(instance);
            sootMethodBody.getUnits().insertBefore(identity, sootMethodBody.getUnits().getFirst());
            return instance;
        } else {
            Local instance = Jimple.v().newLocal(NameProvider.genVarName() + "_instance", sootClass.getType());
            LocalRefBlock localRefBlock = new LocalRefBlock(sootClass.getType());
            List<Local> locals = new ArrayList<>();
            List<Stmt> stmts = new ArrayList<>();
            Value value = localRefBlock.createValue(locals, stmts);
            if (value == null) {
                return null;
            }
            Stmt assign = Jimple.v().newAssignStmt(instance, value);
            Body sootMethodBody = method.retrieveActiveBody();
            locals.add(instance);
            stmts.add(assign);
            sootMethodBody.getLocals().addAll(locals);
            List<Unit> list = sootMethodBody.getUnits()
                    .stream()
                    .filter(unit -> !(unit instanceof IdentityStmt))
                    .collect(Collectors.toList());
            if (list.isEmpty()) {
                list = sootMethodBody.getUnits()
                        .stream()
                        .filter(unit -> unit instanceof IdentityStmt)
                        .collect(Collectors.toList());
                sootMethodBody.getUnits().insertAfter(stmts,
                        list.get(list.size() - 1));
                return instance;
            }
            sootMethodBody.getUnits().insertBefore(stmts,
                    list.get(0));
            return instance;
        }
    }

    public static void sortMethod(List<SootMethod> sootMethodList) {
//        Collections.shuffle(sootMethodList);
        sootMethodList.sort(Comparator.comparingInt(SootMethod::getParameterCount));
    }

    public static Local splitMultiArray(BasicBlock block, Local local, List<Value> arraySize) {

        Local lastArrayRef = null;
        for (int i = 0; i < arraySize.size() - 1; i++) {

            int index = FuzzingConfig.nextChoice(arraySize.get(i).hashCode());
            JArrayRef arrayRef = (JArrayRef) Jimple.v().newArrayRef(local, IntConstant.v(index));
            Local localRef = Jimple.v().newLocal(NameProvider.genVarName(), arrayRef.getType());
            lastArrayRef = localRef;
            block.addLocalVar(localRef);
            JAssignStmt assign = (JAssignStmt) Jimple.v().newAssignStmt(localRef, arrayRef);
            block.addStmt(assign);
            //CHECK 这里是不是想剥离出一个一维数组。
            local = localRef;
        }
        return lastArrayRef;
    }

    public static boolean isExtends(SootClass child, SootClass parent) {
        ArrayList<SootClass> superClass = new ArrayList<>();
        if (child.hasSuperclass()) {
            superClass.add(child.getSuperclass());
        }
        if (child.getInterfaceCount() > 0) {
            superClass.addAll(child.getInterfaces());
        }
        for (SootClass aClass : superClass) {
            if (aClass.getName().equals(parent.getName())) {
                return true;
            } else {
                return isExtends(aClass, parent);
            }
        }
        return false;
    }

    public static Value conditionGenerate(ClassInfo clazz, String methodSign, Stmt insertTarget) {
        FlowSet<Local> initAndNonNull = GCElementsProvider.getAvailableVars(clazz, methodSign, insertTarget);
        FlowSet<Local> nullVars = GCElementsProvider.getNullVars(clazz, methodSign, insertTarget);
        initAndNonNull.difference(nullVars);
        Value cond = null;
        if (initAndNonNull.isEmpty()) {
            if (!nullVars.isEmpty()) {
                Local local = nullVars.toList().get(FuzzingConfig.nextChoice(nullVars.size()));
                cond = Jimple.v().newNeExpr(local, NullConstant.v());
            }
        } else {
            Local local = initAndNonNull.toList().get(FuzzingConfig.nextChoice(initAndNonNull.size()));
            cond = Jimple.v().newEqExpr(local, NullConstant.v());
        }
        return cond;
    }
}
