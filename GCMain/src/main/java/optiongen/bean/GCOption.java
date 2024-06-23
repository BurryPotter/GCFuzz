package optiongen.bean;

import config.GCConfiguration;
import dtjvms.executor.GC.GCHelper;
import dtjvms.executor.GC.GCType;

public class GCOption<T extends GCType> extends StatisticData {
    private final T gcType;
    private String gcOption;
    private double priority;

    public GCOption(T gcType) {
        this.gcType = gcType;
        this.gcOption = GCHelper.gc2option(gcType);
        priority = GCConfiguration.OPTION_PRIORITY;
    }

    public double getPriority() {
        return priority;
    }

    public void setPriority(double priority) {
        this.priority = priority;
    }

    public String getGcOption() {
        return gcOption;
    }
    public T getGcType(){
        return gcType;
    }
}
