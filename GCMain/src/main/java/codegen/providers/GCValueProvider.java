package codegen.providers;

import codegen.blocks.ClassInfo;
import codegen.blocks.add.LocalArrayBlock;
import codegen.blocks.add.LocalRefBlock;
import codegen.utils.SootHelper;
import config.FuzzingConfig;
import soot.*;
import soot.jimple.IntConstant;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.toolkits.scalar.FlowSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GCValueProvider {
    public static List<Integer> arraySize = new ArrayList<>(Arrays.asList(8, 16, 32, 64, 128, 256, 512));
    public static List<Integer> objSize = new ArrayList<>(Arrays.asList(1, 2, 4, 8, 16, 32, 64, 128, 256, 512));
    public static List<Integer> objSizeUnit = new ArrayList<>(Arrays.asList(1, 1, 1, 1, 1024, 1024, 1024, 1024 * 1024));

    public static final int MAX_OBJECT_DEEP = 3;
    public static final int MAX_ARRAY_DIMENSION = 3;

    public static final int MAX_MULTI_OBJECT_COUNT = 10;

    public static final int MAX_NULL_OBJECT_COUNT = 10;


    public static int nextArraySize() {
        return arraySize.get(FuzzingConfig.getDefaultRandom().nextInt(arraySize.size()));
    }

    public static int nextArrayDimension() {
        return FuzzingConfig.getDefaultRandom().nextInt(MAX_ARRAY_DIMENSION) + 1;
//        return MAX_ARRAY_DIMENSION;
    }


    public static int nextMultiRefCount() {
        return FuzzingConfig.nextChoice(MAX_MULTI_OBJECT_COUNT, FuzzingConfig.RandomType.OPERATOR_POOL);
    }

    public static int nextObjectSize() {
        return objSize.get(FuzzingConfig.nextChoice(objSize.size())) *
                objSizeUnit.get(FuzzingConfig.nextChoice(objSizeUnit.size()));
    }

    public static int nextObjectDeep() {
        return FuzzingConfig.nextChoice(MAX_OBJECT_DEEP - 1,FuzzingConfig.RandomType.OPERATOR_POOL) + 1;
    }

    public static List<Value> nextDimensionSize(int numDimen) {
        List<Value> list = new ArrayList<>();
        for (int i = 0; i < numDimen; i++) {
            list.add(IntConstant.v(nextArraySize()));
        }
        return list;
    }

    public static String nextString() {
        int length = Math.abs(PrimitiveValueProvider.nextInt()) + 1;
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < length; i++) {
            str.append(PrimitiveValueProvider.nextChar());
        }
        return str.toString();
    }

    public static List<Value> paramValue(List<Type> paramTypeList, List<Local> locals, List<Stmt> stmts, ClassInfo clazz, String methodSign, Unit targetUnit) {
        List<Value> params = new ArrayList<>();
        for (Type paramType : paramTypeList) {
            if (clazz != null && methodSign != null && targetUnit != null &&
                    (paramType instanceof ArrayType || paramType instanceof RefType)) {
                if (FuzzingConfig.flipCoin(70)) {
                    FlowSet<Local> availableAndNotDeathVars = GCElementsProvider.getAvailableVars(clazz, methodSign, targetUnit);
                    availableAndNotDeathVars.difference(GCElementsProvider.getDeathVars(clazz, methodSign, targetUnit));
                    List<Local> list = availableAndNotDeathVars.toList().stream()
                            .filter(local -> {
                                        if (paramType instanceof RefType && local.getType() instanceof RefType) {
                                            return SootHelper.isExtends(((RefType) local.getType()).getSootClass(), ((RefType) paramType).getSootClass());
                                        }
                                        return local.getType().equals(paramType);
                                    }
                            )
                            .collect(Collectors.toList());
                    if (!list.isEmpty()) {
                        params.add(list.get(FuzzingConfig.nextChoice(list.size())));
                        continue;
                    }
                }
            }
            if (paramType instanceof ArrayType) {
                List<Value> arraySize = new ArrayList<>();
                for (int i = 0; i < ((ArrayType) paramType).numDimensions; i++) {
                    arraySize.add(IntConstant.v(GCValueProvider.nextArraySize()));
                }
                LocalArrayBlock localArrayBlock = new LocalArrayBlock((ArrayType) paramType, arraySize);
                Value value = localArrayBlock.createValue(locals, stmts);
                if (value == null) {
                    return null;
                }
                params.add(value);
            } else if (paramType instanceof RefType) {
                if (((RefType) paramType).getClassName().equals("java.lang.String")) {
                    params.add(StringConstant.v(nextString()));
                } else {
                    Value value = new LocalRefBlock((RefType) paramType).createValue(locals, stmts, clazz, methodSign, targetUnit);
                    if (value == null) {
                        return null;
                    }
                    params.add(value);
                }
            } else {
                params.add(PrimitiveValueProvider.next(paramType));
            }
        }
        return params;
    }
}
