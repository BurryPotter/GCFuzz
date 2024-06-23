package codegen.providers;

import config.FuzzingConfig;
import soot.*;
import utils.ClassUtils;

import java.util.ArrayList;

public class GCTypeProvider extends TypeProvider {
    public static int exceptionHandlerNum = 0;

    public static ArrayList<RefType> gcRelaTypes = new ArrayList<>();
    private static final String[] GC_RELA_CLASS = new String[]{
            "java.util.ArrayList", "java.util.LinkedList",
            "java.lang.ref.PhantomReference","java.lang.ref.SoftReference", "java.lang.ref.WeakReference"
            , "java.lang.String"};
    // TODO
    public static ArrayList<RefType> gcRelaExceptions = new ArrayList<>();

    private static final String[] GC_RELA_EXCEPTION = new String[]{
            "java.lang.OutOfMemoryError"
    };

    static {
        for (String name : GC_RELA_CLASS) {
            SootClass sootClass = ClassUtils.loadClass(name);
            if (sootClass != null) {
                gcRelaTypes.add(sootClass.getType());
            }
        }
        for (String name : GC_RELA_EXCEPTION) {
            SootClass sootClass = ClassUtils.loadClass(name);
            if (sootClass != null) {
                gcRelaExceptions.add(sootClass.getType());
            }
        }
    }

    public static Type gcType() {
        boolean f = FuzzingConfig.flipCoin();
        Type type;
        if (f) {
            type = arrayType();
        } else {
            type = gcRefType();
        }
        return type;
    }

    public static ArrayType arrayType() {
        int random = FuzzingConfig.getDefaultRandom().nextInt(3);
        ArrayType arrayType = null;
        switch (random) {
            case 0:
                arrayType = primArrayType();
                break;
            case 1:
                arrayType = refArrayType();
                break;
            case 2:
                arrayType = gcRefArrayType();
                break;
        }
        return arrayType;
    }

    public static ArrayType primArrayType() {
        Type type = anyPrimType();
        int dimension = GCValueProvider.nextArrayDimension();
        return ArrayType.v(type, dimension);
    }

    public static ArrayType refArrayType() {
        loadRefTypes();
        Type type = anyRefType();
        int dimension = GCValueProvider.nextArrayDimension();
        return ArrayType.v(type, dimension);
    }

    public static ArrayType gcRefArrayType() {
        Type type = gcRefType();
        int dimension = GCValueProvider.nextArrayDimension();
        return ArrayType.v(type, dimension);
    }

    public static RefType gcRefType() {
        boolean gcRelated = FuzzingConfig.flipCoin(80);
        if (gcRelated) {
            return gcRelaTypes.get(FuzzingConfig.nextChoice(gcRelaTypes.size()));
        } else {
            return (RefType) TypeProvider.anyRefType();
        }
    }

    public static RefType gcExceptionType() {
        return gcRelaExceptions.get(FuzzingConfig.nextChoice(gcRelaExceptions.size()));
    }
    public static String genHandleException(){
        return "ExceptionHandler_" + exceptionHandlerNum++;
    }
}
