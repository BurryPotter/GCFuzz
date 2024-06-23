package dtjvms;

import java.io.*;
import java.util.*;

public class DTConfiguration {

    public static final String SLASH = "/";
    public static final String DOT = ".";
    public static final String SEMICOLON = ";";

    public static String JVM_DEPENS_ROOT = "." + DTPlatform.FILE_SEPARATOR + "01JVMS";
    public static String PROJECT_DEPENS_ROOT = "." + DTPlatform.FILE_SEPARATOR + "02Benchmarks";

    public static Boolean debug = false;

    public static Boolean HAS_PREDEFINED_CLASSES = false;
    public static String PREDEFINED_CLASSES_PATH = "";

    public static Set<String> OPENJDK_VERSIONS = new HashSet<>();
    public static Set<String> FILTER_PROJECTS = new HashSet<>();
    public static Set<String> TARGET_PROJECTS = new HashSet<>();
    public static Set<String> FILTER_JVMS = new HashSet<>();
    public static Set<String> TARGET_JVMS = new HashSet<>();
    public static Set<String> SPECIFY_VM_VMOPTIONS = new HashSet<>();
    public static Set<String> FILTER_WORDS = new HashSet<>();
    public static Set<String> RESULT_KEY_WORDS = new HashSet<>();
    public static Set<String> PROJECTS_ELEMENTS = new HashSet<>();
    public static String PROJECT_RUNTIME_CONFIG;
    public static long CLASS_MAX_RUNTIME = 5;
    public static Set<String> PROJECTS_FILTER_CLASSES = new HashSet<>();
    public static Set<String> TARGET_CLASSES_PREDEFINED = new HashSet<>();
    public static Set<String> HOTSPOT_OPTIONS_NUMERIC = new HashSet<>();
    public static Set<String> HOTSPOT8_OPTIONS_BOOLEAN = new HashSet<>();
    public static Set<String> HOTSPOT11_OPTIONS_BOOLEAN = new HashSet<>();
    public static Set<String> FILTER_PROJECTS_KEYWORDS = new HashSet<>();
    public static Set<String> TARGET_PROJECTS_KEYWORDS = new HashSet<>();

