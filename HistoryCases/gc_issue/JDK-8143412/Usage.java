
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;

public class Usage {

    public static void main(String[] args) {
        MemoryUsage memToFill = getEdenUsage();
        System.out.printf("Commited : %15d  Init : %15d  Max : %15d Used : %15d%n", memToFill.getCommitted(), memToFill.getInit(), memToFill.getMax(), memToFill.getUsed());
    }

    private static MemoryUsage getUsage(String name) {
        MemoryUsage ret = null;
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            if (pool.getName().matches(name)) {
                ret = pool.getUsage();
            }
        }
        return ret;
    }

    public static MemoryUsage getEdenUsage() {
        return getUsage(".*Eden.*");
    }
}
