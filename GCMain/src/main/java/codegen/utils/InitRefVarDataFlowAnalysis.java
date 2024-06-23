package codegen.utils;

import soot.*;
import soot.JastAddJ.ExprStmt;
import soot.jimple.*;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

import java.util.HashMap;
import java.util.List;

public class InitRefVarDataFlowAnalysis extends ForwardFlowAnalysis<Unit, FlowSet<Local>> {
    HashMap<Unit, FlowSet<Local>> InResult = new HashMap<>();
    HashMap<Unit, FlowSet<Local>> OutResult = new HashMap<>();

    FlowSet<Local> allLocals;
    FlowSet<Local> refLikeLocals;

    public InitRefVarDataFlowAnalysis(UnitGraph g) {
        super(g);
        allLocals = new ArraySparseSet<>();
        refLikeLocals = new ArraySparseSet<>();

        for (Local loc : g.getBody().getLocals()) {
            allLocals.add(loc);
            if (loc.getType() instanceof RefType || loc.getType() instanceof ArrayType) {
                refLikeLocals.add(loc);
            }
        }

        doAnalysis();
    }

    @Override
    protected void flowThrough(FlowSet<Local> in, Unit unit, FlowSet<Local> out) {
        in.copy(out);

        for (ValueBox box : unit.getUseAndDefBoxes()) { // a.init
            Value value = box.getValue();
            if (value instanceof SpecialInvokeExpr) {
                SpecialInvokeExpr invokeExpr = (SpecialInvokeExpr) value;
                if (invokeExpr.getMethodRef().resolve().isConstructor()) {
                    Value base = invokeExpr.getBase();
                    if (base instanceof Local && refLikeLocals.contains((Local) base)) {
                        out.add((Local) base);
                    }
                }
            } else if (value instanceof NewExpr) {
                List<ValueBox> valueBoxList = unit.getDefBoxes();
                for (ValueBox valueBox : valueBoxList) {
                    Value v = valueBox.getValue();
                    if (v instanceof Local) {
                        out.remove((Local) v);
                    }
                }
            }
        }

        for (ValueBox defBox : unit.getDefBoxes()) {
            Value lhs = defBox.getValue();
            if (lhs instanceof Local && refLikeLocals.contains((Local) lhs)) {
                if (!unit.getUseBoxes().isEmpty()) {
                    Value useValue = unit.getUseBoxes().get(0).getValue();
                    if (!(useValue instanceof ThisRef || useValue instanceof NewExpr)
                            || (useValue instanceof Local && in.contains((Local) useValue))) {
                        // ThisRef and NewExpr need specialinvoke
                        out.add((Local) lhs);
                    }
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


}

