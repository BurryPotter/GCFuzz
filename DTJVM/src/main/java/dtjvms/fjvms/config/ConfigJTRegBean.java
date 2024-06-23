package dtjvms.fjvms.config;

import dtjvms.executor.GC.GCType;

public class ConfigJTRegBean {
    private String className;
    private ConfigBean bean;
    private GCType gcType;

    public ConfigJTRegBean(String className, ConfigBean bean) {
        this.className = className;
        this.bean = bean;
    }

    public ConfigJTRegBean(String className, String options, String fileName, String parameters) {
        this(className, options, fileName, parameters, null, null);
    }

    public ConfigJTRegBean(String className, String options, String fileName, String parameters, GCType gcType) {
        this(className, options, fileName, parameters, null, null);
        this.gcType = gcType;
    }

    public ConfigJTRegBean(String className, String options, String fileName, String parameters, String javacOptions, String jdkVersion) {
        ConfigBean bean = new ConfigBean();
        bean.setFile(fileName);
        bean.setJavac_options(javacOptions);
        bean.setOptions(options);
        bean.setParameters(parameters);
        bean.setVersion(jdkVersion);
        this.className = className;
        this.bean = bean;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public ConfigBean getBean() {
        return bean;
    }

    public void setBean(ConfigBean bean) {
        this.bean = bean;
    }

    public GCType getGcType() {
        return gcType;
    }

    public void setGcType(GCType gcType) {
        this.gcType = gcType;
    }
}
