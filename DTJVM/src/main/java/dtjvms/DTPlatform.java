package dtjvms;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

public class DTPlatform {

    public static String OS_NAME = System.getProperty("os.name").toLowerCase();
    public static String OS_VERSION = System.getProperty("os.version").toLowerCase();
    public static String JAVA_HOME = System.getProperty("java.home");
    public static String JAVA_VERSION = System.getProperty("java.version");
    public static String FILE_SEPARATOR = System.getProperty("file.separator");
    public static String PATH_SEPARATOR = System.getProperty("path.separator");
    public static String LINE_SEPARATOR = System.getProperty("line.separator");
    public static String FILE_ENCODIND;

    public static String JAR_SUFFIX = ".jar";
    public static String CLASS_SUFFIX = ".class";
    public static String JAVA_CMD = "java";
    public static String MACOS_JAVA_PATH = "Contents/Home/bin/java";
    public static String LINUX_JAVA_PATH = "bin" + FILE_SEPARATOR + "java";
    public static String LINUX_DEBUG_JAVA_PATH = "jdk" + FILE_SEPARATOR + "bin" + FILE_SEPARATOR + "java";

    public static String WIN_JAVA_PATH = "bin" + FILE_SEPARATOR + "java.exe";
    public static String PROJECT_ELEMENTS = "lib|out|src|test|target";
    public static String RUNNABLE_CLASS_LOADER = "dtjvms.loader.RunnableClassLoader";

    public static String JUNIT_CMD = "JAVACMD VMOPTIONS -cp CLASSPATH org.junit.runner.JUnitCore CLASSNAME ARGS";
    public static String APPLICATION_CMD = "JAVACMD VMOPTIONS -cp CLASSPATH CLASSNAME ARGS";

    public static String LOCAL_JAVA_CMD = "java -cp CLASSPATH CLASSNAME ARGS";
    public static String LOCAL_FULL_JAVA_CMD = "java VMOPTIONS -cp CLASSPATH CLASSNAME ARGS";

    public static String[] FILTER_CLASS_PATH = new String[]{
            "IDEA"
    };

    private static DTPlatform tp;

    static {
        if (isWin()) {
            FILE_ENCODIND = "GBK";
        } else {
            FILE_ENCODIND = "UTF-8";
        }
    }

    public static String getJavaClassPath() {

        String classPath = System.getProperty("java.class.path");
        String modulePath = System.getProperty("jdk.module.path");
        String[] paths;
        if (modulePath != null) {
            classPath = classPath + System.getProperty("path.separator") + modulePath;// windows的path分隔符是;
        }
        paths = classPath.split(System.getProperty("path.separator"));

        ArrayList<String> classPaths = new ArrayList<>();
        for (String path : paths) {
            for (String s : FILTER_CLASS_PATH) {
                if (!path.contains(s)) {
                    classPaths.add(path);
                }
            }
        }
        classPath = StringUtils.join(classPaths, DTPlatform.PATH_SEPARATOR);
//        for (String s : FILTER_CLASS_PATH) {
//            classPath = classPath.replace(s,"");
//        }
        return classPath;
    }

    public static DTPlatform getInstance() {

        if (tp == null) {
            tp = new DTPlatform();
        }
        return tp;
    }

    public static String getOsName() {
        return OS_NAME;
    }

    public static boolean isMac() {
        return OS_NAME.indexOf("mac") >= 0;
    }

    public static boolean isLinux() {
        return OS_NAME.indexOf("linux") >= 0;
    }

    public static boolean isWin() {
        return OS_NAME.indexOf("windows") >= 0;
    }

    @Override
    public String toString() {
        String titile = String.join("", Collections.nCopies(50, "=")) +
                " Testing Platform Information " + String.join("", Collections.nCopies(50, "="));
        return titile + LINE_SEPARATOR +
                "     os name: " + OS_NAME + LINE_SEPARATOR +
                "  os version: " + OS_VERSION + LINE_SEPARATOR +
                "   java home: " + JAVA_HOME + LINE_SEPARATOR +
                "java version: " + JAVA_VERSION + LINE_SEPARATOR +
                String.join("", Collections.nCopies(titile.length(), "="));
    }
}
