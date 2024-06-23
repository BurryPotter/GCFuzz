package utils;

import soot.Local;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.*;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

import java.util.HashMap;

public class InitDataFlowAnalysis extends ForwardFlowAnalysis<Unit, FlowSet<Local>> {
    HashMap<Unit, FlowSet<Local>> InResult = new HashMap<>();
    HashMap<Unit, FlowSet<Local>> OutResult = new HashMap<>();
    HashMap<Local, Unit> DefUnit = new HashMap<>();

    FlowSet<Local> allLocals;

    public InitDataFlowAnalysis(UnitGraph g) {
        super(g);
        allLocals = new ArraySparseSet<>();
        for (Local loc : g.getBody().getLocals()) {
            allLocals.add(loc);
        }

        doAnalysis();
    }

    @Override
    protected void flowThrough(FlowSet<Local> in, Unit unit, FlowSet<Local> out) {
        in.copy(out);

        for (ValueBox box: unit.getUseAndDefBoxes()) { // a.init
            Value value = box.getValue();
            if (value instanceof SpecialInvokeExpr) {
                SpecialInvokeExpr invokeExpr = (SpecialInvokeExpr) value;
                if (invokeExpr.getMethodRef().resolve().isConstructor()) {
                    Value base = invokeExpr.getBase();
                    if (base instanceof Local) {
                        out.add((Local) base);
                    }
                }
            }
        }

        for (ValueBox defBox : unit.getDefBoxes()) {
            Value lhs = defBox.getValue();
            if (lhs instanceof Local) {
                if (! unit.getUseBoxes().isEmpty()) {
                    if (unit.getUseBoxes().get(0).getValue() instanceof NewExpr
                            || unit.getUseBoxes().get(0).getValue() instanceof NewArrayExpr
                            || unit.getUseBoxes().get(0).getValue() instanceof NewMultiArrayExpr) {
                        DefUnit.put((Local)lhs, unit);
                        continue;
                    }
                    out.add((Local) lhs);
                } else {
                    out.add((Local) lhs); // for example, long a = System.currentTimeMillis();
                }
            } else if (lhs instanceof ArrayRef) {
                Value base = ((ArrayRef) lhs).getBase();
                if (base instanceof Local) {
                    out.add((Local) base);
                }
            }
        }
        InResult.put(unit, in);
        OutResult.put(unit, out);
    }

    @Override
    protected FlowSet<Local> entryInitialFlow() {
        return new ArraySparseSet<>();
    }

    @Override
    protected FlowSet<Local> newInitialFlow() {
        return new ArraySparseSet<>();
    }


    @Override
    protected void merge(FlowSet<Local> in1, FlowSet<Local> in2, FlowSet<Local> out) {
        in1.intersection(in2, out);
    }

    @Override
    protected void copy(FlowSet<Local> source, FlowSet<Local> dest) {
        source.copy(dest);
    }

    public FlowSet<Local> getInLocal(Unit unit) {
        return InResult.getOrDefault(unit, new ArraySparseSet<>());
    }

    public FlowSet<Local> getOutLocal(Unit unit) {
        return OutResult.getOrDefault(unit, new ArraySparseSet<>());
    }

    public Unit getDefUnit(Local local) {
        return DefUnit.get(local);
    }


}

