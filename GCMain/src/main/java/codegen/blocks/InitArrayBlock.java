package codegen.blocks;

import codegen.blocks.BasicBlock;
import codegen.blocks.ClassInfo;
import codegen.blocks.LoopBlock;
import codegen.providers.NameProvider;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.JAssignStmt;

import java.util.List;

public class InitArrayBlock extends BasicBlock {
    private List<Value> dimSize;
    private ArrayType type;
    private Local arrVar;


    public InitArrayBlock(ArrayType type, List<Value> dimSize, Local arrVar) {
        if (type.numDimensions != dimSize.size()) {
            throw new RuntimeException("array dimension != dimSize.size()");
        }
        this.type = type;
        this.dimSize = dimSize;
        this.arrVar = arrVar;
    }

    public Local getArrVar() {
        return arrVar;
    }

    public void setArrVar(Local arrVar) {
        this.arrVar = arrVar;
    }

    public List<Value> getDimSize() {
        return dimSize;
    }

    public void setDimSize(List<Value> dimSize) {
        this.dimSize = dimSize;
    }

    public ArrayType getType() {
        return type;
    }

    public void setType(ArrayType type) {
        this.type = type;
    }

    @Override
    public Boolean insertBlock(ClassInfo clazz, SootMethod sootMethod) {
        return true;
//        LoopBlock block = createLoop(0, 1, 1, stmts);
//        List<LoopBlock> blockList = new ArrayList<>();
//        for (int i = 0; i < dimSize.size(); i++) {
//            int to = dimSize.get(i).hashCode();
//            block = createLoop(0, to, 1, block.getContents());
//            blockList.add(block);
//        }
//        Local lastDimArr = arrVar;
//        for (int i = 0; i < blockList.size() - 1; i++) {
//            Local loopIndex = blockList.get(i).getLocalVars().get(0);
//            JArrayRef arrayRef = (JArrayRef) Jimple.v().newArrayRef(lastDimArr, loopIndex);
//            Local localRef = Jimple.v().newLocal(NameProvider.genVarName(), arrayRef.getType());
//            lastDimArr = localRef;
//            this.addLocalVar(localRef);
//            JAssignStmt assign = (JAssignStmt) Jimple.v().newAssignStmt(localRef, arrayRef);
//            this.addStmt(assign);
//        }
//        Local loopIndex = blockList.get(blockList.size() - 1).getLocalVars().get(0);
//        Value assignValue = null;
//        Type baseType = type.baseType;
//        if (baseType instanceof RefType) {
//            LocalRefBlock refBlock = new LocalRefBlock((RefType) baseType);
//            assignValue = refBlock.createValue(localVars, stmts);
//        } else {
//            // 其他为基本类型
//            assignValue = PrimitiveValueProvider.next(baseType);
//        }
//        if (assignValue == null) {
//            return false;
//        }
//        AssignStmt assign = Jimple.v().newAssignStmt(Jimple.v().newArrayRef(lastDimArr, loopIndex), assignValue);
//        stmts.add(assign);
//        Body sootMethodBody = sootMethod.retrieveActiveBody();
//        sootMethodBody.getLocals().addAll(localVars);
//        sootMethodBody.getUnits().insertBefore(stmts, target);
//        for (LoopBlock b : blockList) {
//            b.insertBlock(clazz, sootMethod);
//        }
//        return true;
    }


    public LoopBlock createLoop(int from, int to, int step, List<Stmt> insertContents) {
        LoopBlock block = new LoopBlock();
        block.setGotoTarget(Jimple.v().newNopStmt());
        // init loop index
        Local loopIndex = Jimple.v().newLocal(NameProvider.genVarName(), IntType.v());
        JAssignStmt initStmt = (JAssignStmt) Jimple.v().newAssignStmt(loopIndex, IntConstant.v(from));
        block.addLocalVar(loopIndex);
        block.setInitStmt(initStmt);
        Expr cond;
        if (from <= to) {
            cond = Jimple.v().newGeExpr(loopIndex, IntConstant.v(to));
        } else {
            cond = Jimple.v().newLeExpr(loopIndex, IntConstant.v(to));
        }
        IfStmt headStmt = Jimple.v().newIfStmt(cond, block.getGotoTarget());
        block.setHeadStmt(headStmt);
        GotoStmt backJumpStmt = Jimple.v().newGotoStmt(headStmt);
        block.setBackJumpStmt(backJumpStmt);
        // set loop step
        AddExpr newValue = Jimple.v().newAddExpr(loopIndex, IntConstant.v(step));
        AssignStmt stepStmt = Jimple.v().newAssignStmt(loopIndex, newValue);
        block.setStepStmt(stepStmt);
        block.setContents(insertContents);
        return block;
    }
}
