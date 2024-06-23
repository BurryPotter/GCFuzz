package codegen.blocks;

import soot.*;
import soot.jimple.ArrayRef;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.toolkits.graph.CompleteUnitGraph;
import soot.toolkits.graph.DominatorsFinder;
import soot.toolkits.graph.MHGDominatorsFinder;
import utils.InitDataFlowAnalysis;

import java.util.*;

    public class Generic {

    public Unit getNextUnit(Body body, Unit targetUnit) {

        Unit ret = null;

        Iterator<Unit> unitIterator = body.getUnits().iterator();
        while (unitIterator.hasNext()) {
            ret = unitIterator.next();
            if (ret == targetUnit && unitIterator.hasNext()) {
                ret = unitIterator.next();
                break;
            }
        }
        return ret;
    }

    /**
     * @description: 判断需要被包裹的代码块中的定义语句是否需要提前
     * @param clazz:
     * @param sootMethod:
     * @return void
     * @author: jiachen
     * @date: 2022/8/10 12:21
     */
    public List<Stmt> getDefinitions(ClassInfo clazz, SootMethod sootMethod, List<Stmt> seq) {

        Body sootMethodBody = sootMethod.retrieveActiveBody();
        List<Stmt> def = new ArrayList<>();
        if (seq.size() <= 0) {
            return def;
        }
        InitDataFlowAnalysis analysis = new InitDataFlowAnalysis(new CompleteUnitGraph(sootMethodBody));
        Iterator<Unit> units = sootMethodBody.getUnits().iterator(seq.get(0), seq.get(seq.size() - 1));
        while (units.hasNext()) {
            Unit stmt = units.next();
            // 变量赋值，初始化
            if (! stmt.getDefBoxes().isEmpty()) {
                for (ValueBox box: stmt.getDefBoxes()) {
                    Value value = box.getValue();
                    if (value instanceof Local) { // 判断是否是局部变量（包括 基础变量 和 对象new）
                        Local local = (Local) value;
                        if(value instanceof ArrayRef){ // 判断是否是 数组 的定义
                            Value base = ((ArrayRef) value).getBase();
                            local = (Local) base;
                        }
                        if(! analysis.getInLocal(stmt).toList().contains(local)) {
                            def.add((Stmt) stmt);
                        }
                    }
                }
            } else if (!stmt.getUseBoxes().isEmpty()){ // 寻找对象的 构造语句 （new 与 构造 需要同时提前）
                for (ValueBox box: stmt.getUseBoxes()) {
                    if (box.getValue() instanceof SpecialInvokeExpr) {
                        SpecialInvokeExpr expr = (SpecialInvokeExpr) box.getValue();
                        if (expr.getMethodRef().resolve().isConstructor()) {
                            def.add((Stmt) stmt);
                        }
                    }
                }
                // 变量使用，不需要提前
            }
        }

        Set<List<Stmt>> finalSet = getUnitChain(def);
        List<Stmt> removeDef = removedef(clazz, sootMethod, seq, def, finalSet);

        def.clear();
        def.addAll(removeDef);
        return def;
    }

    private Set<List<Stmt>> getUnitChain(List<Stmt> def) {
        Set<List<Stmt>> defChain = new HashSet<>();
        Set<List<Stmt>> finalSet = new HashSet<>();
        Set<Stmt> meetUnits = new HashSet<>();
        // 遍历所有提取出的def语句
        for (Stmt stmt : def) {
            boolean hasChain = false;
            // 遍历当前已经提取出来的chains，如果有符合的链加在后面
            for (List<Stmt> unitList : defChain) {
                unitListLabel:
                // 遍历chain，看是否有定义调用当前seq
                for (int i = 0; i < unitList.size(); i++) {
                    for (ValueBox box : unitList.get(i).getDefBoxes()) { // 全部已经声明的def
                        // 对于每一条语句的usebox，由于存在多条usebox，会被添加到不同的chain中，我们需要整合这些chain
                        if(unitList.get(i).equals(stmt)) {
                            break unitListLabel; // var_5 = var_5 + 0;
                        }
                        for (ValueBox useBox : stmt.getUseBoxes()) {
                            if (box.getValue().equals(useBox.getValue())) {
                                if(hasChain) {
                                    // 说明有至少有两条链包含了该条语句
                                    meetUnits.add(stmt);
                                }
                                unitList.add(stmt);
                                hasChain = true;
                                continue unitListLabel;
                            }
                        }
                    }
                }
            }

            if(!hasChain) {
                List<Stmt> unitList = new ArrayList<>();
                unitList.add(stmt);
                defChain.add(unitList);
            }
        }

        if(meetUnits.isEmpty()) {
            finalSet.addAll(defChain);
            return finalSet;
        }

        // 不同待归并的代码
        /*
        *   sets : (set1, set2)
        *   set1 : 都包含var0.（var1, var2, var3）
        *   set2 : 都包含$r4.(r11)
        *
        * */
        Set<Set<List<Stmt>>> sets = new HashSet<>();
        for (Unit meetUnit : meetUnits) {
            Set<List<Stmt>> set = new HashSet<>();
            for (List<Stmt> unitList : defChain) {
                if (unitList.contains(meetUnit)) {
                    set.add(unitList); // 待归并的几个list，按照def中的顺序进行归并
                }
            }
            sets.add(set);
        }

        for (List<Stmt> unitLists : defChain) {
            boolean inSets = false;
            for (Set<List<Stmt>> set : sets) {
                if (set.contains(unitLists)) {
                    inSets = true;
                }
            }
            if(!inSets) {
                finalSet.add(unitLists);
            }
        }

        for (Set<List<Stmt>> set : sets) {
            for (Stmt meetUnit : meetUnits) {
                // 按照def的顺序归并list
                List<Stmt> list = new ArrayList<>(); // 归并后的list
                for (Stmt stmt : def) {
                    boolean hasContain = false;
                    for (List<Stmt> unitList : set) {
                        for (Unit unit : unitList) {
                            if (stmt.equals(unit) && !hasContain && unitList.contains(meetUnit)) {
                                list.add(stmt);
                                hasContain = true;
                            }
                        }
                    }
                }
                if(!list.isEmpty()) {
                    finalSet.add(list);
                }
            }
        }

        return finalSet;
    }


    public List<Stmt> removedef(ClassInfo clazz, SootMethod sootMethod, List<Stmt> seq, List<Stmt> def, Set<List<Stmt>> finalSet) {
        List<Stmt> removeDef = new ArrayList<>();

        Body sootMethodBody = sootMethod.retrieveActiveBody();
        CompleteUnitGraph graph = new CompleteUnitGraph(sootMethodBody);
        DominatorsFinder<Unit> df = new MHGDominatorsFinder<>(graph);

        // 遍历从seq后一句到最后
        Unit next = getNextUnit(sootMethodBody, seq.get(seq.size() - 1));
        Iterator<Unit> units = sootMethodBody.getUnits().iterator(next, sootMethodBody.getUnits().getLast());
        while (units.hasNext()) {
            Unit unit = units.next();
            List<Unit> dom = df.getDominators(unit);
            // 该语句是否使用def, 以及该语句是否被def支配

            // unit 后面的语句
            for (ValueBox useBox : unit.getUseBoxes()) {
                for (Unit stmt : dom) { // 支配该语句的所有语句
                    for (ValueBox defBox : stmt.getDefBoxes()) {
                        if (defBox.getValue().equals(useBox.getValue())) {
                            // 将该条语句的使用情况设置为被调用
                            if(def.contains(stmt)) {
                                for (List<Stmt> unitList : finalSet) {
                                    if(unitList.contains(stmt)) {
                                        removeDef.addAll(unitList.subList(0, unitList.indexOf(stmt) + 1));
                                    }
                                }
                            }
                        } else if (defBox.getValue() instanceof ArrayRef) {
                            Value base = ((ArrayRef) defBox.getValue()).getBase();
                            if (base.equals(useBox.getValue())) {
                                if(def.contains(stmt)) {
                                    for (List<Stmt> unitList : finalSet) {
                                        if(unitList.contains(stmt)) {
                                            removeDef.addAll(unitList.subList(0, unitList.indexOf(stmt) + 1));
                                        }
                                    }
                                }
                            }
                            if (useBox.getValue() instanceof ArrayRef) {
                                if (((ArrayRef) useBox.getValue()).getBase().equals(base)) {
                                    if(def.contains(stmt)) {
                                        for (List<Stmt> unitList : finalSet) {
                                            if(unitList.contains(stmt)) {
                                                removeDef.addAll(unitList.subList(0, unitList.indexOf(stmt) + 1));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (useBox.getValue().getType() instanceof RefType) {
                    for (Unit stmt : dom) {
                        for (ValueBox box : stmt.getUseBoxes()) { // def中所有的定义
                            if (box.getValue() instanceof SpecialInvokeExpr) {
                                SpecialInvokeExpr expr = (SpecialInvokeExpr) box.getValue();
                                if (expr.getMethodRef().resolve().isConstructor()) {
                                    if (useBox.getValue().equals(expr.getBase())) {
                                        if(def.contains(stmt)) {
                                            for (List<Stmt> unitList : finalSet) {
                                                if(unitList.contains(stmt)) {
                                                    removeDef.addAll(unitList.subList(0, unitList.indexOf(stmt) + 1));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return removeDef;
    }


}
