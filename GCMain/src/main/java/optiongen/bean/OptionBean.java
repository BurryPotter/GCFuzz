package optiongen.bean;


public class OptionBean extends StatisticData {

    /**
     * name : ActiveProcessorCount
     * since : OpenJDK10
     * deprecated :
     * type : int
     * component : gc
     * default : -1
     * availability : product
     * prefix : -XX:
     * obsoleted :
     * expired :
     * mutex :
     * scope : (0,10000)
     * priority : 100
     */
    /**
     * prefix : -Xgc:
     * name : breadthFirstScanOrdering
     * type : bool
     * default :
     * scope :
     * priority : 100
     * mutex :
     * step : 0.5
     * since : OpenJDK8
     * expired : OpenJDK9
     */
    private String name;
    private String since;
    private String deprecated;
    private String type;
    private String component;
    private String defaultX;
    private String availability;
    private String prefix;
    private String obsoleted;
    private String expired;
    private String mutex;
    private String scope;
    private double priority;
    private String step;

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSince() {
        return since;
    }

    public void setSince(String since) {
        this.since = since;
    }

    public String getDeprecated() {
        return deprecated;
    }

    public void setDeprecated(String deprecated) {
        this.deprecated = deprecated;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getDefaultX() {
        return defaultX;
    }

    public void setDefaultX(String defaultX) {
        this.defaultX = defaultX;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getObsoleted() {
        return obsoleted;
    }

    public void setObsoleted(String obsoleted) {
        this.obsoleted = obsoleted;
    }

    public String getExpired() {
        return expired;
    }

    public void setExpired(String expired) {
        this.expired = expired;
    }

    public String getMutex() {
        return mutex;
    }

    public void setMutex(String mutex) {
        this.mutex = mutex;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public double getPriority() {
        return priority;
    }

    public void setPriority(double priority) {
        this.priority = priority;
    }
}
