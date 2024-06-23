package codegen.operators;

import codegen.blocks.ClassInfo;
import codegen.blocks.IfBlock;
import codegen.blocks.LoopBlock;
import codegen.blocks.StmtBlock;
import codegen.operands.OperandGenerator;
import codegen.providers.ElementsProvider;
import codegen.providers.NameProvider;
import codegen.providers.OperatorProvider;
import codegen.providers.PrimitiveValueProvider;
import config.FuzzingConfig;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JBreakpointStmt;
import soot.jimple.internal.JGotoStmt;
import soot.util.Numberable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Generic implements Operator {

    /**
     * 可访问变量，包括local, instance field, static field
     *
     * @param availableValues
     * @return
     */
    public List<Numberable> filterValidVars(List<Numberable> availableValues) {

        List<Numberable> vars = new ArrayList<>();
        for (Numberable value : availableValues) {
            if (value instanceof Local) {
                if (((Local) value).getType() instanceof PrimType) {
                    vars.add(value);
                }
            } else if (value instanceof SootField) {
                if (((SootField) value).getType() instanceof PrimType) {
                    vars.add(value);
                }
            }
        }
        return vars;
    }

    public boolean insertReturnStmt(ClassInfo clazz, String methodSign, IfBlock block, Stmt targetStmt) {

        if (FuzzingConfig.randomUpTo(FuzzingConfig.PROB_RETURN_VALUE,FuzzingConfig.RandomType.OPERATOR_POOL)) {

            SootMethod sootMethod = clazz.getMethodMaps().get(methodSign);
            Type retType = sootMethod.getReturnType();

            if (retType instanceof VoidType) {
                block.addStmt(Jimple.v().newReturnVoidStmt());
            } else {

                List<Numberable> vars = ElementsProvider.getAvailableValues(clazz, methodSign, targetStmt);
                List<Numberable> candidates = new ArrayList<>();
                for (Numberable var : vars) {
                    if (var instanceof Local) {
                        if (((Local) var).getType() == retType) {
                            candidates.add(var);
                        }
                    } else if (var instanceof SootField) {
                        if (((SootField) var).getType() == retType) {
                            candidates.add(var);
                        }
                    }
                }
                if (candidates.size() > 0 && FuzzingConfig.randomUpTo(FuzzingConfig.PROB_REUSE_VAR,FuzzingConfig.RandomType.OPERATOR_POOL)) {

                    Numberable ret = candidates.get(FuzzingConfig.nextChoice(candidates.size() - 1,FuzzingConfig.RandomType.OPERATOR_POOL));
                    Local retL = Jimple.v().newLocal(NameProvider.genVarName(), retType);
                    block.addLocalVar(retL);
                    if (ret instanceof Local) {
                        block.addStmt(Jimple.v().newAssignStmt(retL, (Local) ret));
                    }
                    if (ret instanceof SootField) {
                        if (((SootField) ret).isStatic()) {
                            StaticFieldRef staticFieldRef = Jimple.v().newStaticFieldRef(((SootField) ret).makeRef());
                            block.addStmt(Jimple.v().newAssignStmt(staticFieldRef, retL));
                        } else {
                            InstanceFieldRef instanceFieldRef = Jimple.v().newInstanceFieldRef(clazz.getThisRef(methodSign), ((SootField) ret).makeRef());
                            block.addStmt(Jimple.v().newAssignStmt(instanceFieldRef, retL));
                        }
                    }
                    block.addStmt(Jimple.v().newReturnStmt(retL));
                } else {

                    StmtBlock newBlock = OperandGenerator.getInstance().nextVariable(clazz, methodSign, retType, targetStmt);
                    block.addAllLocalVars(newBlock.getLocalVars());
                    block.addAllStmts(newBlock.getStmts());

                    if (newBlock.getLocalVars().size() > 0) {
                        Local newVar = newBlock.getLocalVars().get(newBlock.getLocalVars().size() - 1); // 数组生成时会有随机赋值
                        for (Local local : newBlock.getLocalVars()) {
                            if (local.getType().equals(retType)) {
                                newVar = local;
                                break;
                            }
                        }
                        block.addStmt(Jimple.v().newReturnStmt(newVar));
                    }
                }
            }
            return true;
        }
        return false;
    }

    public boolean insertGotoStmt(ClassInfo clazz, String methodSign, StmtBlock block) {

        if (block.getStmts().size() > 1 && FuzzingConfig.randomUpTo(FuzzingConfig.PROB_GOTO_VALUE,FuzzingConfig.RandomType.OPERATOR_POOL)) {

            List<Stmt> defStmts = identifyDefStmts(block);
            List<Stmt> stmts = block.getStmts();
            if (defStmts.size() == stmts.size()) return false;
            int startIndex = 0;
            for (Stmt defStmt : defStmts) {
                if (stmts.indexOf(defStmt) > startIndex) {
                    startIndex = stmts.indexOf(defStmt);
                }
            }
            int targetIndex = FuzzingConfig.nextChoice(stmts.size() - startIndex,FuzzingConfig.RandomType.OPERATOR_POOL) + startIndex;
            Stmt gotoTarget = stmts.get(targetIndex);
            JGotoStmt jGotoStmt = (JGotoStmt) Jimple.v().newGotoStmt(gotoTarget);
            block.getStmts().add(startIndex, jGotoStmt);
            return true;
        }
        return false;
    }

    public List<Stmt> identifyDefStmts(StmtBlock block) {

        List<Stmt> defStmts = new ArrayList<>();
        List<Stmt> assignStmts = block.getStmts().stream().filter(stmt -> stmt instanceof JAssignStmt).collect(Collectors.toList());
        assignStmts.forEach(stmt -> {
            if (stmt.getDefBoxes().stream().filter(valueBox -> !block.getLocalVars().contains(valueBox)).count() > 0) {
                defStmts.add(stmt);
            }
        });
        return defStmts;
    }

    public boolean insertBreakStmt(ClassInfo clazz, String methodSign, LoopBlock block) {

        if (FuzzingConfig.randomUpTo(FuzzingConfig.PROB_BREAK_VALUE, FuzzingConfig.RandomType.OPERATOR_POOL)) {

            List<Local> locals = block.getLocalVars();
            Local targetLocal = locals.get(FuzzingConfig.nextChoice(locals.size(), FuzzingConfig.RandomType.OPERATOR_POOL));
            Value condition;
            if (targetLocal.getType() instanceof PrimType) {
                //01 prim
                Expr relationOperator = FuzzingConfig.randomUpTo(FuzzingConfig.PROB_RELATION_GROUP, FuzzingConfig.RandomType.OPERATOR_POOL);
                condition = (Value) OperatorProvider.createOperatorFormulaStmt(relationOperator, targetLocal, PrimitiveValueProvider.next(targetLocal.getType()));
            } else if (targetLocal.getType() instanceof RefType) {
                //02 reference
                condition = Jimple.v().newEqExpr(targetLocal, NullConstant.v());
            } else {
                throw new RuntimeException("This should not happen: IfOperator - nextBlock: " + targetLocal.getType());
            }
            NopStmt ifTarget = Jimple.v().newNopStmt();
            IfStmt headStmt = Jimple.v().newIfStmt(condition, ifTarget);
            JBreakpointStmt jBreakpointStmt = (JBreakpointStmt) Jimple.v().newBreakpointStmt();
            block.getStmts().add(headStmt);
            block.getStmts().add(jBreakpointStmt);
            block.getStmts().add(ifTarget);
            return true;
        }
        return false;
    }
}
