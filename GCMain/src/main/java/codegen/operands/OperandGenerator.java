package codegen.operands;

import codegen.blocks.*;
import codegen.operators.Operator;
import codegen.providers.*;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.JArrayRef;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JIdentityStmt;
import soot.util.Numberable;
import config.FuzzingConfig;

import java.util.*;

public class OperandGenerator implements Operator, Operand {

    protected int maxArrayDimensions = FuzzingConfig.MAX_ARRAY_DIM;
    protected int maxArraySize = FuzzingConfig.MAX_ARRAY_SIZE_PERDIM;

    protected int maxArrayLength = FuzzingConfig.MAX_ARRAY_LENGTH; // the length as a one dimension array.

    protected static OperandGenerator vop;

    public static OperandGenerator getInstance() {
        if (vop == null) {
            vop = new OperandGenerator();
        }
        return vop;
    }

    /**
     * 创建下一个block，这里与nextVariable的区别在于，nextVariable是在其他Operator中创建相应类型的变量
     * nextBlock为随机创建一个变量
     *
     * @param clazz
     * @param methodSign
     * @return
     */
    @Override
    public StmtBlock nextBlock(ClassInfo clazz, String methodSign, List<Stmt> targets) {

        StmtBlock block = new StmtBlock();
        Type type = TypeProvider.anyType();
        // array type
        if (FuzzingConfig.randomUpTo(FuzzingConfig.PROB_ARRAY_VALUE)) {
            type = ArrayType.v(type, FuzzingConfig.nextChoice(maxArrayDimensions) + 1);
        }
        SootField newField = null;
        // global field
        if (FuzzingConfig.randomUpTo(FuzzingConfig.PROB_GLOBAL_FIELD) && !(type instanceof NullType)) {
            //create field
            //TODO instance field
            newField = new SootField(NameProvider.genFieldName(), type, ModifierProvider.nextFieldModifier() | Modifier.STATIC);
            clazz.getSootClass().addField(newField);
        }
        if (type instanceof ArrayType) {
            block = createArrayVars(clazz, methodSign, type, targets.get(0));
        } else if (PrimitiveValueProvider.isPrimitiveOrString(type)) {
            //primitive type
            block = createPrimVars(clazz, methodSign, type);
        } else if (type instanceof RefType) {
            //reference type
            //TODO state2 NULLPOINTEREXCEPTION
            block = createObjVars(clazz, methodSign, (RefType) type, targets.get(0));
        } else if (type instanceof NullType) {
            block = createNullVars(clazz, methodSign, type);
        }
        if (newField != null) {
            //创建赋值语句
            if (newField.isStatic()) {
                Local var = block.getLocalVars().get(block.getLocalVars().size() - 1);
                for (Local v : block.getLocalVars()) {
                    if (v.getType().equals(newField.getType())) {
                        var = v;
                    }
                }
                block.addStmt(Jimple.v().newAssignStmt(Jimple.v().newStaticFieldRef(newField.makeRef()), var));
            } else {
                //TODO add support for instance field
            }
        }
        block.setInserationTarget(targets.get(0));
        return block;
    }

    /**
     * 创建相应类型的变量
     *
     * @param clazz
     * @param methodSign
     * @param type
     * @return
     */
    @Override
    public StmtBlock nextVariable(ClassInfo clazz, String methodSign, Type type, Stmt target) {

        StmtBlock block = null;

        if (type instanceof ArrayType) {
            if (((ArrayType) type).baseType instanceof NullType) {
                type = TypeProvider.anyNonArrayType();
            }
            block = createArrayVars(clazz, methodSign, type, target);
        } else if (PrimitiveValueProvider.isPrimitiveOrString(type)) {
            //primitive type
            block = createPrimVars(clazz, methodSign, type);
        } else if (type instanceof RefType) {
            block = createObjVars(clazz, methodSign, (RefType) type, target);
        } else if (type instanceof NullType) {
            block = createNullVars(clazz, methodSign, type);
            //should not happen
        }

        block.setInserationTarget(target);
        return block;
    }

