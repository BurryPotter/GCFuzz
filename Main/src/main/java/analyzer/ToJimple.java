package analyzer;

import dtjvms.executor.GC.GCExecutor;
import dtjvms.executor.JvmOutput;
import soot.SootClass;
import soot.options.Options;
import utils.ClassUtils;

import java.io.File;
import java.util.ArrayList;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class ToJimple {
    public static String javac = "javac";

    public static void main(String[] args) {
//        String path = "D:\\projects\\GCGenerator\\historyCases\\gc";
        String[] paths = new String[]{
                "D:\\projects\\GCGenerator\\historyCases\\hotspot_gc"
//                "D:\\projects\\GCGenerator\\historyCases\\filter\\gc",
//                "D:\\projects\\GCGenerator\\historyCases\\filter\\runtime",
//                "D:\\projects\\GCGenerator\\historyCases\\filter\\compiler",
        };
        for (String path : paths) {
//            compileJavaFile(path);
            class2Jimple(path);
        }

    }
    public static void class2Jimple(String path) {
        File rootDir = new File(path);
        FileFinder.loadFiles(rootDir, ".class");
        int errCount = 0;
        for (File javaFile : FileFinder.files) {
            try {
                String cp = javaFile.getParentFile().getAbsolutePath();
                String fileName = javaFile.getName();
                fileName = fileName.replace(".class", "");
                ClassUtils.initSootEnvWithClassPath(cp);
                Options.v().set_src_prec(Options.src_prec_class);
                Options.v().set_output_format(Options.output_format_jimple);
                SootClass sootClass = ClassUtils.loadClass(fileName);
//                System.out.println(sootClass);
                if (sootClass == null) {
                    System.out.println(javaFile.getAbsolutePath());
                    errCount++;
                    System.out.println("sootClass load error");
                    continue;
                }
                ClassUtils.saveClassToPath(sootClass, cp);
            } catch (Throwable e) {
                errCount++;
                e.printStackTrace();
            }
        }
        System.out.println(errCount+" cases can not convert to jimple");
    }
    public static void compileJavaFile(String path){
        File rootDir = new File(path);
        FileFinder.loadFiles(rootDir, ".java");
        int errCount = 0;
        for (File javaFile : FileFinder.files) {
            try {
                String cp = javaFile.getParentFile().getAbsolutePath();
                String fileName = javaFile.getName();
                JvmOutput output = javac(cp, fileName);
                if (output.getExitValue() != 0) {
                    System.out.println(javaFile.getAbsolutePath());
                    System.out.println(output.getStderr());
                    errCount++;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        System.out.println(errCount+" cases can not be compiled");
    }
    public static JvmOutput javac(String cp, String javaName) {
        String cmd = javac + " -Xlint:deprecation " + javaName;
        JvmOutput output = GCExecutor.getInstance().execute(cmd, cp);
//        String err = output.getStderr();
//        System.out.println(err);
//        System.out.println();
        return output;
    }

    public static void java2jimple(String cp, String className) {
        System.out.println(cp);
        System.out.println(className);
        soot.Main.main(new String[]{"-pp", "-src-prec", "J", "-f", "c", "-cp", cp, className});
    }
}
