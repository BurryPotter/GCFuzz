package codegen.blocks.controll;

import codegen.blocks.BasicBlock;
import codegen.blocks.ClassInfo;
import codegen.providers.NameProvider;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.JAssignStmt;
import utils.ClassUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CheckBlock extends BasicBlock {
    private Value cond;

    public CheckBlock(Value cond) {
        this.cond = cond;
    }

    public Value getCond() {
        return cond;
    }

    public void setCond(Value cond) {
        this.cond = cond;
    }

    @Override
    public Boolean insertBlock(ClassInfo clazz, SootMethod sootMethod) {
        if (cond == null) {
            return false;
        }
        Body sootMethodBody = sootMethod.retrieveActiveBody();
        RefType refType = RefType.v("java.io.PrintStream");
        String varName = NameProvider.genVarName() + "_printer";
        Local printer = Jimple.v().newLocal(varName, refType);
        SootClass system = ClassUtils.loadClass("java.lang.System");
        Stmt assignStmt = Jimple.v().newAssignStmt(printer, Jimple.v().newStaticFieldRef(system.getField("err", refType).makeRef()));
        Local msg = Jimple.v().newLocal(NameProvider.genVarName() + "_msg", RefType.v("java.lang.String"));
        Stmt msgAssign = Jimple.v().newAssignStmt(msg, StringConstant.v("GCFuzzing: " + varName + "CheckError"));
        SootMethod printMethod = refType.getSootClass().getMethod("void println(java.lang.String)");
        Stmt printStmt = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(printer, printMethod.makeRef(), msg));


//        RefType refType = RefType.v("java.io.PrintStream");
//        Local newLocal = Jimple.v().newLocal(NameProvider.genVarName() + "_printer", refType);
//        Local msg = Jimple.v().newLocal(NameProvider.genVarName() + "_msg", RefType.v("java.lang.String"));
//        Stmt msgAssign = Jimple.v().newAssignStmt(msg, StringConstant.v("GCFuzzing: Check Error"));
//        AssignStmt assign = Jimple.v().newAssignStmt(newLocal, Jimple.v().newNewExpr(refType));
//        SootMethod method = refType.getSootClass().getMethod("", new ArrayList<>(),refType);
//
////        Stmt invoke = Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(,StringConstant.v("GCFuzzing: Check Error")));
//        Stmt throwStmt = Jimple.v().newThrowStmt(newLocal);

        Stmt nop = Jimple.v().newNopStmt();

        IfStmt ifStmt = Jimple.v().newIfStmt(cond, Jimple.v().newNopStmt());

        ifStmt.setTarget(nop);
        sootMethodBody.getLocals().add(printer);
        sootMethodBody.getLocals().add(msg);

        sootMethodBody.getUnits().insertBeforeNoRedirect(nop, this.target);

//                body.getUnits().insertBeforeNoRedirect(gotoStmt, nop);
        sootMethodBody.getUnits().insertBeforeNoRedirect(ifStmt, nop);

        sootMethodBody.getUnits().insertBeforeNoRedirect(assignStmt, nop);
        sootMethodBody.getUnits().insertBeforeNoRedirect(msgAssign, nop);
//        sootMethodBody.getUnits().insertBeforeNoRedirect(invoke, nop);
        sootMethodBody.getUnits().insertBeforeNoRedirect(printStmt, nop);

        return true;
    }
}
