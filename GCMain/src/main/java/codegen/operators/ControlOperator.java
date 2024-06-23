package codegen.operators;

import codegen.blocks.BasicBlock;
import codegen.blocks.ClassInfo;
import codegen.operators.control.CheckOperator;
import codegen.operators.control.GCTrapOperator;
import config.FuzzingConfig;
import soot.jimple.Stmt;

import java.util.*;

public class ControlOperator extends Generic {
    //    public ArrayList<Operator> controlOperators = new ArrayList<>();
    public TreeMap<Integer, Operator> controlOperators = new TreeMap<>();

    private ControlOperator() {
        controlOperators.put(20, LoopOperator.getInstance());
//        controlOperators.put(40, CheckOperator.getInstance());
        controlOperators.put(100, GCTrapOperator.getInstance());
    }

    private static final class AopHolder {
        static final ControlOperator aop = new ControlOperator();
    }

    public static ControlOperator getInstance() {
        return ControlOperator.AopHolder.aop;
    }

    public TreeMap<Integer, Operator> getControlOperators() {
        return controlOperators;
    }

    public void setControlOperators(TreeMap<Integer, Operator> controlOperators) {
        this.controlOperators = controlOperators;
    }

    @Override
    public BasicBlock nextBlock(ClassInfo clazz, String methodSign, List<Stmt> targets) {
        int priority = FuzzingConfig.nextChoice(100,FuzzingConfig.RandomType.OPERATOR_POOL);
        Iterator<Integer> iterator = controlOperators.keySet().iterator();
        Operator operator = GCTrapOperator.getInstance();
        while (iterator.hasNext()) {
            int p = iterator.next();
            if (priority < p) {
                operator = controlOperators.get(p);
                break;
            }
        }

        return operator.nextBlock(clazz, methodSign, targets);
    }
}
