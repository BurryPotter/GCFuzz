import sun.misc.Unsafe;
import java.lang.management.*;
import sun.hotspot.WhiteBox;

/*
 * -XX:+UseG1GC -XX:OldSize=24m -XX:MaxNewSize=64m -XX:NewSize=64m
 * -XX:G1HeapRegionSize=8m -XX:+PrintHeapAtGC -XX:MaxTenuringThreshold=15
 * -XX:SurvivorRatio=1 -XX:InitialTenuringThreshold=15
 * -XX:+PrintTenuringDistribution -XX:+PrintGC -XX:+G1PrintHeapRegions
 * -XX:+WhiteBoxAPI
 */
public class TestUsingWB {
    private static final int MAX_TENURING_THRESHOLD = 15;
    private static final int MEMORY_TO_ALLOCATE = 16 * 1024 * 1024;

    private static Object[] garbage
            = new Object[MEMORY_TO_ALLOCATE / Unsafe.ARRAY_BYTE_BASE_OFFSET];
    private static Object temp;

    public static void main(String args[]) {
        MemoryPoolMXBean survivorSpaceBean = getMemoryPoolBean("survivor");
        MemoryPoolMXBean tenuredSpaceBean = getMemoryPoolBean("old");
        GarbageCollectorMXBean gcBean = getYoungGCBean();
        WhiteBox whiteBox = WhiteBox.getWhiteBox();
        // tenure everuthing allocated prior this call
        System.gc();
        long expectedUsage = tenuredSpaceBean.getUsage().getUsed()
                + MEMORY_TO_ALLOCATE;
        // allocate objects that will be promoted to survivor space
        for (int i = 0; i < garbage.length; i++) {
            garbage[i] = new byte[0];
        }
        // force young GC MAX_TENURING_THRESHOLD times
        while (gcBean.getCollectionCount() <= MAX_TENURING_THRESHOLD) {
            whiteBox.youngGC();
        }
        // verify that there are no survivor regions
        if (survivorSpaceBean.getUsage().getUsed() > 0) {
            System.out.println("Expected old regions usage: " + expectedUsage);
            System.out.println("Actual old regions usage: "
                               + tenuredSpaceBean.getUsage().getUsed());
            System.out.println("Objects were not tenured?");
            while (true) {}
        }
    }

    private static MemoryPoolMXBean getMemoryPoolBean(String name) {
        for (MemoryPoolMXBean bean :
                     ManagementFactory.getMemoryPoolMXBeans()) {
            if (bean.getName().toLowerCase().contains(name)) {
                return bean;
            }
        }
        throw new Error("Unable to find MemoryPoolMXBean for survivor space");
    }

    private static GarbageCollectorMXBean getYoungGCBean() {
        for (GarbageCollectorMXBean bean :
                     ManagementFactory.getGarbageCollectorMXBeans()) {
            if (bean.getName().contains("Young")) {
                return bean;
            }
        }
        throw new Error("Unable to find GarbageCollectorMXBean fot young GC");
    }
}
