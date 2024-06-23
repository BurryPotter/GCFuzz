package codegen.operators.addoperators;

import codegen.blocks.*;
import codegen.blocks.add.DuplicatedStringBlock;
import codegen.blocks.add.LocalArrayBlock;
import codegen.blocks.add.LocalRefBlock;
import codegen.blocks.add.MultiRefBlock;
import codegen.operators.Generic;
import codegen.providers.GCTypeProvider;
import codegen.providers.GCValueProvider;
import config.FuzzingConfig;
import soot.*;
import soot.jimple.IntConstant;
import soot.jimple.Stmt;

import java.util.ArrayList;
import java.util.List;

public class AddLocalOperator extends Generic {

    protected static AddLocalOperator aop;

    private AddLocalOperator() {
    }

    public static AddLocalOperator getInstance() {
        if (aop == null) {
            aop = new AddLocalOperator();
        }
        return aop;
    }

    @Override
    public BasicBlock nextBlock(ClassInfo clazz, String methodSign, List<Stmt> targets) {
        Stmt targetStmt = targets.get(FuzzingConfig.nextChoice(targets.size(),FuzzingConfig.RandomType.OPERATOR_POOL));
        if (FuzzingConfig.flipCoin(FuzzingConfig.RandomType.OPERATOR_POOL)) {
            ArrayType type = GCTypeProvider.arrayType();
            List<Value> l = new ArrayList<>();
            for (int i = 0; i < type.numDimensions; i++) {
                l.add(IntConstant.v(GCValueProvider.nextArraySize()));
            }
            LocalArrayBlock arrayBlock = new LocalArrayBlock(type, l);
            arrayBlock.setInserationTarget(targetStmt);
            return arrayBlock;
        } else {
            int count = GCValueProvider.nextMultiRefCount();
            List<RefType> refTypes = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                RefType type = GCTypeProvider.gcRefType();
                refTypes.add(type);
            }
            MultiRefBlock multiRefBlock = new MultiRefBlock(refTypes);
            multiRefBlock.setInserationTarget(targetStmt);
            return multiRefBlock;
        }
    }
}
