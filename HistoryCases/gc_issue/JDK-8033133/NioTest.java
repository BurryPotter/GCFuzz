import java.lang.management.MemoryMXBean;
import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;

public class NioTest {

    public static final int SIZE = 25_000_000;
    public static ByteBuffer bb;
    public static final MemoryMXBean mBean = ManagementFactory.getMemoryMXBean();
    
    public static void main(String[] args) {
        // stage 1: initial
        gc();
        long h_before = getUsedHeap();
        // dumpHeap("dump_1.hprof");
        System.out.println("Used heap initial: " + toK(h_before));            


        // stage 2: allocating memory
        System.out.println("   allocating direct memory");
        bb = ByteBuffer.allocateDirect((int)SIZE);
        long h_after = getUsedHeap();
        // dumpHeap("dump_2.hprof");
        System.out.println("Used heap after  : " + toK(h_after));

        // stage 3: invoking GC
        System.out.println("   invoking GC");
        gc();
        long h_last = getUsedHeap();
        // dumpHeap("dump_3.hprof");
        System.out.println("Used heap final  : " + toK(h_last));

        // just check
        if (h_after - h_before > SIZE / 3) {
            System.out.println("Test failed: " + toK(h_after - h_before)+ " of heap was allocated");
            System.exit(1);
        }
        System.out.println("Test passed");

    }

    public static String toK(long number) {
        return (number/1024) + "K";
    }

    public static void dumpHeap(String file) {
        try {
            sun.management.ManagementFactoryHelper.getDiagnosticMXBean().dumpHeap(file, true);
        } catch (Exception e) {
            System.out.println("Ex: " + e);
        }
    }

       
    /**
     * @return the size of used heap
     */
    public static long getUsedHeap() {
         return mBean.getHeapMemoryUsage().getUsed();
    }

    public static void gc() {
        System.gc();
        try {
            Thread.sleep(1000);
        } catch (Exception whatever) {
        }
    }

}
