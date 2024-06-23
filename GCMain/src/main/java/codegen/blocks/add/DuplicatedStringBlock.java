package codegen.blocks.add;

import codegen.blocks.BasicBlock;
import codegen.blocks.ClassInfo;
import codegen.providers.NameProvider;
import codegen.providers.PrimitiveValueProvider;
import soot.*;
import soot.jimple.AssignStmt;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DuplicatedStringBlock extends BasicBlock {

    private String str;

    public DuplicatedStringBlock(String str) {
        this.str = str;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    @Override
    public Boolean insertBlock(ClassInfo clazz, SootMethod sootMethod) {
        RefType strType = RefType.v("java.lang.String");
        SootMethod initMethod = strType.getSootClass().getMethod("<init>", Collections.singletonList(strType));
        List<Value> params = Collections.singletonList(StringConstant.v(str));
        int count = Math.abs(PrimitiveValueProvider.nextInt()) + 1;
        for (int i = 0; i < count; i++) {
            Local newLocal = Jimple.v().newLocal(NameProvider.genVarName() + "_newString", strType);
            AssignStmt assign = Jimple.v().newAssignStmt(newLocal, Jimple.v().newNewExpr(strType));
            Stmt invoke = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(newLocal, initMethod.makeRef(), params));
            this.addLocalVar(newLocal);
            this.addStmt(assign);
            this.addStmt(invoke);
        }
        Body sootMethodBody = sootMethod.retrieveActiveBody();
        sootMethodBody.getLocals().addAll(localVars);
        sootMethodBody.getUnits().insertBefore(stmts, target);
        return super.insertBlock(clazz, sootMethod);
    }
}
