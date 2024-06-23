package codegen.operators;

import codegen.blocks.BasicBlock;
import codegen.blocks.ClassInfo;
import codegen.blocks.delete.MultiDeleteBlock;
import codegen.providers.GCElementsProvider;
import codegen.providers.GCValueProvider;
import config.FuzzingConfig;
import soot.Local;
import soot.NullType;
import soot.RefType;
import soot.jimple.Stmt;
import soot.toolkits.scalar.FlowSet;
import soot.util.Numberable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DeleteOperator extends Generic {
    private DeleteOperator() {
    }

    private static final class AopHolder {
        static final DeleteOperator aop = new DeleteOperator();
    }

    public static DeleteOperator getInstance() {
        return DeleteOperator.AopHolder.aop;
    }

    @Override
    public BasicBlock nextBlock(ClassInfo clazz, String methodSign, List<Stmt> targets) {
        List<Numberable> availableValues = new ArrayList<>();
        Stmt insertTarget = null;
        while (!targets.isEmpty()) {
            int index = FuzzingConfig.nextChoice(targets.size(),FuzzingConfig.RandomType.OPERATOR_POOL);
            insertTarget = targets.get(index);
            targets = targets.subList(index + 1, targets.size());
            FlowSet<Local> flowSet = GCElementsProvider.getDeathVars(clazz, methodSign, insertTarget);
            flowSet.intersection(GCElementsProvider.getAvailableVars(clazz, methodSign, insertTarget));
            availableValues = flowSet.toList()
                    .stream()
                    .filter(value -> {
                        if (value instanceof NullType) {
                            return false;
                        }
                        if (value instanceof RefType) {
                            return !((RefType) value).getClassName().equals("java.lang.Random");
                        }
                        return true;
                    })
                    .collect(Collectors.toList());
            if (!availableValues.isEmpty()) {
                break;
            }
        }
        if (availableValues.size() == 0) {
            return null;
        }
        int maxCount = Math.min(availableValues.size(), GCValueProvider.MAX_NULL_OBJECT_COUNT);
        int count = FuzzingConfig.nextChoice(maxCount - 1,FuzzingConfig.RandomType.OPERATOR_POOL) + 1;
        Collections.shuffle(availableValues);
        List<Numberable> numberables = availableValues.subList(0, count);
        MultiDeleteBlock block = new MultiDeleteBlock(numberables);
        block.setInserationTarget(insertTarget);
        return block;
    }
}
