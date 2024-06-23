package codegen.blocks;

import soot.Body;
import soot.Local;
import soot.SootMethod;

import java.util.ArrayList;
import java.util.List;

public class StmtBlock extends BasicBlock{

    protected List<Local> reusedVars = new ArrayList<>();

    public List<Local> getReusedVars() {
        return reusedVars;
    }

    public void addAllReusedVars(List<Local> reusedVars) {
        this.reusedVars.addAll(reusedVars);
    }

    public void addReusedVar(Local reusedVar) {
        this.reusedVars.add(reusedVar);
    }

    public void setReusedVars(List<Local> reusedVars) {
        this.reusedVars = reusedVars;
    }

    @Override
    public Boolean insertBlock(ClassInfo clazz, SootMethod sootMethod) {

        if (target == null) return false;
        Body sootMethodBody = sootMethod.retrieveActiveBody();
        sootMethodBody.getLocals().addAll(localVars);
        sootMethodBody.getUnits().insertBefore(stmts, target);
        return true;
    }
}
