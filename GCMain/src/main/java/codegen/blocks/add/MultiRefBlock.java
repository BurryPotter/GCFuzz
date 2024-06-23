package codegen.blocks.add;

import codegen.blocks.BasicBlock;
import codegen.blocks.ClassInfo;
import codegen.providers.GCValueProvider;
import soot.*;
import soot.jimple.Stmt;

import java.util.ArrayList;
import java.util.List;

public class MultiRefBlock extends BasicBlock {
    private List<RefType> instanceType;

    public List<RefType> getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(List<RefType> instanceType) {
        this.instanceType = instanceType;
    }

    public MultiRefBlock(List<RefType> instanceType) {
        this.instanceType = instanceType;
    }

    @Override
    public Boolean insertBlock(ClassInfo clazz, SootMethod sootMethod) {
        List<Local> locals = new ArrayList<>(this.getLocalVars());
        List<Stmt> stmts = new ArrayList<>(this.getStmts());
        boolean success = false;
        for (RefType type : instanceType) {
            if (type.getClassName().equals("java.lang.String")) {
                DuplicatedStringBlock block = new DuplicatedStringBlock(GCValueProvider.nextString());
                block.setInserationTarget(getInserationTarget());
                block.insertBlock(clazz, sootMethod);
            } else {
                LocalRefBlock localRefBlock = new LocalRefBlock(type);
                Value newRef = localRefBlock.createValue(locals, stmts, clazz, sootMethod.getSignature(), getInserationTarget());
                if (newRef != null) {
                    success = true;
                }
            }

        }
        Body sootMethodBody = sootMethod.retrieveActiveBody();
        sootMethodBody.getLocals().addAll(locals);
        sootMethodBody.getUnits().insertBefore(stmts, target);
        return success;
    }
}
