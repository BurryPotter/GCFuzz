package dtjvms.fjvms.config;

import dtjvms.executor.GC.GCType;

public class ConfigBean {
    private String version;
    private String file;
    private String options;
    private String javac_options;
    private String parameters;
    public void setVersion(String version) {
        this.version = version;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public void setJavac_options(String javac_options) {
        this.javac_options = javac_options;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getVersion() {
        return version;
    }

    public String getFile() {
        return file;
    }

    public String getOptions() {
        return options;
    }

    public String getJavac_options() {
        return javac_options;
    }

    public String getParameters() {
        return parameters;
    }
}

