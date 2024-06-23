package codegen.utils;

import soot.*;
import soot.jimple.NewExpr;
import soot.jimple.ThisRef;
import soot.jimple.internal.JInstanceFieldRef;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.BackwardFlowAnalysis;
import soot.toolkits.scalar.FlowSet;

public class DeathVarAnalysis extends BackwardFlowAnalysis<Unit, FlowSet<Local>> {


    FlowSet<Local> refLikeLocals;
    /**
     * Construct the analysis from a DirectedGraph representation of a Body.
     *
     * @param graph
     */
    public DeathVarAnalysis(UnitGraph graph) {
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
        for (ValueBox valueBox : unit.getUseBoxes()) {
            Value value = valueBox.getValue();
            if (value instanceof Local && refLikeLocals.contains((Local) value)){
                if (out.contains((Local) value)){
                    out.remove((Local)value);
                }
            }
        }
        for (ValueBox valueBox: unit.getUseAndDefBoxes()){
            Value value = valueBox.getValue();
            if (value instanceof JInstanceFieldRef){
                Value instance = ((JInstanceFieldRef) value).getBaseBox().getValue();
                if (instance instanceof Local && refLikeLocals.contains((Local) instance)){
                    if (out.contains((Local) instance)){
                        out.remove((Local)instance);
                    }
                }
            }
        }
    }

    @Override
    protected FlowSet<Local> newInitialFlow() {
        FlowSet<Local> flowSet = new ArraySparseSet<>();
        refLikeLocals.copy(flowSet);
        return flowSet;
    }

    @Override
    protected void merge(FlowSet<Local> in1, FlowSet<Local> in2, FlowSet<Local> out) {
        in1.intersection(in2, out);
    }

    @Override
    protected void copy(FlowSet<Local> source, FlowSet<Local> dest) {
        source.copy(dest);

    }
}
