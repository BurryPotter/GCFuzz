package codegen.operators.addoperators;

import codegen.blocks.*;
import codegen.blocks.add.AddFieldBlock;
import codegen.blocks.add.DeepRefBlock;
import codegen.blocks.add.LocalArrayBlock;
import codegen.blocks.add.LocalRefBlock;
import codegen.operators.Generic;
import codegen.providers.*;
import codegen.providers.GCTypeProvider;
import codegen.providers.GCValueProvider;
import soot.*;
import soot.jimple.*;

import java.util.ArrayList;
import java.util.List;

public class AddFieldOperator extends Generic {

    protected static AddFieldOperator aop;

    private AddFieldOperator() {
    }

    public static AddFieldOperator getInstance() {
        if (aop == null) {
            aop = new AddFieldOperator();
        }
        return aop;
    }

    @Override
    public BasicBlock nextBlock(ClassInfo clazz, String methodSign, List<Stmt> targets) {
        AddFieldBlock block = null;
        String fieldName = NameProvider.genFieldName();
        Type fieldType = GCTypeProvider.gcType();
        SootField field = new SootField(fieldName, fieldType, ModifierProvider.nextFieldModifier());
        if (fieldType instanceof ArrayType) {
            List<Value> l = new ArrayList<>();
            for (int i = 0; i < ((ArrayType) fieldType).numDimensions; i++) {
                l.add(IntConstant.v(GCValueProvider.nextArraySize()));
            }
            LocalArrayBlock arrayBlock = new LocalArrayBlock((ArrayType) fieldType, l);
            block = new AddFieldBlock(field, arrayBlock);
        } else if (fieldType instanceof RefType) {
            LocalRefBlock refBlock = new LocalRefBlock((RefType) fieldType);
            block = new AddFieldBlock(field, refBlock);
        }
        return block;
    }


}
