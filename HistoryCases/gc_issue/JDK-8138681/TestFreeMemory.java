package gc.g1.humongousObjects;

import sun.hotspot.WhiteBox;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;


/**
 * @test TestFreeMemory
 * @summary Checks that heap counters work as expected during Humongous allocations
 * @requires vm.gc=="G1" | vm.gc=="null"
 * @library /testlibrary /../../test/lib
 * @modules java.management
 * @build sun.hotspot.WhiteBox
 * gc.g1.humongousObjects.TestFreeMemory
 * @run driver ClassFileInstaller sun.hotspot.WhiteBox
 * sun.hotspot.WhiteBox$WhiteBoxPermission
 * @run main/othervm -Xms200M -Xmx200M -XX:+UseG1GC -XX:+UnlockDiagnosticVMOptions -XX:+WhiteBoxAPI -Xbootclasspath/a:.
 * -XX:G1HeapRegionSize=1M -Xloggc:TestFreeMemory.gc.log
 * gc.g1.humongousObjects.TestFreeMemory
 */
public class TestFreeMemory {
    // In case of 128 byte padding
    private static final int MAX_PADDING_SIZE = 128;

    /**
     * Detects amount of extra bytes required to allocate a byte array.
     * Allocating a byte[n] array takes more then just n bytes in the heap.
     * Extra bytes are required to store object reference and the length.
     * This amount depends on bitness and other factors.
     *
     * @return byte[] memory overhead
     */
    public static int detectByteArrayAllocationOverhead() {

        WhiteBox whiteBox = WhiteBox.getWhiteBox();

        int zeroLengthByteArraySize = (int) whiteBox.getObjectSize(new byte[0]);

        // Since we do not know is there any padding in zeroLengthByteArraySize we cannot just take byte[0] size as overhead
        for (int i = 1; i < MAX_PADDING_SIZE + 1; ++i) {
            int realAllocationSize = (int) whiteBox.getObjectSize(new byte[i]);
            if (realAllocationSize != zeroLengthByteArraySize) {
                // It means we did not have any padding on previous step
                return zeroLengthByteArraySize - (i - 1);
            }
        }
        throw new Error("We cannot find byte[] memory overhead - should not reach here");
    }

    private static final WhiteBox WHITE_BOX = WhiteBox.getWhiteBox();
    private static final int REGION_SIZE = WHITE_BOX.g1RegionSize();


    private enum MemoryCounter {
        MX_BEAN_COUNTER {
            @Override
            public long getFreeMemory() {
                return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax() - ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
            }

        },
        RUNTIME_COUNTER {
            @Override
            public long getFreeMemory() {
                return Runtime.getRuntime().freeMemory();
            }

        };

        public abstract long getFreeMemory();
    }

    private static String formatByte(long bytes) {

        if (bytes < 1024) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = ("KMGTPE").charAt(exp - 1) + "i";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    public static void main(String[] args) {

        MemoryCounter memoryRuntimeCounter = MemoryCounter.RUNTIME_COUNTER;
        MemoryCounter memoryMXBeanCounter = MemoryCounter.MX_BEAN_COUNTER;

        String runtimeFreeText = "Runtime.getRuntime().freeMemory() returns %s free\n";
        String mxBeanFreeText = "MemoryMXBean (.getMax() - .getUsed()) returns %s free\n";

        int byteArrayMemoryOverhead = detectByteArrayAllocationOverhead();

        // Maximum byte[] that takes exactly one region
        int maxByteArrayOneRegionSize = REGION_SIZE - byteArrayMemoryOverhead;

        List<byte[]> allocations = new ArrayList<>();

        //Free memory before starting humongous allocations - since the test is supposed to start with -Xms200M and
        // -Xmx200M
        //Rough estimation used only as indicator
        long actualFreeMemory = 200 * 1024 * 1024;


        while (true) {
            System.out.format(runtimeFreeText, formatByte(memoryRuntimeCounter.getFreeMemory()));
            System.out.format(mxBeanFreeText, formatByte(memoryMXBeanCounter.getFreeMemory()));

            System.out.format("Rough estimation of an actual free heap %s\n", formatByte(actualFreeMemory));

            System.out.println("Trying to allocate " + formatByte(maxByteArrayOneRegionSize + 1));

            try {
                allocations.add(new byte[maxByteArrayOneRegionSize + 1]);
                // (maxByteArrayOneRegionSize +1) takes 2 regions, wasting (REGION_SIZE - 1) bytes of heap and
                // taking 2 regions
                actualFreeMemory-= REGION_SIZE * 2;

            } catch (OutOfMemoryError oom) {
                System.out.println("Got OutOfMemoryError despite:");
                System.out.format(runtimeFreeText, formatByte(memoryRuntimeCounter.getFreeMemory()));
                System.out.format(mxBeanFreeText, formatByte(memoryMXBeanCounter.getFreeMemory()));

                System.out.format("And rough estimation of an actual free heap is %s\n", formatByte(actualFreeMemory));

                throw oom;
            }
        }
    }
}