    /**
     * 创建原始类型的变量
     *
     * @param clazz
     * @param methodSign
     * @param type
     * @return
     */
    public StmtBlock createPrimVars(ClassInfo clazz, String methodSign, Type type) {

        StmtBlock block = new StmtBlock();
        Local local = Jimple.v().newLocal(NameProvider.genVarName(), type);
        Value value = PrimitiveValueProvider.next(type);
        AssignStmt assign = Jimple.v().newAssignStmt(local, value);
        block.addLocalVar(local);
        block.addStmt(assign);
        return block;
    }

    public StmtBlock createNullVars(ClassInfo clazz, String methodSign, Type type) {

        StmtBlock block = new StmtBlock();
        Local local = Jimple.v().newLocal(NameProvider.genVarName(), type);
        Value value = PrimitiveValueProvider.next(type);
        AssignStmt assign = Jimple.v().newAssignStmt(local, value);
        block.addLocalVar(local);
        block.addStmt(assign);
        return block;
    }

    /**
     * 创建数组变量
     *
     * @param clazz
     * @param methodSign
     * @param type
     * @return
     */
    public StmtBlock createArrayVars(ClassInfo clazz, String methodSign, Type type, Stmt targetUnit) {

        StmtBlock block = new StmtBlock();
        //对于传入的类型，如果类型为数组类型，则不需要做任何修改，否则要定义数组的维度
        if (!(type instanceof ArrayType)) {
            int dimension = FuzzingConfig.nextChoice(maxArrayDimensions) + 1; // [1, maxArrayDimensions]
            type = ArrayType.v(type, dimension);
        }
        //01 声明局部变量 type[] localVar;
        Local local = Jimple.v().newLocal(NameProvider.genVarName(), type);
        block.addLocalVar(local);
        if (((ArrayType) type).numDimensions == 1) {
            //创建一维数组
            createNewArray(clazz, methodSign, block, local, type, targetUnit);
        } else {
            //创建多维数组
            createNewMultiArray(clazz, methodSign, block, local, type, targetUnit);
        }
        return block;
    }

    /**
     * 创建 1维 数组
     *
     * @param clazz
     * @param methodSign
     * @param block
     * @param local
     * @param type
     */
    public void createNewArray(ClassInfo clazz, String methodSign, StmtBlock block, Local local, Type type, Stmt targetUnit) {

        //02 创建长度为1的一维数组
        int arraySize = FuzzingConfig.nextChoice(maxArraySize) + 1;
        NewArrayExpr newArrayExpr = Jimple.v().newNewArrayExpr(((ArrayType) type).baseType, IntConstant.v(arraySize));
        AssignStmt assignArray = Jimple.v().newAssignStmt(local, newArrayExpr);
        block.addStmt(assignArray);
        //03 对数组中的元素进行赋值 随机赋值。
        int round = FuzzingConfig.nextChoice(arraySize);


        for (int i = 0; i < round; i++) {
            int index = FuzzingConfig.nextChoice(arraySize);
            AssignStmt assign = Jimple.v().newAssignStmt(Jimple.v().newArrayRef(local, IntConstant.v(index)),
                    nextValue(clazz, methodSign, block, ((ArrayType) type).baseType, targetUnit));
            block.addStmt(assign);
        }
    }

    /**
     * 创建高维数组
     *
     * @param clazz
     * @param methodSign
     * @param block
     * @param local
     * @param type
     */
    public void createNewMultiArray(ClassInfo clazz, String methodSign, StmtBlock block, Local local, Type type, Stmt targetUnit) {
        int dimensions = ((ArrayType) type).numDimensions;
        int length = 1;
        List<Value> arraySize = new ArrayList<>();
        for (int i = 0; i < dimensions; i++) {
            int maxsize = length == 0 ? 20 : maxArrayLength / length;
            maxsize = maxsize <= 20 ? maxsize : 20;
            int size = FuzzingConfig.nextChoice(maxsize) + 1;
            length = size * length;
            arraySize.add(IntConstant.v(size));
        }
        NewMultiArrayExpr newMultiArrayExpr = Jimple.v().newNewMultiArrayExpr((ArrayType) type, arraySize);
        AssignStmt assignArray = Jimple.v().newAssignStmt(local, newMultiArrayExpr);
        block.addStmt(assignArray);
        //03 对数组中的元素进行赋值 随机赋值。
        int round = FuzzingConfig.nextChoice(arraySize.get(arraySize.size() - 1).hashCode());
        for (int i = 0; i < round; i++) {

            int index = FuzzingConfig.nextChoice(arraySize.get(arraySize.size() - 1).hashCode());
            Local localRef = splitMultiArray(block, local, arraySize);
            AssignStmt assign = Jimple.v().newAssignStmt(Jimple.v().newArrayRef(localRef, IntConstant.v(index)),
                    nextValue(clazz, methodSign, block, ((ArrayType) type).baseType, targetUnit));
            block.addStmt(assign);
        }
    }

