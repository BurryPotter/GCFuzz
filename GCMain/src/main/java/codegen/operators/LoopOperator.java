package codegen.operators;

import codegen.blocks.BasicBlock;
import codegen.blocks.ClassInfo;
import codegen.blocks.LoopBlock;
import codegen.providers.NameProvider;
import config.FuzzingConfig;
import soot.IntType;
import soot.Local;
import soot.jimple.*;
import soot.jimple.internal.JAssignStmt;

import java.util.ArrayList;
import java.util.List;

/**
 * loop映射在字节码层面两种表示方式：
 *      （1）for、while
 *      （2）do while
 */
public class LoopOperator extends Generic {

    protected static int maxLoopTime = FuzzingConfig.MAX_LOOP_SIZE;;
    protected static int maxLoopStep = FuzzingConfig.MAX_LOOP_STEP;
    protected static LoopOperator lop;

    public static LoopOperator getInstance() {
        if (lop == null) {
            lop = new LoopOperator();
        }
        return lop;
    }

    @Override
    public LoopBlock nextBlock(ClassInfo clazz, String methodSign, List<Stmt> targets) {

        LoopBlock block = new LoopBlock();
        block.setGotoTarget(Jimple.v().newNopStmt());

        int initValue,condValue,temp;
        initValue = FuzzingConfig.nextChoice(maxLoopTime,FuzzingConfig.RandomType.OPERATOR_POOL);
        condValue = FuzzingConfig.nextChoice(maxLoopTime,FuzzingConfig.RandomType.OPERATOR_POOL);
        if (initValue > condValue){
            temp = initValue;
            initValue = condValue;
            condValue = temp;
        }
        //01 create new local variable and init with maxLoopTime
        Local loopIndex = Jimple.v().newLocal(NameProvider.genVarName(), IntType.v());
        block.addLocalVar(loopIndex);
        JAssignStmt initStmt = (JAssignStmt) Jimple.v().newAssignStmt(loopIndex, IntConstant.v(initValue));
        block.setInitStmt(initStmt);

        //02 create operator
        GeExpr cond = Jimple.v().newGeExpr(loopIndex, IntConstant.v(condValue));
        //03 head stmt
        // 等待重定向到循环结束
        // 设置跳转的目标为seq后新插入的nop语句
        IfStmt headStmt = Jimple.v().newIfStmt(cond, block.getGotoTarget());
        block.setHeadStmt(headStmt);
        //04 create loop content
//        if (FuzzingConfig.flipCoin(FuzzingConfig.PROB_REUSE_INST)) {
        block.setContents(targets);
//        } else {
//            ArrayList<Stmt> target = new ArrayList<>();
//            target.add(targets.get(0));
//            block.setContents(target);
//            //create internal body
//            Operator defaultSeq = FuzzingConfig.randomUpTo(FuzzingConfig.PROB_INTERNAL_OPERATOR_GROUP);
//            BasicBlock defaultBlock = defaultSeq.nextBlock(clazz, methodSign, targets);
//            block.addAllLocalVars(defaultBlock.getLocalVars());
//            block.addAllStmts(defaultBlock.getStmts());
//        }
        //05 set loop step
        AddExpr newValue = Jimple.v().newAddExpr(loopIndex, IntConstant.v(FuzzingConfig.nextChoice(maxLoopStep,FuzzingConfig.RandomType.OPERATOR_POOL) + 1));
        AssignStmt stepStmt = Jimple.v().newAssignStmt(loopIndex,newValue);
        block.setStepStmt(stepStmt);
        //06 create back jump stmt
        GotoStmt backJumpStmt = Jimple.v().newGotoStmt(headStmt);
        block.setBackJumpStmt(backJumpStmt);
        block.setInserationTarget(targets.get(0));
        if (!insertBreakStmt(clazz, methodSign, block)){

        }
        return block;
    }
}