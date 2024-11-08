/**
 * Simulate an object allocation pattern observed online request processing services
 * that frequently use large primitive arrays as data buffers.
 * By configuring the JVM so that such buffers represent "humongous objects" to G1,
 * we can cause the IHOP value to drop prematurely,
 * causing more frequent GC than needed to keep up. 
 *
 * Baseline allocation rates:
 * non-humongous allocations: 50 MB/s - 25K objects/s (1/4 will survive)
 * humongous allocations:     40 MB/s - 20 objects/s (Each object is 1.5 MB)
 * 
 * Periodic added allocation spikes to induce to-space exhaustion:
 * non-humongous allocations: 150 MB/s - 75K objects/s (all will survive)
 *
 * Run this program with these JVM command line options:
 *
 * -Xmx512M -Xms512M -XX:G1HeapRegionSize=1M -XX:+UnlockExperimentalVMOptions 
 * -XX:G1MaxNewSizePercent=30 -XX:G1NewSizePercent=30 -Xlog:gc*=debug:file=gc-%p-%t.log
 *
 * (We fix the young gen size to ensure that the baseline allocation does not trigger concurrent marking by itself.)
 *
 * Pass a test duration as Java program parameter. For instance, to run for 60 seconds: 
 * java <vm_flags> AdaptiveIHOPIssueRepro 60
 *
 * @author luoziyi@amazon.com, mathiske@amazon.com
 */
public class AdaptiveIHOPIssueRepro2 {
    
    static final int K = 1024;
    static final int M = K * K;

    static final int REGION_SIZE = 1 * M; // Set this accordingly in the JVM arguments

    static final int HUMONGOUS_OBJECT_SIZE = REGION_SIZE * 3 / 2; // Each object takes two regions
    static final int YOUNG_OBJECT_SIZE = 2 * K;

    static final int HUMONGOUS_ALLOCATION_RATE = 40 * M;
    static final int YOUNG_ALLOCATION_RATE = 50 * M;
    
    static final int N_OLD_OBJECTS = 80 * K;
    static final Object oldObjects[] = new Object[N_OLD_OBJECTS];
     
    static int _oldObjectIndex = 0;
    
    private static void retain(Object newObject, int limit) {
        oldObjects[_oldObjectIndex++] = newObject;
        _oldObjectIndex %= limit;
    }
    
    private static Object newObject() {
        return new byte[YOUNG_OBJECT_SIZE];
    }
    
    private static Object newHumongousObject() {
        return new byte[HUMONGOUS_OBJECT_SIZE];
    }
    
    public static void main(String[] args) throws InterruptedException {
        final int humongousAllocationsPerSecond = HUMONGOUS_ALLOCATION_RATE / (REGION_SIZE * (int) Math.ceil((float) HUMONGOUS_OBJECT_SIZE/REGION_SIZE));
        final int baselineYoungAllocationsPerSecond = YOUNG_ALLOCATION_RATE / YOUNG_OBJECT_SIZE;

        // args[0]: Program duration in seconds
        final int totalHumongousCycles = Integer.parseInt(args[0]) * humongousAllocationsPerSecond;

        int remainingYoungSpikeCycles = 0;

        for (int cycle = 1; cycle <= totalHumongousCycles; cycle++) {
            final long cycleStartTimeMillis = System.currentTimeMillis();

            newHumongousObject(); // All our humongous objects are short-lived

            final int youngCyclesPerHumongousCycle = baselineYoungAllocationsPerSecond / humongousAllocationsPerSecond;
            for (int i = 1; i < youngCyclesPerHumongousCycle; i++) {
                if (remainingYoungSpikeCycles > 0) {
                    // Increase young gen object alloc rate from 50 MB/s to 150 MB/s to cause a to-space exhaustion
                    // and increase old object pressure inducing promotions to old gen.
                    retain(newObject(), N_OLD_OBJECTS);
                    retain(newObject(), N_OLD_OBJECTS);
                    retain(newObject(), N_OLD_OBJECTS);
                } else if (i % 4 == 0) {
                    retain(newObject(), N_OLD_OBJECTS / 8); // low pressure from old objects, no promotions to old gen
                }
            }

            // Every 10 seconds, induce a 2 second spike:
            remainingYoungSpikeCycles--;
            if (cycle % (10 * humongousAllocationsPerSecond) == 0) {
                remainingYoungSpikeCycles = 2 * humongousAllocationsPerSecond;
            }

            // Wait for the rest of the humongous allocation cycle to match the requested rate
            final long duration = System.currentTimeMillis() - cycleStartTimeMillis;
            final int millisPerHumongousCycle = 1000 / humongousAllocationsPerSecond;
            if (duration < millisPerHumongousCycle) {
                Thread.sleep(millisPerHumongousCycle - duration);
            } else {
                System.err.println("[WARN] allocation took longer than expected, milliseconds: " + duration);
            }
        }
    }
 
}