    public Local splitMultiArray(BasicBlock block, Local local, List<Value> arraySize) {

        Local lastArrayRef = null;
        for (int i = 0; i < arraySize.size() - 1; i++) {

            int index = FuzzingConfig.nextChoice(arraySize.get(i).hashCode());
            JArrayRef arrayRef = (JArrayRef) Jimple.v().newArrayRef(local, IntConstant.v(index));
            Local localRef = Jimple.v().newLocal(NameProvider.genVarName(), arrayRef.getType());
            lastArrayRef = localRef;
            block.addLocalVar(localRef);
            JAssignStmt assign = (JAssignStmt) Jimple.v().newAssignStmt(localRef, arrayRef);
            block.addStmt(assign);
            //CHECK 这里是不是想剥离出一个一维数组。
            local = localRef;
        }
        return lastArrayRef;
    }

    /**
     * @param clazz
     * @param methodSign
     * @param type
     * @return
     */
    //新建一个对象, 对象默认为空
    public StmtBlock createObjVars(ClassInfo clazz, String methodSign, RefType type, Stmt targetUnit) {

        StmtBlock block = new StmtBlock();

        Local local = Jimple.v().newLocal(NameProvider.genVarName(), type);
        //如果不合法就直接返回一个值为null的对象
        StmtBlock nullBlock = new StmtBlock();
        nullBlock.addLocalVar(local);
        nullBlock.addStmt(
                Jimple.v().newAssignStmt(local, Jimple.v().newCastExpr(NullConstant.v(), type))
        );

        if (type.getSootClass().isAbstract()) return nullBlock;

        List<SootMethod> constructs = new ArrayList<>();
        for (SootMethod method : type.getSootClass().getMethods()) {
            if (method.isPublic() && method.isConstructor()) {
                constructs.add(method);
            }
        }

        //搜索Method
        if (constructs.isEmpty()) return nullBlock;
        SootMethod construct = constructs.get(FuzzingConfig.nextChoice(constructs.size()));
        //System.out.println(construct.getDeclaration() + " " + construct);
        //new 变量
        AssignStmt init = Jimple.v().newAssignStmt(local, Jimple.v().newNewExpr(type));
        //调用构造函数
        StmtBlock invokeBlock = funcInvocation(clazz, methodSign, local, construct, targetUnit);
        if (invokeBlock.getStmts().isEmpty()) return nullBlock;

        block.addAllLocalVars(invokeBlock.getLocalVars());
        block.addAllReusedVars(invokeBlock.getReusedVars());
        block.addLocalVar(local);
        block.addAllStmts(invokeBlock.getStmts());
        block.getStmts().add(block.getStmts().size() - 1, init);
        return block;
    }

