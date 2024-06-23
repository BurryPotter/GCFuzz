package codegen.operators;

import codegen.blocks.BasicBlock;
import codegen.blocks.ClassInfo;
import codegen.operators.addoperators.AddFieldOperator;
import codegen.operators.addoperators.AddLocalOperator;
import codegen.operators.addoperators.DeepRefOperator;
import config.FuzzingConfig;
import soot.jimple.Stmt;

import java.util.ArrayList;
import java.util.List;

public class AddOperator extends Generic {

    public ArrayList<Operator> addOperators = new ArrayList<>();

    private AddOperator() {
        addOperators.add(AddFieldOperator.getInstance());
        addOperators.add(AddLocalOperator.getInstance());
        addOperators.add(DeepRefOperator.getInstance());
    }

    private static final class AopHolder {
        static final AddOperator aop = new AddOperator();
    }

    public static AddOperator getInstance() {
        return AopHolder.aop;
    }

    public void setAddOperators(ArrayList<Operator> operators) {
        addOperators = operators;
    }

    @Override
    public BasicBlock nextBlock(ClassInfo clazz, String methodSign, List<Stmt> targets) {
        Operator operator = addOperators.get(FuzzingConfig.nextChoice(addOperators.size(),FuzzingConfig.RandomType.OPERATOR_POOL));
        return operator.nextBlock(clazz, methodSign, targets);
    }
}
