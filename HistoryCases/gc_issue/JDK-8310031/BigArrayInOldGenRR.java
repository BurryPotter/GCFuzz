
// java
// -XX:+UseParallelGC
// -Xlog:gc
// -Xms2G
// -Xmx2G
// -Xmn1G
// -XX:ParallelGCThreads=10
// BigArrayInOldGenRR

// Young GC pauses increase when adding parallel gc threads

public class BigArrayInOldGenRR {

    public static final int BIG_ARR_LEN = 8 * 1024 * 1024;
    public static final int DUPS = 16;
    public static final Object[] BIG_OLD_ARRAY = new Object[BIG_ARR_LEN];

    public static void main(String[] args) {
        System.out.println();
        System.out.println("BIG_ARR_LEN:" + BIG_ARR_LEN + " DUPS:" + DUPS);
        System.out.println();
        System.gc();
        while (true) {
            allocateNewElements();
        }
    }

    static void allocateNewElements() {
        final Object[] big = BIG_OLD_ARRAY;

        for (int i = 0; i < BIG_ARR_LEN; i += DUPS) {
            big[i] = new Object();

            for (int j = i + 1; j < Math.min(BIG_ARR_LEN , i + DUPS); ++j) {
                big[j] = big[i];
            }
        }
    }
}
