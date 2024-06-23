package codegen.blocks.add;

import codegen.blocks.BasicBlock;
import codegen.blocks.ClassInfo;
import codegen.blocks.InitArrayBlock;
import codegen.providers.NameProvider;
import soot.*;
import soot.jimple.AssignStmt;
import soot.jimple.Jimple;
import soot.jimple.Stmt;

import java.util.List;

public class LocalArrayBlock extends BasicBlock implements ValueCreatable {

    private List<Value> dimSize;
    private ArrayType type;

    public LocalArrayBlock(ArrayType type, List<Value> dimSize) {
        if (type.numDimensions != dimSize.size()) {
            throw new RuntimeException("array dimension != dimSize.size()");
        }
        this.type = type;
        this.dimSize = dimSize;
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
        Value array = createValue(this.getLocalVars(), this.getStmts());
        if (array == null) {
            return false;
        }
        Body sootMethodBody = sootMethod.retrieveActiveBody();
        sootMethodBody.getLocals().addAll(localVars);
        sootMethodBody.getUnits().insertBefore(stmts, target);
        InitArrayBlock initArrayBlock = new InitArrayBlock(type, dimSize, (Local) array);
        initArrayBlock.setInserationTarget(target);
        return initArrayBlock.insertBlock(clazz, sootMethod);
    }

    @Override
    public Value createValue(List<Local> locals, List<Stmt> stmts) {
        return createValue(locals,stmts,null,null,null);
    }

    @Override
    public Value createValue(List<Local> locals, List<Stmt> stmts, ClassInfo classInfo, String methodSign, Unit target) {
        Local array = Jimple.v().newLocal(NameProvider.genVarName() + "_newArr", type);
        locals.add(array);
        AssignStmt assign = Jimple.v().newAssignStmt(array, Jimple.v().newNewMultiArrayExpr(type, dimSize));
        stmts.add(assign);
        return array;
    }
}
