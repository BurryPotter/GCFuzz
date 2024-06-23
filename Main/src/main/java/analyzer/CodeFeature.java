package analyzer;

import fj.test.Bool;
import org.junit.Test;
import soot.*;
import soot.dava.internal.AST.ASTIfNode;
import soot.jimple.*;
import soot.jimple.internal.*;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.options.Options;
import soot.toolkits.graph.LoopNestTree;
import utils.ClassUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class CodeFeature {
    public static int[] code = new int[SourceCodeFeature.values().length];

    @Test
    public void debug() {
        String cp = "D:\\projects\\GCGenerator\\GCFuzzing\\Main\\src\\main\\java";
        String fileName = "Case";
        ClassUtils.initSootEnvWithClassPath(cp);
        Options.v().set_src_prec(Options.src_prec_java);
        SootClass c = ClassUtils.loadClass(fileName);
        extract(c);
    }

    public static void main(String[] args) throws IOException {

        String[] paths = new String[]{
//                "D:\\projects\\GCGenerator\\historyCases\\hotspot_gc",
                "D:\\projects\\GCGenerator\\historyCases\\filter\\gc",
//                "D:\\projects\\GCGenerator\\historyCases\\filter\\runtime",
//                "D:\\projects\\GCGenerator\\historyCases\\filter\\compiler",
        };
        for (String path : paths) {
            extract(path);
        }

    }

    public static void extract(String path) throws IOException {
        File rootDir = new File(path);
        FileFinder.files.clear();
        FileFinder.loadFiles(rootDir, ".jimple");
        LinkedList<FileInfo> fileInfoList = new LinkedList<>();
        for (File javaFile : FileFinder.files) {
            try {
                initCodeFeature();
                System.out.println(javaFile.getAbsolutePath());
                String cp = javaFile.getParentFile().getAbsolutePath();
                String fileName = javaFile.getName();
                fileName = fileName.replace(".jimple", "");
                ClassUtils.initSootEnvWithClassPath(cp);
                SootClass sootClass = ClassUtils.loadClass(fileName);
                if (sootClass == null) {
                    System.out.println("sootClass load error");
                    continue;
                }
                extract(sootClass);
                FileInfo fileInfo = new FileInfo(javaFile.getName(), javaFile.getAbsolutePath());
                ArrayList<CodeFeatureBean> codeFeatureBeans = new ArrayList<>();
                for (int i = 0; i < code.length; i++) {
                    codeFeatureBeans.add(new CodeFeatureBean(SourceCodeFeature.values()[i], code[i]));
                }
                codeFeatureBeans.sort(Comparator.comparing(CodeFeatureBean::getFeature));
                for (CodeFeatureBean codeFeatureBean : codeFeatureBeans) {
                    fileInfo.addFeature(codeFeatureBean);
                }
                fileInfoList.add(fileInfo);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        writeCSV(fileInfoList, path + ".csv");
    }

    public static void writeCSV(LinkedList<FileInfo> infoList, String path) throws IOException {
        File writeFile = new File(path);
        BufferedWriter writer = new BufferedWriter(new FileWriter(writeFile));
        // write header
        if (!infoList.isEmpty()) {
            FileInfo i = infoList.get(0);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("fileName")
                    .append(",")
                    .append("filePath");
            for (CodeFeatureBean feature : i.features) {
                stringBuilder.append(",")
                        .append(feature.feature);
            }
            writer.write(stringBuilder.toString());
            writer.newLine();
        }
        // write content
        for (FileInfo fileInfo : infoList) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(fileInfo.fileName)
                    .append(",")
                    .append(fileInfo.filePath);
            for (CodeFeatureBean feature : fileInfo.features) {
                stringBuilder.append(",")
                        .append(feature.count);
            }
            writer.write(stringBuilder.toString());
            writer.newLine();
        }
        writer.close();
    }

    public static void extract(SootClass sootClass) {
        for (SootMethod method : sootClass.getMethods()) {
            try {
                extract(method);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    public static void initCodeFeature() {
        Arrays.fill(code, 0);
    }

    public static void extract(SootMethod method) {
        method.retrieveActiveBody();
        Body body = method.getActiveBody();
        LoopNestTree loopNestTree = new LoopNestTree(body);
        code[SourceCodeFeature.loopDepthOne.ordinal()] += loopNestTree.size();
        code[SourceCodeFeature.tryStmt.ordinal()] += body.getTraps().size();
        int loopIfCount = 0;
        HashSet<IfStmt> ifStmts = new HashSet<>();
        for (Loop loop : loopNestTree) {
            for (Stmt loopStatement : loop.getLoopStatements()) {
                if (loopStatement instanceof JIfStmt) {
                    if (ifStmts.contains(loopStatement)) {
                        break;
                    }
                    ifStmts.add((JIfStmt) loopStatement);
                    loopIfCount++; // loop condition may be presented as IfStatement
                    break;
                }
            }
        }
        code[SourceCodeFeature.ifStmt.ordinal()] -= loopIfCount;
        Set<Local> declLocal = new HashSet<>();
        for (Local local : method.getActiveBody().getLocals()) {
            Type localType = local.getType();
            localType(localType);
        }
        for (Unit unit : body.getUnits()) {
            if (unit instanceof JAssignStmt) {
                JAssignStmt stmt = (JAssignStmt) unit;
                Value rightValue = stmt.getRightOp();
                Value leftValue = stmt.getLeftOp();
                if (leftValue instanceof JArrayRef) {
                    code[SourceCodeFeature.modifyArray.ordinal()] += 1;
                } else if (leftValue instanceof JInstanceFieldRef) {
                    Type type = ((JInstanceFieldRef) leftValue).getBase().getType();
                    modifyType(type);
                } else if (rightValue instanceof JNewExpr) {
                    Type type = rightValue.getType();
                    if ((leftValue instanceof JimpleLocal && declLocal.contains(leftValue))) {
                        modifyType(type);
                    } else {
                        newType(type);
                    }
                } else if (rightValue instanceof JNewArrayExpr || rightValue instanceof JNewMultiArrayExpr) {
                    code[SourceCodeFeature.newArray.ordinal()] += 1;
                } else if (rightValue instanceof NullConstant) {
                    Type type = leftValue.getType();
                    nullType(type);
                } else if (rightValue instanceof JimpleLocal) {
                    Type type = rightValue.getType();
                    modifyType(type);
                } else if (rightValue instanceof JInvokeStmt) {

                    String methodName = ((JInvokeStmt) rightValue).getInvokeExpr().getMethod().getName();
                    if (methodName.equals("println") || methodName.equals("print") || methodName.equals("<init>")) {
                        continue;
                    }
                    if (((JInvokeStmt) rightValue).getInvokeExpr() instanceof JVirtualInvokeExpr) {
                        JVirtualInvokeExpr invokeExpr = (JVirtualInvokeExpr) ((JInvokeStmt) rightValue).getInvokeExpr();
                        Value base = invokeExpr.getBase();
                        invokeModifyType(base.getType());
                    } else {
                        code[SourceCodeFeature.invocationStmt.ordinal()]++;
                    }
                }
                Type leftType = leftValue.getType();
                if (leftType instanceof PrimType) {
                    if (rightValue instanceof BinopExpr) {
                        if (rightValue instanceof AddExpr || rightValue instanceof SubExpr ||
                                rightValue instanceof MulExpr || rightValue instanceof DivExpr || rightValue instanceof RemExpr) {
                            code[SourceCodeFeature.arithmeticStmt.ordinal()] += 1;
                        } else if (rightValue instanceof ShlExpr || rightValue instanceof ShrExpr || rightValue instanceof UshrExpr) {
                            code[SourceCodeFeature.shiftStmt.ordinal()] += 1;

                        } else {
                            code[SourceCodeFeature.logicalStmt.ordinal()] += 1;

                        }
                    } else if (rightValue instanceof UnopExpr) {
                        code[SourceCodeFeature.unopStmt.ordinal()] += 1;
                    }
                }
            } else if (unit instanceof JInvokeStmt) {
                String methodName = ((JInvokeStmt) unit).getInvokeExpr().getMethod().getName();
                if (methodName.equals("println") || methodName.equals("print") || methodName.equals("<init>")) {
                    continue;
                }
                if (((JInvokeStmt) unit).getInvokeExpr() instanceof JVirtualInvokeExpr) {
                    JVirtualInvokeExpr invokeExpr = (JVirtualInvokeExpr) ((JInvokeStmt) unit).getInvokeExpr();
                    Value base = invokeExpr.getBase();
                    invokeModifyType(base.getType());
                } else {
                    code[SourceCodeFeature.invocationStmt.ordinal()]++;
                }
            } else if (unit instanceof JIfStmt) {
                code[SourceCodeFeature.ifStmt.ordinal()]++;
            } else if (unit instanceof JLookupSwitchStmt) {
                code[SourceCodeFeature.switchStmt.ordinal()]++;
            }
//            else if (unit instanceof JReturnStmt || unit instanceof JReturnVoidStmt) {
//                code[SourceCodeFeature.returnStmt.ordinal()]++;
//            }

            for (ValueBox defBox : unit.getDefBoxes()) {
                Value value = defBox.getValue();
                if (value instanceof JimpleLocal) {
                    declLocal.add((JimpleLocal) value);
                }
            }
        }
    }

    private static void localType(Type type) {
        if (type instanceof RefType) {
            code[SourceCodeFeature.referType.ordinal()] += 1;
        } else if (type instanceof ArrayType) {
            code[SourceCodeFeature.arrayType.ordinal()] += 1;
        } else if (type instanceof ByteType || type instanceof ShortType || type instanceof CharType || type instanceof BooleanType ||
                type instanceof IntType || type instanceof LongType || type instanceof FloatType || type instanceof DoubleType) {
            code[SourceCodeFeature.primType.ordinal()] += 1;
        }
    }

    private static void newType(Type type) {
        if (type instanceof RefType) {
            switch (((RefType) type).getClassName()) {
                case "java.lang.ref.PhantomReference":
                    code[SourceCodeFeature.newPhantomRef.ordinal()] += 1;
                    break;
                case "java.lang.ref.SoftReference":
                    code[SourceCodeFeature.newSoftRef.ordinal()] += 1;
                    break;
                case "java.lang.ref.WeakReference":
                    code[SourceCodeFeature.newWeakRef.ordinal()] += 1;
                    break;
                case "java.lang.String":
                case "java.lang.StringBuilder":
                    code[SourceCodeFeature.newString.ordinal()] += 1;
                    break;
                default:
                    code[SourceCodeFeature.newStrongRef.ordinal()] += 1;
            }
        }
    }

    private static void invokeModifyType(Type type) {
        if (type instanceof RefType) {
            switch (((RefType) type).getClassName()) {
                case "java.lang.ref.PhantomReference":
                    code[SourceCodeFeature.invokeModifyPhantomRef.ordinal()] += 1;
                    break;
                case "java.lang.ref.SoftReference":
                    code[SourceCodeFeature.invokeModifyStrongRef.ordinal()] += 1;
                    break;
                case "java.lang.ref.WeakReference":
                    code[SourceCodeFeature.invokeModifyWeakRef.ordinal()] += 1;
                    break;
                case "java.lang.String":
                case "java.lang.StringBuilder":
                    code[SourceCodeFeature.invokeModifyString.ordinal()] += 1;
                    break;
                default:
                    code[SourceCodeFeature.invokeModifyStrongRef.ordinal()] += 1;
            }
        } else if (type instanceof ArrayType) {
            code[SourceCodeFeature.modifyArray.ordinal()] += 1;
        }
    }

    private static void modifyType(Type type) {
        if (type instanceof RefType) {
            switch (((RefType) type).getClassName()) {
                case "java.lang.ref.PhantomReference":
                    code[SourceCodeFeature.modifyPhantomRef.ordinal()] += 1;
                    break;
                case "java.lang.ref.SoftReference":
                    code[SourceCodeFeature.modifyStrongRef.ordinal()] += 1;
                    break;
                case "java.lang.ref.WeakReference":
                    code[SourceCodeFeature.modifyWeakRef.ordinal()] += 1;
                    break;
                case "java.lang.String":
                case "java.lang.StringBuilder":
                    code[SourceCodeFeature.modifyString.ordinal()] += 1;
                    break;
                default:
                    code[SourceCodeFeature.modifyStrongRef.ordinal()] += 1;
            }
        } else if (type instanceof ArrayType) {
            code[SourceCodeFeature.modifyArray.ordinal()] += 1;
        }
    }

    private static void nullType(Type type) {
        if (type instanceof RefType) {
            switch (((RefType) type).getClassName()) {
                case "java.lang.ref.PhantomReference":
                    code[SourceCodeFeature.assignNullPhantomRef.ordinal()] += 1;
                    break;
                case "java.lang.ref.SoftReference":
                    code[SourceCodeFeature.assignNullSoftRef.ordinal()] += 1;
                    break;
                case "java.lang.ref.WeakReference":
                    code[SourceCodeFeature.assignNullWeakRef.ordinal()] += 1;
                    break;
                case "java.lang.String":
                case "java.lang.StringBuilder":
                    code[SourceCodeFeature.assignNullString.ordinal()] += 1;
                    break;
                default:
                    code[SourceCodeFeature.assignNullStrongRef.ordinal()] += 1;
            }
        } else if (type instanceof ArrayType) {
            code[SourceCodeFeature.assignNullArray.ordinal()] += 1;
        }
    }

    public static class FileInfo {
        public String filePath;

        public String fileName;
        public LinkedList<CodeFeatureBean> features = new LinkedList<>();

        public FileInfo(String name, String path) {
            fileName = name;
            filePath = path;
        }

        public void addFeature(CodeFeatureBean featureBean) {
            features.add(featureBean);
        }

        public void addFeature(SourceCodeFeature feature, int count) {
            this.addFeature(new CodeFeatureBean(feature, count));
        }
    }

    public static class CodeFeatureBean {
        public SourceCodeFeature feature;
        public int count;

        public CodeFeatureBean(SourceCodeFeature feature, int count) {
            this.feature = feature;
            this.count = count;
        }

        @Override
        public String toString() {
            return feature.toString() + ": " + count;
        }

        public SourceCodeFeature getFeature() {
            return feature;
        }

        public void setFeature(SourceCodeFeature feature) {
            this.feature = feature;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }
}
