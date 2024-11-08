
package test.gc;

// Tests parallelization of StealMarkingTask

public class TestStealMarkingTask {

    private static final long MB = 1 << 20;
    private int listLength;
    private int sublistLength;
    private int empty2nonemptySublistRation;
    private int iterations;
    private ListOfLists listList;

    public static void main(String[] args) {
        TestStealMarkingTask test = new TestStealMarkingTask();
        test.parseCmdLine(args);
        test.buildListOfLists();
        test.run();
    }

    private void run() {
        while (iterations-- > 0) {
            message("Calling System.gc()");
            long start = System.currentTimeMillis();
            System.gc();
            long end = System.currentTimeMillis();
            heapStats();
            message("Full GC duration: " + (end - start) + " ms");
        }
    }

    public void buildListOfLists() {
        try {
            message("Building list of length " + listLength + " holding sub-lists of length " + sublistLength);
            heapStats();
            ListOfLists newList = null;
            for (int i = 0; i < listLength; i++) {
                newList = new ListOfLists(newList, (i % empty2nonemptySublistRation) == 0 ? sublistLength : 0);
            }
            listList = newList;
            message("done");
            heapStats();
        } catch (OutOfMemoryError oom) {
            listList = null;
            message("OutOfMemory: please reduce list lengths");
            System.exit(1);
        }
    }

    private void heapStats() {
        long free = Runtime.getRuntime().freeMemory();
        long max = Runtime.getRuntime().maxMemory();
        float freePercentage = round((float) free / (float) max * 100);
        message("Heap stats: free:" + free / MB + " MB (" + freePercentage + " %)" + "  max:" + max / MB + " MB");
    }

    private float round(float f) {
        return Math.round(f * 10) / 10f;
    }

    private void parseCmdLine(String[] args) {
        if (args.length != 4) {
            message();
            message("usage: java " + TestStealMarkingTask.class.getName()
                    + " <test iterations> <list length, e.g. 10000> <sublist length, e.g. 10000> <ratio #empty : #non-empty sublists, e.g. 2>");
            message();
            System.exit(1);
        }
        int i = 0;
        iterations = Integer.parseInt(args[i++]);
        listLength = Integer.parseInt(args[i++]);
        sublistLength = Integer.parseInt(args[i++]);
        empty2nonemptySublistRation = Integer.parseInt(args[i++]);
    }

    private void message(String msg) {
        System.out.println(msg);
    }

    private void message() {
        System.out.println();
    }

    public static class ListOfLists {

        @SuppressWarnings("unused")
        private ListOfLists tail;
        private ListOfLists subList;

        public ListOfLists(ListOfLists tail, int subLength) {
            this.tail = tail;
            ListOfLists newList = null;
            for (int i = 0; i < subLength; i++) {
                newList = new ListOfLists(newList);
            }
            subList = newList;
        }

        public ListOfLists(ListOfLists tail) {
            this.tail = tail;
        }
    }
}

