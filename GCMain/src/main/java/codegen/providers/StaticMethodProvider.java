package codegen.providers;

import config.FuzzingConfig;
import soot.*;

import java.util.ArrayList;

public class StaticMethodProvider {
    public static ArrayList<SootMethod> staticMethods = new ArrayList<>();
    public static ArrayList<SootMethod> safeStaticMethods = new ArrayList<>();//不throw异常

    static {

        for (Type type: TypeProvider.refTypes) {
            assert(type instanceof RefType);
            SootClass cls = ((RefType) type).getSootClass();
            for (SootMethod method: cls.getMethods()) {
                if (method.isPublic() && method.isStatic() && !method.isStaticInitializer()) {
                    staticMethods.add(method);
                    if (method.getExceptions().isEmpty()) {
                        safeStaticMethods.add(method);
                    }
                }
            }
        }
    }

    public static void loadStaticMethods() {
        if (!staticMethods.isEmpty()) {
            for (Type type: TypeProvider.refTypes) {
                assert(type instanceof RefType);
                SootClass cls = ((RefType) type).getSootClass();
                for (SootMethod method: cls.getMethods()) {
                    if (method.isPublic() && method.isStatic() && !method.isStaticInitializer()) {
                        staticMethods.add(method);
                        if (method.getExceptions().isEmpty()) {
                            safeStaticMethods.add(method);
                        }
                    }
                }
            }
        }
    }

    public static ArrayList<SootMethod> getStaticMethods() {
        return  staticMethods;
    }

    public static ArrayList<SootMethod> getSafeStaticMethods() {
        return safeStaticMethods;
    }

    public static SootMethod getStaticMethod() {
        return staticMethods.get(FuzzingConfig.nextChoice(staticMethods.size()));
    }

    public static SootMethod getSafeStaticMethod() {
        return safeStaticMethods.get(FuzzingConfig.nextChoice(safeStaticMethods.size()));
    }

}
