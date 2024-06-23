package dtjvms;

import java.io.InputStream;
import java.util.Properties;

public class DTProperties extends Properties {

    public static final String OPENJDK_VERSIONS_KEY = "openjdk.versions";
    public static final String FILTER_PROJECTS_KEY = "filter.projects";
    public static final String TARGET_PROJECTS_KEY = "target.projects";
    public static final String FILTER_JVMS_KEY = "filter.jvms";
    public static final String TARGET_JVMS_KEY = "target.jvms";
    public static final String SPECIFY_VM_VMOPTIONS_KEY = "specify.vm.options";
    public static final String FILTER_WORDS_KEY = "result.filter.words";
    public static final String RESULT_KEY_WORDS_KEY = "result.key.words";
    public static final String PROJECTS_ELEMENTS_KEY = "projects.elements";
    public static final String CLASS_MAX_RUNTIME_KEY = "class.max.runtime";
    public static final String PROJECTS_RUNTIME_CONFIG_KEY = "projects.runtime.config.file";
    public static final String PROJECTS_FILTER_CLASSES_KEY = "projects.filter.classes.prefix";
    public static final String DEFAULT_PROPERTIES = "/default.properties";
    public static final String HOTSPOT_OPTIONS_NUMERIC_KEY = "hotspot.options.numeric";
    public static final String HOTSPOT8_OPTIONS_BOOLEAN_KEY = "hotspot8.options.boolean";
    public static final String HOTSPOT11_OPTIONS_BOOLEAN_KEY = "hotspot11.options.boolean";
    public static final String FILTER_PROJECTS_KEYWORDS_KEY = "filter.projects.keywords";
    public static final String TARGET_PROJECTS_KEYWORDS_KEY = "target.projects.keywords";

    public static final String OUTPUT_MAX_LINES_KEY = "output.max.lines";
    public static final String TIMEOUT_MAX_COUNT_KEY = "timeout.max.count";
    public static final String GC_EVENT_NGRAM_KEY = "gc.event.ngram";

    public static final String FIND_UNIQUE_BUG_KEY = "find.unique.bug";
    public static final String UNIQUE_JDK_VERSION_KEY = "unique.openjdk.version";
    public static final String AFL_PATH_KEY = "afl.path";
    public static final String EDGE_COV_KEY = "edge.coverage";

    private static DTProperties instance;
    public DTProperties() {

        try {
            InputStream inputStream = this.getClass().getResourceAsStream(DEFAULT_PROPERTIES);
            if (inputStream == null){
                System.err.println("No " + DEFAULT_PROPERTIES);
                throw new IllegalAccessException();
            }
            load(inputStream);
            inputStream.close();
        }catch (Exception e){

            e.printStackTrace();
            System.err.println("Unable to load properties file from " + DEFAULT_PROPERTIES);
        }

    }

    public static DTProperties getInstance(){
        if (instance == null){
            instance = new DTProperties();
        }
        return instance;
    }

    @Override
    public String getProperty(String key) {
        String prop = System.getProperty(key);
        if (prop == null) {
            prop = super.getProperty(key);
        }
        return prop;
    }
}
