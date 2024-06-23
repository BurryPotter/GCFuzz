package codegen.blocks;

import config.FuzzingConfig;
import soot.Body;
import soot.SootMethod;
import soot.Trap;
import soot.Unit;
import soot.jimple.*;

import java.util.ArrayList;
import java.util.List;

public class TrapBlock extends BasicBlock{

    // 标识trap头位置，使用跳转语句防止goto被优化掉
    protected Stmt initCond;
    protected Stmt headCond;

    public Stmt getInitCond() {
        return initCond;
    }

    public void setInitCond(Stmt initCond) {
        this.initCond = initCond;
    }

    public Stmt getHeadCond() {
        return headCond;
    }

    public void setHeadCond(Stmt headCond) {
        this.headCond = headCond;
    }

    protected List<Stmt> throwBlock;
    protected GotoStmt gotoStmt;
    protected List<Stmt> catchBlock;
    protected Trap trap;

    public void addThrowStmt(Stmt unit) {

        if (throwBlock == null) {
            throwBlock = new ArrayList<>();
        }
        throwBlock.add(unit);
    }

    public List<Stmt> getThrowBlock() {
        return throwBlock;
    }

    public void setThrowBlock(List<Stmt> throwBlock) {
        this.throwBlock = throwBlock;
    }

    public Trap getTrap() {
        return trap;
    }

    public void setTrap(Trap trap) {
        this.trap = trap;
    }

    public List<Stmt> getCatchBlock() {
        return catchBlock;
    }

    public void setCatchBlock(List<Stmt> catchBlock) {
        this.catchBlock = catchBlock;
    }

    public GotoStmt getGotoStmt() {
        return gotoStmt;
    }

    public void setGotoStmt(GotoStmt gotoStmt) {
        this.gotoStmt = gotoStmt;
    }

    @Override
    public Boolean insertBlock(ClassInfo clazz, SootMethod sootMethod) {

        Body sootMethodBody = sootMethod.retrieveActiveBody();
        sootMethodBody.getLocals().addAll(localVars);

        // 保存targets的头
        Unit head = contents.get(0);
        if (contents.size() > 0) {
            //插入包住已有代码片段的if语句
            List<Stmt> def = getDefinitions(clazz, sootMethod, contents);
            if (def.size() > 0 && contents.size() - def.size() > 0) {
                // 移除原chain中的def语句
                for (Stmt stmt : def) {
                    contents.remove(stmt);
                    sootMethodBody.getUnits().remove(stmt);
                }
                head = contents.get(0);
                // 先将def插入after前
                for (Stmt stmt : def) {
                    Unit newUnit = (Unit) stmt.clone();
                    sootMethodBody.getUnits().insertBeforeNoRedirect(newUnit, head);
                }
            } else {
                //TODO 不知道对不对
                NopStmt nop = Jimple.v().newNopStmt();
                sootMethodBody.getUnits().insertBeforeNoRedirect(nop, head);
                contents.clear();
                contents.add(nop);
                head = contents.get(0);
            }
        }
        //02 语句插入到seq后一个位置
        Unit tail = getNextUnit(sootMethodBody, contents.get(contents.size() - 1));
        //03 调整goto跳转到target
        gotoStmt.setTarget(tail);
        //04 set target of if stmt
        ((IfStmt) headCond).setTarget(gotoStmt);
        //05 insert if condition and if stmt
//        sootMethodBody.getUnits().insertBefore(stmts, tail);
        if (initCond !=null){
            sootMethodBody.getUnits().insertBeforeNoRedirect(initCond, tail);
        }
        sootMethodBody.getUnits().insertBeforeNoRedirect(headCond, tail);
        //06 insert try block body
        if (FuzzingConfig.flipCoin(FuzzingConfig.RandomType.OPERATOR_POOL)){
            for (Stmt stmt : throwBlock) {
                sootMethodBody.getUnits().insertBeforeNoRedirect(stmt, tail);
            }
        }
        //07 插入goto语句
        sootMethodBody.getUnits().insertBeforeNoRedirect(gotoStmt, tail);
        //08 插入catch语句
        for (Stmt stmt : catchBlock) {
            sootMethodBody.getUnits().insertBeforeNoRedirect(stmt, tail);
        }
        //09 set trap stmt
        trap.setBeginUnit(head);
        trap.setEndUnit(gotoStmt);
        //10 插入trap
        sootMethodBody.getTraps().add(trap);

        return true;
    }

    @Override
    public String toString() {
        return "TrapBlock[ " + throwBlock + " " + gotoStmt + " " + catchBlock + " ]";
    }
}
