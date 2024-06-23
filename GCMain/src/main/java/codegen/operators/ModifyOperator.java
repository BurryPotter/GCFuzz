package codegen.operators;

import codegen.blocks.BasicBlock;
import codegen.blocks.ClassInfo;
import codegen.operators.modifyoperators.ModifyDataOperator;
import codegen.operators.modifyoperators.ModifyRefOperator;
import config.FuzzingConfig;
import soot.jimple.Stmt;

import java.util.ArrayList;
import java.util.List;

public class ModifyOperator extends Generic {
    public ArrayList<Operator> modifyOperators = new ArrayList<>();

    private ModifyOperator() {
        modifyOperators.add(ModifyDataOperator.getInstance());
        modifyOperators.add(ModifyRefOperator.getInstance());
    }

    private static final class AopHolder {
        static final ModifyOperator aop = new ModifyOperator();
    }

    public static ModifyOperator getInstance() {
        return ModifyOperator.AopHolder.aop;
    }

    public void setModifyOperators(ArrayList<Operator> operators) {
        modifyOperators = operators;
    }

    @Override
    public BasicBlock nextBlock(ClassInfo clazz, String methodSign, List<Stmt> targets) {
        Operator operator = modifyOperators.get(FuzzingConfig.nextChoice(modifyOperators.size(),FuzzingConfig.RandomType.OPERATOR_POOL));
//        Operator operator = modifyOperators.get(0);

        return operator.nextBlock(clazz, methodSign, targets);
    }
}
