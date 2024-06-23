package codegen.blocks;

import soot.Body;
import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.*;

import java.util.List;

public class LoopBlock extends BasicBlock{

    protected Stmt initStmt;
    protected Stmt headStmt;
    protected Stmt backJumpStmt;
    protected Stmt stepStmt;

    protected Local reuseVar;

    protected Stmt gotoTarget;

    public Stmt getInitStmt() {
        return initStmt;
    }

    public void setInitStmt(Stmt initStmt) {
        this.initStmt = initStmt;
    }

    public Stmt getHeadStmt() {
        return headStmt;
    }

    public void setHeadStmt(Stmt headStmt) {
        this.headStmt = headStmt;
    }

    public Stmt getBackJumpStmt() {
        return backJumpStmt;
    }

    public void setBackJumpStmt(Stmt backJumpStmt) {
        this.backJumpStmt = backJumpStmt;
    }

    public Stmt getStepStmt() {
        return stepStmt;
    }

    public void setStepStmt(Stmt stepStmt) {
        this.stepStmt = stepStmt;
    }

    public Local getReuseVar() {
        return reuseVar;
    }

    public void setReuseVar(Local reuseVar) {
        this.reuseVar = reuseVar;
    }

    public Stmt getGotoTarget() {
        return gotoTarget;
    }

    public void setGotoTarget(Stmt gotoTarget) {
        this.gotoTarget = gotoTarget;
    }

    @Override
    public Boolean insertBlock(ClassInfo clazz, SootMethod sootMethod) {

        Body sootMethodBody = sootMethod.retrieveActiveBody();
        sootMethodBody.getLocals().addAll(localVars);

        Unit insertionPoint = contents.get(0);
        if (contents.size() > 0) {
            //插入包住已有代码片段的if语句
            List<Stmt> def = getDefinitions(clazz, sootMethod, contents);
            if (contents.size() - def.size() > 0) {
                // 移除原chain中的def语句
                for (Stmt stmt : def) {
                    contents.remove(stmt);
                    sootMethodBody.getUnits().remove(stmt);
                }
                insertionPoint = contents.get(0);
                // 先将def插入after前
                for (Stmt stmt : def) {
                    Unit newUnit = (Unit) stmt.clone();
                    sootMethodBody.getUnits().insertBeforeNoRedirect(newUnit, insertionPoint);
                }
            } else {
                NopStmt nop = Jimple.v().newNopStmt();
                sootMethodBody.getUnits().insertBeforeNoRedirect(nop, insertionPoint);
                contents.clear();
                contents.add(nop);
                insertionPoint = contents.get(0);
            }
        }

        //01 插入 loopIndex 的初始化语句
        sootMethodBody.getUnits().insertBeforeNoRedirect(initStmt, insertionPoint);
        //02 插入 if 语句
        sootMethodBody.getUnits().insertBeforeNoRedirect(headStmt, insertionPoint);
        //03 插入 新创建 stmts
        sootMethodBody.getUnits().insertBefore(stmts, insertionPoint);
        //04 中间是seqs， 将Loop结束的Nop语句插入到seq后一个位置
        if (contents.size() == 1 || ( insertionPoint instanceof ReturnStmt || insertionPoint instanceof ReturnVoidStmt)) {
            sootMethodBody.getUnits().insertBeforeNoRedirect(gotoTarget, insertionPoint);
        } else {
            sootMethodBody.getUnits().insertAfter(gotoTarget, contents.get(contents.size() - 1));
        }
        //05 loopStep increase stmt
        sootMethodBody.getUnits().insertBeforeNoRedirect(stepStmt, gotoTarget);
        //06 insert backjump stmt
        sootMethodBody.getUnits().insertBeforeNoRedirect(backJumpStmt, gotoTarget);

        return true;
    }

    @Override
    public String toString() {
        return "LoopBlock[ " + initStmt + " " + headStmt + " " + stepStmt + " " + backJumpStmt + " ]";
    }
}
