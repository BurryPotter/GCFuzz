package codegen.blocks.add;

import codegen.blocks.BasicBlock;
import codegen.blocks.ClassInfo;
import codegen.providers.GCValueProvider;
import codegen.providers.NameProvider;
import codegen.utils.SootHelper;
import soot.*;
import soot.asm.SootClassBuilder;
import soot.jimple.*;

import java.util.List;
import java.util.stream.Collectors;

public class LocalRefBlock extends BasicBlock implements ValueCreatable {

    private RefType instanceType;

    public RefType getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(RefType instanceType) {
        this.instanceType = instanceType;
    }

    public LocalRefBlock(RefType type) {
        this.instanceType = type;
    }

    @Override
    public Boolean insertBlock(ClassInfo clazz, SootMethod sootMethod) {
        Value newRef = this.createValue(this.getLocalVars(), this.getStmts(), clazz, sootMethod.getSignature(), target);
        if (newRef == null) {
            return false;
        }
        Body sootMethodBody = sootMethod.retrieveActiveBody();
        sootMethodBody.getLocals().addAll(localVars);
        sootMethodBody.getUnits().insertBefore(stmts, target);
        return true;
    }


    @Override
    public Value createValue(List<Local> locals, List<Stmt> stmts) {
        return createValue(locals, stmts, null, null, null);
    }

    @Override
    public Value createValue(List<Local> locals, List<Stmt> stmts, ClassInfo classInfo, String methodSign, Unit target) {
        Local newLocal = Jimple.v().newLocal(NameProvider.genVarName() + "_newRef", instanceType);
        List<SootMethod> initMethods = instanceType.getSootClass().getMethods()
                .stream()
                .filter(s -> s.isConstructor() && s.isPublic())
                .collect(Collectors.toList());
        SootHelper.sortMethod(initMethods);
        SootMethod init = null;
        for (SootMethod method : initMethods) {
            if (isSupportMethod(method,instanceType)) {
                init = method;
                break;
            }
        }
        if (init == null) {
            return null;
        }
        List<Value> params = GCValueProvider.paramValue(init.getParameterTypes(), locals, stmts, classInfo, methodSign, target);
        if (params == null) {
            return null;
        }
        locals.add(newLocal);
        AssignStmt assign = Jimple.v().newAssignStmt(newLocal, Jimple.v().newNewExpr(instanceType));
        Stmt invoke = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(newLocal, init.makeRef(), params));
        stmts.add(assign);
        stmts.add(invoke);
        return newLocal;
    }

    /**
     * if the params of methods not contains `interface` or `abstract class`
     *
     * @param method
     * @return
     */
    public boolean isSupportMethod(SootMethod method,RefType refType) {
        for (Type type : method.getParameterTypes()) {
            if (type instanceof RefType) {
                SootClass sootClass = ((RefType) type).getSootClass();
                if (refType.equals(type)){
                    return false;
                }
                return !(sootClass.isAbstract() || sootClass.isInterface());
            }
        }
        return true;
    }
}