    public static long OUTPUT_MAX_LINES = 200;
    public static long TIMEOUT_MAX_COUNT = 5;
    public static int GC_EVENT_NGRAM = 5;
    public static boolean FIND_UNIQUE_BUG = false;
    public static int UNIQUE_JDK_VERSION = 8;
    public static String AFL_PATH = "/home/zhengkai/AFLplusplus-stable/afl-showmap";
    public static boolean EDGE_COV = false;
    static {

        DTProperties dtProps = DTProperties.getInstance();
        storePropertyValues(dtProps.getProperty(DTProperties.OPENJDK_VERSIONS_KEY), OPENJDK_VERSIONS);
        storePropertyValues(dtProps.getProperty(DTProperties.FILTER_PROJECTS_KEY), FILTER_PROJECTS);
        storePropertyValues(dtProps.getProperty(DTProperties.TARGET_PROJECTS_KEY), TARGET_PROJECTS);
        storePropertyValues(dtProps.getProperty(DTProperties.FILTER_JVMS_KEY), FILTER_JVMS);
        storePropertyValues(dtProps.getProperty(DTProperties.TARGET_JVMS_KEY), TARGET_JVMS);
        storePropertyValues(dtProps.getProperty(DTProperties.SPECIFY_VM_VMOPTIONS_KEY), SPECIFY_VM_VMOPTIONS);
        storePropertyValues(dtProps.getProperty(DTProperties.FILTER_WORDS_KEY), FILTER_WORDS);
        storePropertyValues(dtProps.getProperty(DTProperties.RESULT_KEY_WORDS_KEY), RESULT_KEY_WORDS);
        storePropertyValues(dtProps.getProperty(DTProperties.PROJECTS_ELEMENTS_KEY), PROJECTS_ELEMENTS);
        PROJECT_RUNTIME_CONFIG = dtProps.getProperty(DTProperties.PROJECTS_RUNTIME_CONFIG_KEY);
        CLASS_MAX_RUNTIME = Long.parseLong(dtProps.getProperty(DTProperties.CLASS_MAX_RUNTIME_KEY));
        storePropertyValues(dtProps.getProperty(DTProperties.PROJECTS_FILTER_CLASSES_KEY), PROJECTS_FILTER_CLASSES);
        storePropertyValues(dtProps.getProperty(DTProperties.HOTSPOT_OPTIONS_NUMERIC_KEY), HOTSPOT_OPTIONS_NUMERIC);
        storePropertyValues(dtProps.getProperty(DTProperties.HOTSPOT8_OPTIONS_BOOLEAN_KEY), HOTSPOT8_OPTIONS_BOOLEAN);
        storePropertyValues(dtProps.getProperty(DTProperties.HOTSPOT11_OPTIONS_BOOLEAN_KEY), HOTSPOT11_OPTIONS_BOOLEAN);
        storePropertyValues(dtProps.getProperty(DTProperties.FILTER_PROJECTS_KEYWORDS_KEY), FILTER_PROJECTS_KEYWORDS);
        storePropertyValues(dtProps.getProperty(DTProperties.TARGET_PROJECTS_KEYWORDS_KEY), TARGET_PROJECTS_KEYWORDS);
        OUTPUT_MAX_LINES = Long.parseLong(dtProps.getProperty(DTProperties.OUTPUT_MAX_LINES_KEY));
        TIMEOUT_MAX_COUNT = Long.parseLong(dtProps.getProperty(DTProperties.TIMEOUT_MAX_COUNT_KEY));
        GC_EVENT_NGRAM = Integer.parseInt(dtProps.getProperty(DTProperties.GC_EVENT_NGRAM_KEY));
        FIND_UNIQUE_BUG = Boolean.parseBoolean(dtProps.getProperty(DTProperties.FIND_UNIQUE_BUG_KEY));
        UNIQUE_JDK_VERSION = Integer.parseInt(dtProps.getProperty(DTProperties.UNIQUE_JDK_VERSION_KEY));

        EDGE_COV = Boolean.parseBoolean(dtProps.getProperty(DTProperties.EDGE_COV_KEY));
        AFL_PATH = dtProps.getProperty(DTProperties.AFL_PATH_KEY);


    }

    private static void storePropertyValues(String values, Set<String> toSet) {

        if (values != null) {
            String[] split = values.split(SEMICOLON);
            for (String val : split) {
//                val = val.replace(DOT, SLASH).trim();
                val = val.trim();
                if (!val.isEmpty()) {
                    toSet.add(val);
                }
            }
        }
    }

    public static void setJvmDepensRoot(String jvmDepensRoot) {
        JVM_DEPENS_ROOT = jvmDepensRoot;
    }

    public static void setProjectDepensRoot(String projectDepensRoot) {
        PROJECT_DEPENS_ROOT = projectDepensRoot;
    }

    /**
     * load predefined target classes
     *
     * @param filepath
     */
    public static void setPredefinedTargetClasses(String filepath) {

        DTConfiguration.HAS_PREDEFINED_CLASSES = true;
        DTConfiguration.PREDEFINED_CLASSES_PATH = filepath;

        DTConfiguration.TARGET_CLASSES_PREDEFINED.clear();
        File file = new File(filepath);
        if (file.exists()) {
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
//                    System.out.println(line);
                    if (!line.isEmpty()) {
                        DTConfiguration.TARGET_CLASSES_PREDEFINED.add(line);
                    }
                }
                bufferedReader.close();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Exception found when reading: " + filepath + " !");
            }
        } else {

            throw new RuntimeException("Predefined Classes Path: " + filepath + " not Available!");
        }
    }
}
