package codegen.blocks.modify;

import codegen.blocks.ClassInfo;
import soot.*;
import soot.jimple.FieldRef;
import soot.jimple.Stmt;

import java.util.List;

public interface ModifyBlock {
    boolean modify(ClassInfo clazz, SootMethod sootMethod, RefType type, Local target, List<Local> localVars, List<Stmt> stmts,Unit insertTarget);

    boolean modify(ClassInfo clazz, SootMethod sootMethod, ArrayType type, List<Value> dimSize, Local target, List<Local> localVars, List<Stmt> stmts,Unit insertTarget);

    boolean modify(ClassInfo clazz, SootMethod sootMethod, RefType type, FieldRef target, List<Local> localVars, List<Stmt> stmts,Unit insertTarget);

    boolean modify(ClassInfo clazz, SootMethod sootMethod, ArrayType type, List<Value> dimSize, FieldRef target, List<Local> localVars, List<Stmt> stmts,Unit insertTarget);

}
