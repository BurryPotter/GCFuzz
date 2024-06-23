package codegen.blocks.gc;

import codegen.blocks.BasicBlock;
import codegen.blocks.ClassInfo;
import codegen.providers.NameProvider;
import soot.*;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import utils.ClassUtils;

import java.util.ArrayList;

public class WhiteBoxGCBlock extends BasicBlock {
    private GCType type;

    public WhiteBoxGCBlock(GCType type) {
        this.type = type;
    }

    public GCType getType() {
        return type;
    }

    public void setType(GCType type) {
        this.type = type;
    }

    @Override
    public Boolean insertBlock(ClassInfo clazz, SootMethod sootMethod) {
        SootClass sootClass = ClassUtils.loadClass("sun.hotspot.WhiteBox");
        if (sootClass == null) {
            System.err.println("sun.hotspot.WhiteBox load error.");
            return false;
        }
        if (sootClass.getMethods().isEmpty()) {
            System.err.println("sun.hotspot.WhiteBox load error. Methods is empty.");
            SystemGCBlock block = new SystemGCBlock();
            block.setInserationTarget(target);
            return block.insertBlock(clazz,sootMethod);
        }

        SootMethod method = sootClass.getMethod("getWhiteBox", new ArrayList<>(), RefType.v(sootClass));
        Local whiteBoxInstance = Jimple.v().newLocal(NameProvider.genVarName() + "_whitebox", RefType.v(sootClass));
        localVars.add(whiteBoxInstance);
        Stmt assign = Jimple.v().newAssignStmt(whiteBoxInstance, Jimple.v().newStaticInvokeExpr(method.makeRef()));
        stmts.add(assign);
        SootMethod gc = sootClass.getMethod("getWhiteBox", new ArrayList<>(), RefType.v(sootClass));
        switch (type) {
            case FullGC:
                gc = sootClass.getMethod("fullGC", new ArrayList<>(), VoidType.v());
                break;
            case YoungGC:
                gc = sootClass.getMethod("youngGC", new ArrayList<>(), VoidType.v());
                break;
        }
        Stmt invoke = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(whiteBoxInstance, gc.makeRef()));
        stmts.add(invoke);
        Body sootMethodBody = sootMethod.retrieveActiveBody();
        sootMethodBody.getLocals().addAll(localVars);
        sootMethodBody.getUnits().insertBefore(stmts, target);
        return true;
    }
}
