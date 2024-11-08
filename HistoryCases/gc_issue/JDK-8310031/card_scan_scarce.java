// To stress large-array dirty-card scanning with alternating clean and dirty cards
// java -Xms3g -Xmx3g -XX:+UseParallelGC -XX:ParallelGCThreads=2 card_scan_scarce 1000 1
// java -Xms3g -Xmx3g -XX:+UseParallelGC -XX:ParallelGCThreads=2 card_scan_scarce 100 10
// java -Xms3g -Xmx3g -XX:+UseParallelGC -XX:ParallelGCThreads=2 card_scan_scarce 10 100

class card_scan_scarce {
    static Object[][] allBigobjs;

    // chosen to get alternating clean and dirty cards
    static final int stride = 32 * 64;

    static int[] garbage;

    static final int loop_count = 30;

    public static void main(String[] args) {
        int bigArrLen = Integer.parseInt(args[0]);
        int bigArrCount = Integer.parseInt(args[1]);
        System.out.println("### bigArrLen:" + bigArrLen + "M   bigArrCount:" + bigArrCount);
        allBigobjs = new Object[bigArrCount][];
        for (int k = 0; k < bigArrCount; k++) {
            allBigobjs[k] = new Object[bigArrLen * 1024 * 1024 / 4 - 20];
        }
        System.out.println("### System.gc"); // get arrays promoted
        System.gc();
        for (int i = 0; i < loop_count; i++) {
            Object tmp = new Object();
            for (int k = 0; k < allBigobjs.length; k++) {
                Object[] bigobj = allBigobjs[k];
                for (int j = 0; j < bigobj.length; j += stride) {
                    bigobj[j] = tmp;
                }
            }
            int garbage_loop = 8_000 * 32;
            for (int j = 0; j < garbage_loop; ++j) {
                garbage = new int[100];
            }
        }
    }
}
