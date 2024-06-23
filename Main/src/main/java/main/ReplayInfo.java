package main;

import dtjvms.executor.GC.GCType;
import dtjvms.executor.GC.HotspotGCType;
import dtjvms.executor.GC.OpenJ9GCType;
import optiongen.generator.VMType;

public class ReplayInfo {
    private long seed;
    private String testClass;
    private String vmoption;

    private String originClass;
    private String tag;
    private VMType vmType;

    public ReplayInfo(long seed, String testClass, String vmoption, String originClass) {
        this.seed = seed;
        this.testClass = testClass;
        this.vmoption = vmoption;
        this.originClass = originClass;
    }

    public ReplayInfo(long seed, String testClass, String vmoption, String originClass, String tag) {
        this.seed = seed;
        this.testClass = testClass;
        this.vmoption = vmoption;
        this.originClass = originClass;
        this.tag = tag;
    }

    public ReplayInfo(long seed, String testClass, String vmoption, String originClass, String tag, String gcType) {
        this.seed = seed;
        this.testClass = testClass;
        this.vmoption = vmoption;
        this.originClass = originClass;
        this.tag = tag;
        VMType vmType = VMType.HOTSPOT;
        if (gcType != null) {
            try {
                HotspotGCType.valueOf(gcType);
                //use valueOf() to classify the vmType.
                vmType = VMType.HOTSPOT;
            } catch (IllegalArgumentException e) {
//            e.printStackTrace();
            }
            try {
                OpenJ9GCType.valueOf(gcType);
                //use valueOf() to classify the vmType.
                vmType = VMType.J9;
            } catch (IllegalArgumentException e) {
//            e.printStackTrace();
            }
        } else {
            System.out.println("gc type is null. gcType=" + gcType);
        }
        this.vmType = vmType;
        System.out.println("vmType: " + vmType);

    }

    public VMType getVmType() {
        return vmType;
    }

    public void setVmType(VMType vmType) {
        this.vmType = vmType;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getOriginClass() {
        return originClass;
    }

    public void setOriginClass(String originClass) {
        this.originClass = originClass;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public String getTestClass() {
        return testClass;
    }

    public void setTestClass(String testClass) {
        this.testClass = testClass;
    }

    public String getVmoption() {
        return vmoption;
    }

    public void setVmoption(String vmoption) {
        this.vmoption = vmoption;
    }
}
