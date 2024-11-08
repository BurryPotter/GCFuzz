// To stress large-array dirty-card scanning with alternating clean and dirty cards.
// This version calculates the stride from the constant CARD_SIZE_BYTES.
//
// java -Xms3g -Xmx3g -XX:+UseParallelGC -XX:ParallelGCThreads=2 card_scan_scarce_2 1000 1
// java -Xms3g -Xmx3g -XX:+UseParallelGC -XX:ParallelGCThreads=2 card_scan_scarce_2 100 10
// java -Xms3g -Xmx3g -XX:+UseParallelGC -XX:ParallelGCThreads=2 card_scan_scarce_2 20 50
// java -Xms3g -Xmx3g -XX:+UseParallelGC -XX:ParallelGCThreads=2 card_scan_scarce_2 10 100

class card_scan_scarce_2 {
    static Object[][] allBigobjs;

    public static final int K = 1024;
    public static final int M = 1024*K;
    public static final int CARD_SIZE_BYTES = 512;
    public static final int CARD_SIZE_WORDS = CARD_SIZE_BYTES / 8;
    public static final int ARR_ELTS_PER_WORD = 2;
    public static final int ARR_ELTS_PER_CARD = CARD_SIZE_WORDS * ARR_ELTS_PER_WORD;

    // chosen to get dirty and clean cards alternatig one by one
    static final int stride = ARR_ELTS_PER_CARD * 2;

    static int[] garbage;

    static final int loop_count = 20;

    public static void main(String[] args) {
        int bigArrLen = Integer.parseInt(args[0]);
        int bigArrCount = Integer.parseInt(args[1]);
        System.out.println("ARR_ELTS_PER_CARD:" + ARR_ELTS_PER_CARD);
        System.out.println("stride:" + stride);
        System.out.println("### bigArrLen:" + bigArrLen + "M   bigArrCount:" + bigArrCount + "  length sum:" + (bigArrLen*bigArrCount) + "M");
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