    /**
     * 向clazz类中添加一个新的field
     * filed是public的。并有50%概率是静态的。
     *
     * @param clazz 类
     */
    public void addClassField(ClassInfo clazz) {

        StmtBlock block = new StmtBlock();

        Type type = TypeProvider.anyType();
        // array type
        if (FuzzingConfig.randomUpTo(FuzzingConfig.PROB_ARRAY_VALUE)) {
            type = ArrayType.v(type, FuzzingConfig.nextChoice(maxArrayDimensions) + 1);
        }
        // global field
        SootField newField = new SootField(NameProvider.genFieldName(), type, ModifierProvider.nextFieldModifier());
        clazz.getSootClass().addField(newField);

        SootMethod sootMethod = null;
        if (newField.isStatic()) {
            for (SootMethod method : clazz.getSootClass().getMethods()) {
                if (method.isStaticInitializer()) {
                    sootMethod = method;
                }
            }
        } else {
            for (SootMethod method : clazz.getSootClass().getMethods()) {
                if (method.isConstructor()) {
                    sootMethod = method;
                }
            }
        }
        if (sootMethod == null) {
            return;
        }
        Optional<Unit> units = sootMethod.retrieveActiveBody().getUnits().stream().filter(unit -> !(unit instanceof JIdentityStmt)).findFirst();
        Stmt targetUnit = (Stmt) units.get();

        if (type instanceof ArrayType) {
            block = createArrayVars(clazz, sootMethod.getSignature(), type, targetUnit);
        } else if (PrimitiveValueProvider.isPrimitiveOrString(type)) {
            //primitive type
            block = createPrimVars(clazz, sootMethod.getSignature(), type);
        } else if (type instanceof RefType) {
            //reference type
            block = createObjVars(clazz, sootMethod.getSignature(), (RefType) type, targetUnit);
        } else if (type instanceof NullType) {
            block = createNullVars(clazz, sootMethod.getSignature(), type);
        }
        if (newField != null) {
            //创建赋值语句
            Local var = block.getLocalVars().get(block.getLocalVars().size() - 1);
            for (Local v : block.getLocalVars()) {
                if (v.getType().equals(newField.getType())) {
                    var = v;
                }
            }
            if (newField.isStatic()) {
                block.addStmt(Jimple.v().newAssignStmt(Jimple.v().newStaticFieldRef(newField.makeRef()), var));
            } else {
                block.addStmt(Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(sootMethod.getActiveBody().getThisLocal(), newField.makeRef()), var));
            }
        }
        sootMethod.retrieveActiveBody().getLocals().addAll(block.getLocalVars());
        sootMethod.retrieveActiveBody().getUnits().insertBefore(block.getStmts(), targetUnit);
    }

    /**
     * 对数组中的某个索引进行赋值时，为其生成对应的值
     * （1）如果需要的类型为原始类型，则直接插入一个随机的值
     * （2）如果需要的类型为引用类型，则创建相应的局部变量
     *
     * @param clazz
     * @param methodSign
     * @param block
     * @param type
     * @return
     */
    public Value nextValue(ClassInfo clazz, String methodSign, StmtBlock block, Type type, Stmt target) {

        //create new Variable
        if (PrimitiveValueProvider.isPrimitiveOrString(type)) {
            //primitive type
            return PrimitiveValueProvider.next(type);
        } else {
            //reference type
            //创建新的变量
            StmtBlock newBlock = OperandGenerator.getInstance().nextVariable(clazz, methodSign, type, target);
//            Generator.nextVariable(clazz, methodSign, type, target);
            if (newBlock.getLocalVars().isEmpty()) return NullConstant.v();
            block.addAllLocalVars(newBlock.getLocalVars());
            block.addAllReusedVars(newBlock.getReusedVars());
            block.addAllStmts(newBlock.getStmts());
            for (Local var : newBlock.getLocalVars()) {
                if (var.getType().equals(type)) {
                    return var;
                }
            }
            return newBlock.getLocalVars().get(0);
        }
    }

    /**
     * 函数调用
     *
     * @param clazz
     * @param methodSign
     * @param invoker
     * @param invokeFunc
     * @param targetUnit
     * @return
     */
    public StmtBlock funcInvocation(ClassInfo clazz, String methodSign, Local invoker, SootMethod invokeFunc, Stmt targetUnit) {

        HashMap<Type, ArrayList<Numberable>> reused = new HashMap<>();

        Local This = null;
        if (!clazz.getMethodMaps().get(methodSign).isStatic()) This = clazz.getThisRef(methodSign);
        //如果可以使用局部变量，搜索所有可以使用的全局变量及其fields。
        if (targetUnit != null && FuzzingConfig.randomUpTo(FuzzingConfig.PROB_REUSE_VAR)) {
            List<Numberable> values = ElementsProvider.getAvailableValues(clazz, methodSign, targetUnit);
            for (Numberable value : values) {
                Type type = null;
                if (value instanceof Local) type = ((Local) value).getType();
                else if (value instanceof SootField) ((SootField) value).getType();
                else continue;
                ArrayList<Numberable> list = reused.getOrDefault(type, new ArrayList<>());
                if (list.isEmpty()) reused.put(type, list);
                list.add(value);
            }
        }

        List<Value> args = new ArrayList<>();
        StmtBlock block = new StmtBlock();

        //生成每个参数
        //依次考虑，使用局部变量和This，使用fields， 使用null， 用构造函数生成。
        for (Type type : invokeFunc.getParameterTypes()) {
            ArrayList<Numberable> list = reused.getOrDefault(type, new ArrayList<>());
            if (!list.isEmpty()) {
                Numberable var = list.get(FuzzingConfig.nextChoice(list.size()));
                Local arg;
                if (var instanceof SootField) {
                    arg = Jimple.v().newLocal(NameProvider.genVarName(), type);
                    block.addLocalVar(arg);
                    if (((SootField) var).isStatic()) {
                        FieldRef ref = Jimple.v().newStaticFieldRef(((SootField) var).makeRef());
                        block.addStmt(Jimple.v().newAssignStmt(arg, ref));
                    } else {
                        FieldRef ref = Jimple.v().newInstanceFieldRef(This, ((SootField) var).makeRef());
                        block.addStmt(Jimple.v().newAssignStmt(arg, ref));
                    }
                    args.add(arg);
                    continue;
                } else if (var instanceof Local) {
                    arg = (Local) var;
                    block.addReusedVar(arg);
                    args.add(arg);
                    continue;
                }
            }

            if (type instanceof RefType && FuzzingConfig.randomUpTo(FuzzingConfig.PROB_NULL_VALUE)) {
                Local var = Jimple.v().newLocal(NameProvider.genVarName(), type);
                AssignStmt assign = Jimple.v().newAssignStmt(var, Jimple.v().newCastExpr(NullConstant.v(), type));
                args.add(var);
                block.addStmt(assign);
                block.addLocalVar(var);
                continue;
            }

            try {
                StmtBlock sub = nextVariable(clazz, methodSign, type, targetUnit);
                if (sub.getLocalVars().isEmpty()) return new StmtBlock();
                for (Local var : sub.getLocalVars()) {
                    if (var.getType().equals(type)) {
                        args.add(var);
                        break;
                    }
                }

                block.addAllStmts(sub.getStmts());
                block.addAllLocalVars(sub.getLocalVars());
                for (Local reuse : sub.getReusedVars()) {
                    block.addReusedVar(reuse);
                }
            } catch (Exception e) {
                System.out.println("Construct failed: " + type);
                return new StmtBlock();
            }
        }

        //根据函数，选择对应的调用语句。
        Stmt invoke;
        SootMethodRef ref = invokeFunc.makeRef();
        if (invokeFunc.getReturnType() instanceof VoidType) {
            if (invokeFunc.isConstructor()) {
                invoke = Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(invoker, ref, args));
            } else if (invokeFunc.isStatic()) {
                invoke = Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(ref, args));
            } else if (ref.getDeclaringClass().isInterface()) {
                invoke = Jimple.v().newInvokeStmt(Jimple.v().newInterfaceInvokeExpr(invoker, ref, args));
            } else {
                invoke = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(invoker, ref, args));
            }
        } else {
            //如果有返回值，需要保存返回值。
            Local ret = Jimple.v().newLocal(NameProvider.genVarName(), invokeFunc.getReturnType());
            if (invokeFunc.isConstructor()) {
                invoke = Jimple.v().newAssignStmt(ret, Jimple.v().newSpecialInvokeExpr(invoker, ref, args));
            } else if (invokeFunc.isStatic()) {
                invoke = Jimple.v().newAssignStmt(ret, Jimple.v().newStaticInvokeExpr(ref, args));
            } else if (ref.getDeclaringClass().isInterface()) {
                invoke = Jimple.v().newAssignStmt(ret, Jimple.v().newInterfaceInvokeExpr(invoker, ref, args));
            } else {
                invoke = Jimple.v().newAssignStmt(ret, Jimple.v().newVirtualInvokeExpr(invoker, ref, args));
            }
            block.addLocalVar(ret);
        }

        block.addStmt(invoke);
        return block;

    }
}
