package codegen.operands;

import codegen.blocks.BasicBlock;
import codegen.blocks.ClassInfo;
import soot.Type;
import soot.jimple.Stmt;

/**
 * 操作数接口
 */
public interface Operand {
    BasicBlock nextVariable(ClassInfo clazz, String methodSign, Type type, Stmt target);
}
