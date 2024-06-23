package codegen.blocks.gc;

import codegen.blocks.BasicBlock;
import codegen.blocks.ClassInfo;
import soot.*;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import utils.ClassUtils;

import java.util.ArrayList;

public class SystemGCBlock extends BasicBlock {
    @Override
    public Boolean insertBlock(ClassInfo clazz, SootMethod sootMethod) {
        SootClass sootClass = ClassUtils.loadClass("java.lang.System");
        if (sootClass == null) {
            System.err.println("java.lang.System load error.");
            return false;
        }
        SootMethod method = sootClass.getMethod("gc", new ArrayList<>(), VoidType.v());
        Stmt stmt = Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(method.makeRef()));
        stmts.add(stmt);
        Body sootMethodBody = sootMethod.retrieveActiveBody();
        sootMethodBody.getLocals().addAll(localVars);
        sootMethodBody.getUnits().insertBefore(stmts, target);
        return true;
    }
}
