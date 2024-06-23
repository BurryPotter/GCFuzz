package codegen.utils;

import soot.*;
import soot.jimple.NewExpr;
import soot.jimple.NullConstant;
import soot.jimple.ThisRef;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;

public class NullVarAnalysis extends ForwardFlowAnalysis<Unit, FlowSet<Local>> {

    FlowSet<Local> refLikeLocals;

    /**
     * Construct the analysis from a DirectedGraph representation of a Body.
     *
     * @param graph
     */
    public NullVarAnalysis(UnitGraph graph) {
        super(graph);
        refLikeLocals = new ArraySparseSet<>();

        for (Local loc : graph.getBody().getLocals()) {
            if (loc.getType() instanceof RefType || loc.getType() instanceof ArrayType) {
                refLikeLocals.add(loc);
            }
        }
        doAnalysis();
    }

    @Override
    protected void flowThrough(FlowSet<Local> in, Unit unit, FlowSet<Local> out) {
        in.copy(out);
        for (ValueBox defBox : unit.getDefBoxes()) {
            Value lhs = defBox.getValue();
            if (lhs instanceof Local && refLikeLocals.contains((Local) lhs)) {
                if (!unit.getUseBoxes().isEmpty()) {
                    Value useValue = unit.getUseBoxes().get(0).getValue();
                    if (useValue instanceof NullConstant) {
                        // ThisRef and NewExpr need specialinvoke
                        out.add((Local) lhs);
                    } else {
                        out.remove((Local) lhs);
                    }
                }
            }

        }
    }

    @Override
    protected FlowSet<Local> newInitialFlow() {
        return new ArraySparseSet<>();

    }

    @Override
    protected void merge(FlowSet<Local> in1, FlowSet<Local> in2, FlowSet<Local> out) {
        in1.union(in2, out);
    }

    @Override
    protected void copy(FlowSet<Local> source, FlowSet<Local> dest) {
        source.copy(dest);
    }
}
