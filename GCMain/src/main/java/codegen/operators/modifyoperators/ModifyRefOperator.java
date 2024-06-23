package codegen.operators.modifyoperators;

import codegen.blocks.BasicBlock;
import codegen.blocks.ClassInfo;
import codegen.blocks.modify.ModifyProxyBlock;
import codegen.blocks.modify.ModifyRefBlock;
import codegen.operators.Generic;
import codegen.providers.GCElementsProvider;
import codegen.providers.GCValueProvider;
import config.FuzzingConfig;
import config.GCConfiguration;
import soot.*;
import soot.jimple.Ref;
import soot.jimple.Stmt;
import soot.jimple.internal.JimpleLocal;
import soot.util.Chain;
import soot.util.Numberable;

import java.util.List;
import java.util.stream.Collectors;

public class ModifyRefOperator extends Generic {
    protected static ModifyRefOperator aop;

    private ModifyRefOperator() {
    }

    public static ModifyRefOperator getInstance() {
        if (aop == null) {
            aop = new ModifyRefOperator();
        }
        return aop;
    }

    @Override
    public BasicBlock nextBlock(ClassInfo clazz, String methodSign, List<Stmt> targets) {
        Stmt insertTarget = targets.get(FuzzingConfig.nextChoice(targets.size(),FuzzingConfig.RandomType.OPERATOR_POOL));
        SootMethod sootMethod = clazz.getMethodMaps().get(methodSign);
        Body body = sootMethod.retrieveActiveBody();
        Chain<Local> chain = body.getLocals();
        List<Numberable> availableValues = GCElementsProvider.getAvailableValues(clazz, methodSign, null);
        availableValues.addAll(chain);
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
        ModifyProxyBlock block = null;
        Type type = null;
        if (targetNumerable instanceof Local) {
            type = ((Local) targetNumerable).getType();
        } else if (targetNumerable instanceof SootField) {
            type = ((SootField) targetNumerable).getType();
        } else {
            return null;
        }
        if (type instanceof ArrayType) {
            List<Value> dimensionSize = GCValueProvider.nextDimensionSize(((ArrayType) type).numDimensions);
            block = new ModifyProxyBlock(new ModifyRefBlock(), targetNumerable, dimensionSize);
        } else {
            block = new ModifyProxyBlock(new ModifyRefBlock(), targetNumerable);
        }
        block.setInserationTarget(insertTarget);
        return block;
    }
}
