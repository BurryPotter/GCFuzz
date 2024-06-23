package codegen.blocks.add;

import codegen.blocks.ClassInfo;
import soot.Local;
import soot.Unit;
import soot.Value;
import soot.jimple.Stmt;

import java.util.List;

public interface ValueCreatable {
    public Value createValue(List<Local> locals, List<Stmt> stmts);
    public Value createValue(List<Local> locals, List<Stmt> stmts, ClassInfo classInfo,String methodSign,Unit target);

}
