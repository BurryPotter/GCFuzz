package codegen.operators;

import codegen.blocks.BasicBlock;
import codegen.blocks.ClassInfo;
import config.FuzzingConfig;
import soot.jimple.Stmt;

import java.util.ArrayList;
import java.util.List;

public class AllOperator extends Generic {
    public ArrayList<Operator> operators = new ArrayList<>();

    private AllOperator() {
        operators.add(AddOperator.getInstance());
        operators.add(ModifyOperator.getInstance());
        operators.add(DeleteOperator.getInstance());
//        operators.add(GCOperator.getInstance());
        operators.add(ControlOperator.getInstance());
    }

    private static final class AopHolder {
        static final AllOperator aop = new AllOperator();
    }

    public static AllOperator getInstance() {
        return AllOperator.AopHolder.aop;
    }


    @Override
    public BasicBlock nextBlock(ClassInfo clazz, String methodSign, List<Stmt> targets) {
        Operator operator = operators.get(FuzzingConfig.nextChoice(operators.size(),FuzzingConfig.RandomType.OPERATOR_POOL));
        return operator.nextBlock(clazz, methodSign, targets);
    }
}
