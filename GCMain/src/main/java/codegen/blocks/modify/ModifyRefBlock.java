package codegen.blocks.modify;

import codegen.blocks.ClassInfo;
import codegen.blocks.add.LocalArrayBlock;
import codegen.blocks.add.LocalRefBlock;
import soot.*;
import soot.jimple.FieldRef;
import soot.jimple.Jimple;
import soot.jimple.Stmt;

import java.util.List;

public class ModifyRefBlock implements ModifyBlock {
    @Override
    public boolean modify(ClassInfo clazz, SootMethod sootMethod, RefType type, Local target, List<Local> localVars, List<Stmt> stmts,Unit insertTarget) {
        LocalRefBlock refBlock = new LocalRefBlock(type);
        Value value = refBlock.createValue(localVars, stmts,clazz,sootMethod.getSignature(),insertTarget);
        if (value == null) {
            return false;
        }
        Stmt assign = Jimple.v().newAssignStmt(target, value);
        stmts.add(assign);
        return true;
    }

    @Override
    public boolean modify(ClassInfo clazz, SootMethod sootMethod, ArrayType type, List<Value> dimSize, Local target, List<Local> localVars, List<Stmt> stmts,Unit insertTarget) {
        LocalArrayBlock arrayBlock = new LocalArrayBlock(type, dimSize);
        Value value = arrayBlock.createValue(localVars, stmts,clazz,sootMethod.getSignature(),insertTarget);
        if (value == null) {
            return false;
        }
        Stmt assign = Jimple.v().newAssignStmt(target, value);
        stmts.add(assign);
        return true;
    }

    @Override
    public boolean modify(ClassInfo clazz, SootMethod sootMethod, RefType type, FieldRef target, List<Local> localVars, List<Stmt> stmts,Unit insertTarget) {
        LocalRefBlock refBlock = new LocalRefBlock(type);
        Value value = refBlock.createValue(localVars, stmts,clazz,sootMethod.getSignature(),insertTarget);
        if (value == null) {
            return false;
        }
        Stmt assign = Jimple.v().newAssignStmt(target, value);
        stmts.add(assign);
        return true;
    }

    @Override
    public boolean modify(ClassInfo clazz, SootMethod sootMethod, ArrayType type, List<Value> dimSize, FieldRef target, List<Local> localVars, List<Stmt> stmts,Unit insertTarget) {
        LocalArrayBlock arrayBlock = new LocalArrayBlock(type, dimSize);
        Value value = arrayBlock.createValue(localVars, stmts,clazz,sootMethod.getSignature(),insertTarget);
        if (value == null) {
            return false;
        }
        Stmt assign = Jimple.v().newAssignStmt(target, value);
        stmts.add(assign);
        return true;
    }
}
