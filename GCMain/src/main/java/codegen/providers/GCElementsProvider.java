package codegen.providers;

import codegen.blocks.ClassInfo;
import codegen.utils.DeathVarAnalysis;
import codegen.utils.InitRefVarDataFlowAnalysis;
import codegen.utils.NullVarAnalysis;
import codegen.utils.SootHelper;
import soot.*;
import soot.jimple.toolkits.pointer.DumbPointerAnalysis;
import soot.jimple.toolkits.pointer.LocalMustAliasAnalysis;
import soot.toolkits.graph.CompleteUnitGraph;
import soot.toolkits.scalar.FlowSet;
import soot.util.Numberable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GCElementsProvider {
    public static FlowSet<Local> getAvailableVars(ClassInfo clazz, String methodSign, Unit target) {
        SootMethod sootMethod = clazz.getMethodMaps().get(methodSign);
        Body body = sootMethod.retrieveActiveBody();
        InitRefVarDataFlowAnalysis analysis = new InitRefVarDataFlowAnalysis(new CompleteUnitGraph(body));
        return analysis.getInLocal(target);
    }

    public static FlowSet<Local> getDeathVars(ClassInfo clazz, String methodSign, Unit target) {
        SootMethod sootMethod = clazz.getMethodMaps().get(methodSign);
        Body body = sootMethod.retrieveActiveBody();
        CompleteUnitGraph graph = new CompleteUnitGraph(body);
        DeathVarAnalysis analysis = new DeathVarAnalysis(graph);
        return analysis.getFlowBefore(target);
    }
    public static FlowSet<Local> getNullVars(ClassInfo clazz, String methodSign, Unit target) {
        SootMethod sootMethod = clazz.getMethodMaps().get(methodSign);
        Body body = sootMethod.retrieveActiveBody();
        NullVarAnalysis analysis = new NullVarAnalysis(new CompleteUnitGraph(body));
        return analysis.getFlowBefore(target);
    }
    public static List<Numberable> getAvailableValues(ClassInfo clazz, String methodSign, Unit targetUnit) {

        List<Numberable> candidateVars = new ArrayList<>();
        //通过flag判断是否使用局部变量
        if (targetUnit != null) {
            candidateVars.addAll(getAvailableVars(clazz, methodSign, targetUnit).toList());
        }
        //获取所有field
        if (clazz.getMethodMaps().get(methodSign).isStatic()) {

            candidateVars.addAll(clazz.getSootClass()
                    .getFields()
                    .stream()
                    .filter(field -> (field.isStatic()
                            && !field.getDeclaration().contains("final"))).collect(Collectors.toList()));
        } else {
            Value instance = SootHelper.findInstance(clazz.getSootClass(),clazz.getMethodMaps().get(methodSign),false);
            if (instance != null && !candidateVars.contains(instance)){
                return candidateVars;
            }
            candidateVars.addAll(clazz.getSootClass()
                    .getFields()
                    .stream()
                    .filter(field -> (!field.getDeclaration().contains("final"))).collect(Collectors.toList()));
        }
        return candidateVars;
    }
}
