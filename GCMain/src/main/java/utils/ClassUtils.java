package utils;

import codegen.blocks.ClassInfo;
import config.PlatformAttr;
import dtjvms.DTPlatform;
import soot.*;
import soot.baf.BafASMBackend;
import soot.baf.JasminClass;
import soot.options.Options;
import vmoptions.Option;

import java.io.*;
import java.util.LinkedList;

public class ClassUtils {

    public static LinkedList<String> excludePathList;

    public static void initSootEnv() {
        G.reset();
        String claspath = System.getProperty("java.class.path");
//        String modulePath = System.getProperty("jdk.module.path");
//        claspath = claspath + System.getProperty("path.separator") + modulePath;
        Options.v().set_soot_classpath(claspath);
        Options.v().set_weak_map_structures(true);
        Options.v().set_ignore_resolving_levels(true);
        Options.v().set_whole_program(true);
        Options.v().set_app(true);
        excludeJDKLibrary();
    }

    /**
     * Add related classes to Soot
     *
     * @param classPath
     */
    public static void initSootEnvWithClassPath(String classPath) {
        G.reset();
        String claspath = DTPlatform.getJavaClassPath() ;
//        String modulePath = System.getProperty("jdk.module.path");
//        claspath = claspath + System.getProperty("path.separator") + modulePath;
        claspath = classPath + System.getProperty("path.separator") + claspath;
        Options.v().set_soot_classpath(claspath);
        Options.v().set_weak_map_structures(true);
        Options.v().set_java_version(Options.java_version_8);
        Options.v().set_wrong_staticness(Options.wrong_staticness_ignore);

        Options.v().set_ignore_resolving_levels(true);
        Options.v().set_whole_program(true);
        Options.v().set_app(true);
        excludeJDKLibrary();
    }

    /**
     * Set the output position of class after Soot mutation
     *
     * @param outpath
     */
    public static void set_output_path(String outpath) {
        Options.v().set_output_dir(outpath);
    }

    public static SootClass loadClass(String classname) {

        try {
            SootClass sootClass = Scene.v().forceResolve(classname, SootClass.BODIES);
            Scene.v().loadNecessaryClasses();
            return sootClass;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ClassInfo loadClassInfo(String classname) {

        try {
            SootClass sootClass = Scene.v().forceResolve(classname, SootClass.BODIES);
            Scene.v().loadNecessaryClasses();
            return ClassInfo.analyseClass(sootClass);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void saveClassToPath(SootClass sootClass, String dirPath) {

        saveClassToPath(sootClass, sootClass.getName(), dirPath);
    }

    public static void saveClassToPath(SootClass sootClass, String className, String dirPath) {

        String jimpleFilePath = dirPath + PlatformAttr.FILE_SEPARATOR + className + ".jimple";
        String classFilePath = dirPath + PlatformAttr.FILE_SEPARATOR + className + ".class";
        try {
            File dir = new File(dirPath);
            if (!dir.exists()) dir.mkdir();
            OutputStream jimpleStreamOut = new FileOutputStream(jimpleFilePath);
            PrintWriter jimpleWriterOut = new PrintWriter(new OutputStreamWriter(jimpleStreamOut));
            //01 jimple
            Printer.v().printTo(sootClass, jimpleWriterOut);
            jimpleStreamOut.flush();
            jimpleWriterOut.flush();
            jimpleStreamOut.close();

            OutputStream classStreamOut = new FileOutputStream(classFilePath);
            //class
            BafASMBackend backend = new BafASMBackend(sootClass, Options.v().java_version());
            backend.generateClassFile(classStreamOut);
            classStreamOut.flush();
            classStreamOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean saveClass(SootClass sootClass, int mode) {

        boolean savaStatus = true;
        String fileName = SourceLocator.v().getFileNameFor(sootClass, mode);
        try {
            OutputStream streamOut = new FileOutputStream(fileName);
            PrintWriter writerOut = new PrintWriter(new OutputStreamWriter(streamOut));
            switch (mode) {
                case Options.output_format_jimple:
                    Printer.v().printTo(sootClass, writerOut);
                    break;
                case Options.output_format_class:
                    try {
                        BafASMBackend backend = new BafASMBackend(sootClass, Options.v().java_version());
                        backend.generateClassFile(streamOut);
                    } catch (Exception e) {
                        e.printStackTrace();
                        savaStatus = false;
                    }
                    break;
                case Options.output_format_jasmin:
                    JasminClass jasminClass = new JasminClass(sootClass);
                    jasminClass.print(writerOut);
                    System.out.println(fileName);
                    break;
                default:
                    jasminClass = new JasminClass(sootClass);
                    jasminClass.print(writerOut);
                    break;
            }
            writerOut.flush();
            streamOut.close();
        } catch (Exception e) {
            e.printStackTrace();
            savaStatus = false;
        }
        return savaStatus;
    }


    private static void excludeJDKLibrary() {
        //exclude jdk classes
        Options.v().set_exclude(excludeList());
//        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_allow_phantom_refs(true);
    }

    private static LinkedList<String> excludeList() {

        if (excludePathList == null) {
            excludePathList = new LinkedList<String>();
            excludePathList.add("java.");
            excludePathList.add("javax.");
            excludePathList.add("sun.");
            excludePathList.add("sunw.");
            excludePathList.add("com.sun.");
            excludePathList.add("com.ibm.");
            excludePathList.add("com.apple.");
            excludePathList.add("apple.awt.");
        }
        return excludePathList;
    }

}
