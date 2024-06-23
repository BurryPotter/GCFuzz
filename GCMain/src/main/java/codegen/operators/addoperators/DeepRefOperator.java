package codegen.operators.addoperators;

import codegen.blocks.BasicBlock;
import codegen.blocks.ClassInfo;
import codegen.blocks.add.DeepRefBlock;
import codegen.operators.Generic;
import codegen.providers.GCValueProvider;
import codegen.providers.PrimitiveValueProvider;
import config.FuzzingConfig;
import soot.jimple.Stmt;

import java.util.List;

public class DeepRefOperator extends Generic {
    protected static DeepRefOperator aop;

    private DeepRefOperator() {
    }

    public static DeepRefOperator getInstance() {
        if (aop == null) {
            aop = new DeepRefOperator();
        }
        return aop;
    }

    @Override
    public BasicBlock nextBlock(ClassInfo clazz, String methodSign, List<Stmt> targets) {
        Stmt targetStmt = targets.get(FuzzingConfig.nextChoice(targets.size(),FuzzingConfig.RandomType.OPERATOR_POOL));
        DeepRefBlock deepRefBlock = new DeepRefBlock(GCValueProvider.nextObjectDeep());
        deepRefBlock.setInserationTarget(targetStmt);
        return deepRefBlock;
    }
}
