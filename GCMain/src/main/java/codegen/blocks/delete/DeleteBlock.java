package codegen.blocks.delete;

import codegen.blocks.ClassInfo;
import codegen.blocks.modify.ModifyBlock;
import soot.*;
import soot.jimple.*;

import java.util.List;

public class DeleteBlock implements ModifyBlock {
    @Override
    public boolean modify(ClassInfo clazz, SootMethod sootMethod, RefType type, Local target, List<Local> localVars, List<Stmt> stmts,Unit insertTarget) {
        Stmt assign = Jimple.v().newAssignStmt(target, NullConstant.v());
        stmts.add(assign);
        return true;
    }

    @Override
    public boolean modify(ClassInfo clazz, SootMethod sootMethod, ArrayType type, List<Value> dimSize, Local target, List<Local> localVars, List<Stmt> stmts,Unit insertTarget) {
        Stmt assign = Jimple.v().newAssignStmt(target, NullConstant.v());
        stmts.add(assign);
        return true;
    }

    @Override
    public boolean modify(ClassInfo clazz, SootMethod sootMethod, RefType type, FieldRef target, List<Local> localVars, List<Stmt> stmts,Unit insertTarget) {
        Stmt assign = Jimple.v().newAssignStmt(target, NullConstant.v());
        stmts.add(assign);
        return true;
    }

    @Override
    public boolean modify(ClassInfo clazz, SootMethod sootMethod, ArrayType type, List<Value> dimSize, FieldRef target, List<Local> localVars, List<Stmt> stmts,Unit insertTarget) {
        Stmt assign = Jimple.v().newAssignStmt(target, NullConstant.v());
        stmts.add(assign);
        return true;
    }
}
