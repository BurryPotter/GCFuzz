package codegen.operators.control;

import codegen.blocks.BasicBlock;
import codegen.blocks.ClassInfo;
import codegen.blocks.controll.CheckBlock;
import codegen.operators.Generic;
import codegen.providers.NameProvider;
import codegen.utils.SootHelper;
import soot.IntType;
import soot.Local;
import soot.Value;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;

import java.util.List;

public class CheckOperator extends Generic {
    private static final class AopHolder {
        static final CheckOperator aop = new CheckOperator();
    }

    public static CheckOperator getInstance() {
        return CheckOperator.AopHolder.aop;
    }

    @Override
    public BasicBlock nextBlock(ClassInfo clazz, String methodSign, List<Stmt> targets) {
        Stmt target = targets.get(0);
        Value cond = SootHelper.conditionGenerate(clazz, methodSign, target);
//        IfStmt headStmt = Jimple.v().newIfStmt(cond, Jimple.v().newNopStmt());
        CheckBlock checkBlock = new CheckBlock(cond);
        checkBlock.setInserationTarget(target);
        return checkBlock;
    }
}
