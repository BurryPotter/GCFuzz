package codegen.blocks.add;

import codegen.blocks.*;
import codegen.providers.ElementsProvider;
import codegen.utils.InitRefVarDataFlowAnalysis;
import codegen.utils.SootHelper;
import config.FuzzingConfig;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.JAssignStmt;
import soot.toolkits.graph.CompleteUnitGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AddFieldBlock extends BasicBlock {
    private SootField newField;

    private ValueCreatable valueCreator;

    public AddFieldBlock(SootField newField, ValueCreatable valueCreator) {
        Type type = newField.getType();
        if ((type instanceof RefType && valueCreator instanceof LocalRefBlock)
                || (type instanceof ArrayType && valueCreator instanceof LocalArrayBlock)) {
            this.newField = newField;
            this.valueCreator = valueCreator;
        } else {
            throw new RuntimeException("FieldBlock cannot support this type [ " + type + "] or field type doesn't match valueCreator");
        }
    }

    public SootField getNewField() {
        return newField;
    }

    public void setNewField(SootField newField) {
        this.newField = newField;
    }

    public ValueCreatable getValueCreator() {
        return valueCreator;
    }

    public void setValueCreator(ValueCreatable valueCreator) {
        this.valueCreator = valueCreator;
    }

    @Override
    public Boolean insertBlock(ClassInfo clazz, SootMethod sootMethod) {
        if (getNewField() == null) {
            return false;
        }
        SootClass sootClass = clazz.getSootClass();
        sootClass.addField(getNewField());
        if (!this.getNewField().isStatic()) {
            List<SootMethod> methods = sootClass.getMethods()
                    .stream()
                    .filter(SootMethod::isConstructor)
                    .collect(Collectors.toList());
            methods.add(sootMethod);
            for (SootMethod targetMethod : methods) {
                // <init> and target method
                Body body = targetMethod.retrieveActiveBody();
                List<Local> locals = new ArrayList<>(this.getLocalVars());
                List<Stmt> stmts = new ArrayList<>(this.getStmts());

                Value instance = SootHelper.findInstance(sootClass, targetMethod);
                if (instance == null) {
                    return false;
                }

                UnitPatchingChain units = body.getUnits();
                Stmt target = null;
                if (targetMethod.isStatic()) {

                    // insert statements after instance

                    InitRefVarDataFlowAnalysis analysis = new InitRefVarDataFlowAnalysis(new CompleteUnitGraph(body));
                    List<Stmt> targets = ElementsProvider.getTargets(clazz, targetMethod.getSignature(), 2)
                            .stream().filter(stmt -> analysis.getInLocal(stmt).contains((Local) instance))
                            .collect(Collectors.toList());
                    if (targets.isEmpty()){
                        return false;
                    }
                    target = targets.get(FuzzingConfig.nextChoice(targets.size(),FuzzingConfig.RandomType.OPERATOR_POOL));
                } else {
                    List<Stmt> targets = ElementsProvider.getTargets(clazz, targetMethod.getSignature(), 2);
                    target = targets.get(FuzzingConfig.nextChoice(targets.size(),FuzzingConfig.RandomType.OPERATOR_POOL));
                }
                Value newValue = valueCreator.createValue(locals, stmts,clazz,targetMethod.getSignature(),target);
                if (newValue == null) {
                    return false;
                }
                AssignStmt assign = Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(instance, this.getNewField().makeRef()), newValue);
                stmts.add(assign);
                body.getLocals().addAll(locals);
                units.insertBefore(stmts, target);
            }
        } else {
            List<SootMethod> methods = sootClass.getMethods()
                    .stream()
                    .filter(s -> !s.isStaticInitializer() && s.isStatic())
                    .collect(Collectors.toList());
//            if (initMethods.isEmpty()) {
//                SootMethod method = new SootMethod("<clinit>", new ArrayList<Type>(), VoidType.v(), Modifier.STATIC);
//                sootClass.addMethod(method);
//                method.setActiveBody(Jimple.v().newBody(method));
//                Body body = method.getActiveBody();
//                body.getUnits().add(Jimple.v().newReturnVoidStmt());
//                initMethods.add(method);
//            }
            SootMethod targetMethod = methods.get(FuzzingConfig.nextChoice(methods.size(),FuzzingConfig.RandomType.OPERATOR_POOL));
            List<Local> locals = new ArrayList<>(this.getLocalVars());
            List<Stmt> stmts = new ArrayList<>(this.getStmts());
            StaticFieldRef fieldRef = Jimple.v().newStaticFieldRef(this.getNewField().makeRef());
            Body body = targetMethod.retrieveActiveBody();
            UnitPatchingChain units = body.getUnits();
            List<Stmt> targets = ElementsProvider.getTargets(clazz, targetMethod.getSignature(), 2);
            Stmt target = targets.get(FuzzingConfig.nextChoice(targets.size(),FuzzingConfig.RandomType.OPERATOR_POOL));

            Value newValue = valueCreator.createValue(locals, stmts,clazz,targetMethod.getSignature(),target);
            if (newValue == null) {
                return false;
            }
            AssignStmt assign = Jimple.v().newAssignStmt(fieldRef, newValue);
            stmts.add(assign);
            body.getLocals().addAll(locals);
            units.insertBefore(stmts, target);
        }

        return true;
    }

}
