package codegen.blocks;

import soot.Body;
import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.*;

import java.util.ArrayList;
import java.util.List;

public class IfBlock extends BasicBlock{

    protected List<Stmt> initStmts = new ArrayList<>();
    protected Stmt headStmt;

    protected Stmt gotoTarget;

    public Stmt getHeadStmt() {
        return headStmt;
    }

    public void setHeadStmt(Stmt headStmt) {
        this.headStmt = headStmt;
    }

    public Stmt getGotoTarget() {
        return gotoTarget;
    }

    public void setGotoTarget(Stmt gotoTarget) {
        this.gotoTarget = gotoTarget;
    }

    public List<Stmt> getInitStmts() {
        return initStmts;
    }

    public void addInitStmt(Stmt stmt) {
        if (initStmts == null) initStmts = new ArrayList<>();
        initStmts.add(stmt);
    }

    public void setInitStmts(List<Stmt> initStmts) {
        this.initStmts = initStmts;
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
        if (!initStmts.isEmpty()) sootMethodBody.getUnits().insertBefore(initStmts, insertionPoint);
        sootMethodBody.getUnits().insertBeforeNoRedirect(headStmt, insertionPoint);
        if (!stmts.isEmpty()) sootMethodBody.getUnits().insertBefore(stmts, insertionPoint);
        if (contents.size() == 1 || ( contents.get(0) instanceof ReturnStmt || contents.get(0) instanceof ReturnVoidStmt)) {
            // Nop语句插入到seq前
            sootMethodBody.getUnits().insertBeforeNoRedirect(gotoTarget, insertionPoint);
        } else {
            // Nop语句插入到seq后一个位置
            sootMethodBody.getUnits().insertAfter(gotoTarget, contents.get(contents.size() - 1));
        }
        return true;
    }

    @Override
    public String toString() {
        return "IfBlock[ " + stmts + " " + headStmt + " ]";
    }
}
