package codegen.blocks;

import soot.Local;
import soot.SootMethod;
import soot.jimple.Stmt;

import java.util.ArrayList;
import java.util.List;

public class BasicBlock extends Generic{

    protected List<Local> localVars;
    protected List<Stmt> stmts;

    protected List<Stmt> contents;
    protected Local reuseVar; // for if trap 的重用变量


    protected Stmt target;

    public BasicBlock() {
        localVars = new ArrayList<>();
        stmts = new ArrayList<>();
    }

    public void addLocalVar(Local var) {
        localVars.add(var);
    }

    public void addAllLocalVars(List<Local> vars) {
        localVars.addAll(vars);
    }

    public void addStmt(Stmt stmt) {
        stmts.add(stmt);
    }

    public void addAllStmts(List<Stmt> stmts) {
        this.stmts.addAll(stmts);
    }

    public List<Local> getLocalVars() {
        return localVars;
    }

    public void setLocalVars(ArrayList<Local> localVars) {
        this.localVars = localVars;
    }

    public List<Stmt> getStmts() {
        return stmts;
    }

    public void setStmts(ArrayList<Stmt> stmts) {
        this.stmts = stmts;
    }

    public List<Stmt> getContents() {
        return contents;
    }

    public void addAllContents(List<Stmt> stmts) {
        this.contents.addAll(stmts);
    }

    public void setContents(List<Stmt> contents) {
        this.contents = contents;
    }

    public Local getReuseVar() {
        return reuseVar;
    }

    public void setReuseVar(Local reuseVar) {
        this.reuseVar = reuseVar;
    }

    public Stmt getInserationTarget() {
        return target;
    }

    public void setInserationTarget(Stmt target) {
        this.target = target;
    }

    public Boolean insertBlock(ClassInfo clazz, SootMethod sootMethod){
        return false;
    }

    @Override
    public String toString() {
        return stmts.toString();
    }
}
