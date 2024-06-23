package codegen.operators;

import codegen.blocks.BasicBlock;
import codegen.blocks.ClassInfo;
import codegen.blocks.gc.GCType;
import codegen.blocks.gc.SystemGCBlock;
import codegen.blocks.gc.WhiteBoxGCBlock;
import config.FuzzingConfig;
import dtjvms.DTConfiguration;
import soot.jimple.Stmt;

import java.util.List;

public class GCOperator extends Generic {

    private GCOperator() {
    }

    private static final class AopHolder {
        static final GCOperator aop = new GCOperator();
    }

    public static GCOperator getInstance() {
        return GCOperator.AopHolder.aop;
    }


    @Override
    public BasicBlock nextBlock(ClassInfo clazz, String methodSign, List<Stmt> targets) {
        Stmt insertTarget = targets.get(FuzzingConfig.nextChoice(targets.size(),FuzzingConfig.RandomType.OPERATOR_POOL));
        SystemGCBlock block = new SystemGCBlock();
        block.setInserationTarget(insertTarget);
        return block;
//        if (DTConfiguration.TARGET_JVMS.contains("openj9")) {
//            // openj9 不支持 whitebox.jar
//
//        } else {
//            GCType gcType = GCType.values()[FuzzingConfig.nextChoice(GCType.values().length)];
//            WhiteBoxGCBlock block = new WhiteBoxGCBlock(gcType);
//            block.setInserationTarget(insertTarget);
//            return block;
//        }
    }
}
