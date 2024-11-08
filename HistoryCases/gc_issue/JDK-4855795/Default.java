class HeapTest {
    static final private int KB = 1024;
    static final private int MB = 1024 * 1024;

    public static void main(String[] args) {
        Runtime rt = Runtime.getRuntime();

        java.util.Vector v = new java.util.Vector(50000);


        int MAXITER = 2000000;
        int BLOCKSIZE = 4 * KB;
        int actBlockSize = BLOCKSIZE;

        // determine actual space allocated per block including overhead
        long preFree = rt.freeMemory();
        byte[] ba = new byte[BLOCKSIZE];
        long curFree = rt.freeMemory();
        ba = null;
        actBlockSize = (int) (preFree - curFree);
        System.out.println("requested allocation size = " + BLOCKSIZE
                + " actual size = " + actBlockSize + "bytes");

        System.gc();
        System.out.println("--- starting allocations -------");

        int gCount = 0;

        for (int i = 1; i < MAXITER; i++) {
            preFree = rt.freeMemory();

            if (preFree < 2 * BLOCKSIZE) {
                System.out.println(i + ": may cause allocation failure. free heap is" + preFree);
                // System.out.println( i + ": forcing GC" );
                // System.gc();
            }

            try {
                ba = new byte[BLOCKSIZE];
                v.add(ba);
                ba = null;
            } catch (java.lang.OutOfMemoryError x) {
                System.out.println("");
                System.out.println(i + ": *** Out of Memory Exception caught * **(expected " + (gCount * BLOCKSIZE / KB) + " K garbage)");
                System.out.println(" free heap is now " + rt.freeMemory() + "bytes");
                System.out.println(" total blocks allocated = " + v.size());
                System.out.println("");
            }

            curFree = rt.freeMemory();

            long freed = (curFree - preFree) + actBlockSize;
            if (freed > 0) {
                System.out.println(i + ": GC freed " + freed / KB + "K"
                        + " (" + (gCount * actBlockSize / KB) + "K expected)");
                gCount = 0;
            }

            // drop references to the most recently allocated 2 blocks if free             memory is tight
            if (rt.freeMemory() < 1 * MB && v.size() > 2) {
                v.removeElementAt(v.size() - 1);
                v.removeElementAt(v.size() - 1);
                gCount += 2;
            }
        }
    }

}