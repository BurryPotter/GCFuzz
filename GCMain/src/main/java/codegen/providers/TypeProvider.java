package codegen.providers;

import config.FuzzingConfig;
import soot.*;

import java.util.ArrayList;
import java.util.HashMap;

public class TypeProvider {

    /**
     * Primitive type
     */
    public static IntType intType = IntType.v();
    public static BooleanType booleanType = BooleanType.v();
    public static FloatType floatType = FloatType.v();
    public static DoubleType doubleType = DoubleType.v();
    public static LongType longType = LongType.v();
    public static CharType charType = CharType.v();
    public static ShortType shortType = ShortType.v();

    public static RefType refType = RefType.v();

    public static NullType nullType = NullType.v();
    /**
     * Array type
     */
    public static ArrayType arrayType = ArrayType.v(NullType.v(), 1);

    public static ArrayList<Type> primTypes = new ArrayList<>();
    public static ArrayList<Type> refTypes = new ArrayList<>();
    public static HashMap<Type, Integer> types = new HashMap<>();

    static {

        primTypes.add(intType);
        primTypes.add(booleanType);
        primTypes.add(floatType);
        primTypes.add(doubleType);
        primTypes.add(longType);
        primTypes.add(charType);
        primTypes.add(shortType);
        primTypes.add(nullType);

        for (SootClass clazz: Scene.v().getClasses()) {
            if (clazz.isAbstract()) continue;
            if (!clazz.isPublic()) continue;
            if (FuzzingConfig.INVALID_REFERENCE_TYPE.contains(clazz.getName()) || !clazz.getName().startsWith("java.")) continue;
            for (SootMethod method : clazz.getMethods()) {
                if (method.isPublic() && method.isConstructor()) {
                    refTypes.add(clazz.getType());
                    break;
                }
            }
        }

        types.put(intType, FuzzingConfig.PROB_INT_VALUE);
        types.put(booleanType, FuzzingConfig.PROB_BOOL_VALUE);
        types.put(floatType, FuzzingConfig.PROB_FLOAT_VALUE);
        types.put(doubleType, FuzzingConfig.PROB_DOUBLE_VALUE);
        types.put(longType, FuzzingConfig.PROB_LONG_VALUE);
        types.put(charType, FuzzingConfig.PROB_CHAR_VALUE);
        types.put(shortType, FuzzingConfig.PROB_SHORT_VALUE);
        types.put(refType, FuzzingConfig.PROB_OBJECT_VALUE);
    }

    public static void loadRefTypes() {
        //need add manually
        if (!refTypes.isEmpty()) return;
        for (SootClass clazz: Scene.v().getClasses()) {
            if (clazz.isAbstract()) continue;
            if (!clazz.isPublic()) continue;
            if (FuzzingConfig.INVALID_REFERENCE_TYPE.contains(clazz.getName()) || !clazz.getName().startsWith("java.")) continue;
            for (SootMethod method : clazz.getMethods()) {
                if (method.isPublic() && method.isConstructor()) {
                    refTypes.add(clazz.getType());
                    break;
                }
            }
        }
    }


    public static Type anyType() {
        Type candidate = FuzzingConfig.randomUpTo(types);
        if (candidate instanceof RefType){
            candidate = refTypes.get(FuzzingConfig.nextChoice(refTypes.size()));
        }
        return candidate;
    }

    public static Type anyNonArrayType() {
        Type candidate = FuzzingConfig.randomUpTo(types);
        if (candidate instanceof RefType){
            candidate = refTypes.get(FuzzingConfig.nextChoice(refTypes.size()));
        }
        return candidate;
    }

    public static Type anyRefType() {
        if (refTypes.size() == 0) {
            reloadRef();
        }
        return refTypes.get(FuzzingConfig.nextChoice(refTypes.size()));
    }

    public static Type anyPrimType() {
        Type type = FuzzingConfig.randomUpTo(types);
        while (type instanceof RefType) type = FuzzingConfig.randomUpTo(types);
        return type;
    }

    public static void reloadRef(){
        for (SootClass clazz: Scene.v().getClasses()) {
            if (clazz.isAbstract()) continue;
            if (!clazz.isPublic()) continue;
            if (FuzzingConfig.INVALID_REFERENCE_TYPE.contains(clazz.getName()) || !clazz.getName().startsWith("java.")) continue;
            for (SootMethod method : clazz.getMethods()) {
                if (method.isPublic() && method.isConstructor()) {
                    refTypes.add(clazz.getType());
                    break;
                }
            }
        }
    }
}
