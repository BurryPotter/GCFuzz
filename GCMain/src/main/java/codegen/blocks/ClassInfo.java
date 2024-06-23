package codegen.blocks;

import soot.*;
import soot.util.Numberable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 保存类信息
 */
public class ClassInfo {

    protected String className;
    protected SootClass sootClass;
    protected HashMap<String, SootMethod> methodMaps;

    protected ArrayList<SootMethod> staticMethods; // self define static method
    protected ArrayList<SootMethod> methods; // self define method

    public ClassInfo(SootClass sootClass) {

        this.className = sootClass.getName();
        this.sootClass = sootClass;
        this.methodMaps = new HashMap<>();
        this.methods = new ArrayList<>();
        this.staticMethods = new ArrayList<>();
        for (SootMethod method : sootClass.getMethods()) {
            try {
                this.methodMaps.put(method.getSignature(), method);
            } catch (Exception e){
                //do nothing
            }
        }
    }

    public ClassInfo(String className, SootClass sootClass, HashMap<String, SootMethod> methodMaps) {

        this.className = className;
        this.sootClass = sootClass;
        this.methodMaps = methodMaps;
        this.methods = new ArrayList<>();
        this.staticMethods = new ArrayList<>();
    }

    public static ClassInfo analyseClass(SootClass clazz){
        return new ClassInfo(clazz);
    }

    public ArrayList<Numberable> typeFilter(String methodSign, Type type) {

        ArrayList<Numberable> vars = new ArrayList<>();
        vars.addAll(fieldTypeFilter(type));
        vars.addAll(localTypeFilter(methodSign, type));
        return vars;
    }

    public ArrayList<SootField> fieldTypeFilter(Type type) {

        ArrayList<SootField> vars = new ArrayList<>();
        if (type instanceof RefType) {

            for (SootField field : sootClass.getFields()) {
                if (field.getName().equals(((RefType) type).getClassName())) {
                    vars.add(field);
                }
            }
        } else {

            for (SootField field : sootClass.getFields()) {
                if (field.getType() == type) {
                    vars.add(field);
                }
            }
        }
        return vars;
    }

    public ArrayList<Local> localTypeFilter(String methodSign, Type type) {

        ArrayList<Local> vars = new ArrayList<>();
        if (type instanceof RefType) {

            for (Local local : methodMaps.get(methodSign).getActiveBody().getLocals()) {
                if (local.getType() instanceof RefType
                        && ((RefType) local.getType()).getClassName().equals(((RefType) type).getClassName())) {
                    vars.add(local);
                }
            }
        } else {
            for (Local local : methodMaps.get(methodSign).getActiveBody().getLocals()) {
                if (local.getType() == type) {
                    vars.add(local);
                }
            }
        }
        return vars;
    }

    public Local getThisRef(String methodSign) {

        SootMethod targetMethod = methodMaps.get(methodSign);
        Body body = targetMethod.getActiveBody();
        return body.getThisLocal();
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public SootClass getSootClass() {
        return sootClass;
    }

    public void setSootClass(SootClass sootClass) {
        this.sootClass = sootClass;
    }

    public HashMap<String, SootMethod> getMethodMaps() {
        return methodMaps;
    }

    public void setMethodMaps(HashMap<String, SootMethod> methodMaps) {
        this.methodMaps = methodMaps;
    }

    public void addMethod(SootMethod method) {
        this.methodMaps.put(method.getSignature(), method);
        if (method.isStatic()) staticMethods.add(method);
        methods.add(method);
    }

    public ArrayList<SootMethod> getStaticMethods() {
        return staticMethods;
    }

    public ArrayList<SootMethod> getMethods() {
        return methods;
    }
}
