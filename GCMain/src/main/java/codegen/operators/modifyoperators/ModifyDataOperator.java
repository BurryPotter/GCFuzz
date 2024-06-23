package codegen.operators.modifyoperators;

import codegen.blocks.BasicBlock;
import codegen.blocks.ClassInfo;
import codegen.blocks.modify.ModifyDataBlock;
import codegen.blocks.modify.ModifyProxyBlock;
import codegen.operators.Generic;
import codegen.providers.GCElementsProvider;
import config.FuzzingConfig;
import config.GCConfiguration;
import soot.*;
import soot.jimple.Stmt;
import soot.jimple.internal.JimpleLocal;
import soot.toolkits.scalar.FlowSet;
import soot.util.Numberable;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ModifyDataOperator extends Generic {
    protected static ModifyDataOperator aop;

    private ModifyDataOperator() {
    }

    public static ModifyDataOperator getInstance() {
        if (aop == null) {
            aop = new ModifyDataOperator();
        }
        return aop;
    }

    @Override
    public BasicBlock nextBlock(ClassInfo clazz, String methodSign, List<Stmt> targets) {
        Stmt insertTarget = targets.get(FuzzingConfig.nextChoice(targets.size(),FuzzingConfig.RandomType.OPERATOR_POOL));
        List<Numberable> availableValues = GCElementsProvider.getAvailableValues(clazz, methodSign, null);
        FlowSet<Local> initAndNonNull = GCElementsProvider.getAvailableVars(clazz, methodSign, insertTarget);
        initAndNonNull.difference(GCElementsProvider.getNullVars(clazz, methodSign, insertTarget));
        availableValues.addAll(initAndNonNull.toList());
        availableValues = availableValues
                .stream()
                .filter(value -> {
                    Type type = null;
                    if (value instanceof JimpleLocal) {
                        type = ((JimpleLocal) value).getType();

                    } else if (value instanceof SootField) {
                        type = ((SootField) value).getType();
                    }
                    if (type != null) {
                        if (type instanceof NullType) {
                            return false;
                        }
                        if (type instanceof RefType) {
                            String typeName = ((RefType) type).getClassName();
                            boolean res = true;
                            for (String className : GCConfiguration.FORBIDDEN_MODIFY_CLASS) {
                                res = res && !typeName.equals(className);
                            }
                            return res;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
        if (availableValues.size() == 0) {
            return null;
        }
        Numberable targetNumerable = availableValues.get(FuzzingConfig.nextChoice(availableValues.size(),FuzzingConfig.RandomType.OPERATOR_POOL));
        ModifyProxyBlock block = new ModifyProxyBlock(new ModifyDataBlock(), targetNumerable);
        block.setInserationTarget(insertTarget);
        return block;
    }
}
