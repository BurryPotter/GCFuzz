package codegen.blocks.modify;

import codegen.blocks.*;
import codegen.utils.SootHelper;
import soot.*;
import soot.jimple.*;
import soot.util.Numberable;

import java.util.List;

public class ModifyProxyBlock extends BasicBlock {
    private Numberable variable;
    private List<Value> dimSize;
    private ModifyBlock subject;

    public ModifyProxyBlock(ModifyBlock subject, Numberable localVar) {
        this.subject = subject;
        this.variable = localVar;
    }

    public ModifyProxyBlock(ModifyBlock subject, Numberable localVar, List<Value> dimSize) {
        this.variable = localVar;
        this.dimSize = dimSize;
        this.subject = subject;
    }

    public Numberable getVariable() {
        return variable;
    }

    public void setVariable(Numberable variable) {
        this.variable = variable;
    }

    public List<Value> getDimSize() {
        return dimSize;
    }

    public void setDimSize(List<Value> dimSize) {
        this.dimSize = dimSize;
    }

    public ModifyBlock getSubject() {
        return subject;
    }

    public void setSubject(ModifyBlock subject) {
        this.subject = subject;
    }

    @Override
    public Boolean insertBlock(ClassInfo clazz, SootMethod sootMethod) {
        boolean success = false;
        if (variable instanceof Local) {
            Type type = ((Local) variable).getType();
            if (type instanceof ArrayType) {
                success = subject.modify(clazz, sootMethod, (ArrayType) type, dimSize, (Local) variable, localVars, stmts,target);
            } else if (type instanceof RefType) {
                success = subject.modify(clazz, sootMethod, (RefType) type, (Local) variable, localVars, stmts,target);
            }
        } else if (variable instanceof SootField) {
            Type type = ((SootField) variable).getType();
            if (((SootField) variable).isStatic()) {
                StaticFieldRef fieldRef = Jimple.v().newStaticFieldRef(((SootField) variable).makeRef());
                if (type instanceof ArrayType) {
                    success = subject.modify(clazz, sootMethod, (ArrayType) type, dimSize, fieldRef, localVars, stmts,target);
                } else if (type instanceof RefType) {
                    success = subject.modify(clazz, sootMethod, (RefType) type, fieldRef, localVars, stmts,target);
                }
            } else {
                Value instance = SootHelper.findInstance(clazz.getSootClass(), sootMethod);
                InstanceFieldRef fieldRef = Jimple.v().newInstanceFieldRef(instance, ((SootField) variable).makeRef());
                if (type instanceof ArrayType) {
                    success = subject.modify(clazz, sootMethod, (ArrayType) type, dimSize, fieldRef, localVars, stmts,target);
                } else if (type instanceof RefType) {
                    success = subject.modify(clazz, sootMethod, (RefType) type, fieldRef, localVars, stmts,target);
                }
            }
        }
        if (!success) {
            return false;
        }
        Body sootMethodBody = sootMethod.retrieveActiveBody();
        sootMethodBody.getLocals().addAll(localVars);
        sootMethodBody.getUnits().insertBefore(stmts, target);
        return true;
    }

}
