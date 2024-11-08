
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class BigRamTester {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
    
    private static final long RESERVED_SPACE = 10L * 1024L * 1024L * 1024L; // 10 GB

    private static final int MAX_LIST_SIZE = 30;

    private static final int APPROX_BYTES_PER_RECORD = 1000;

    private static final int TARGET_HIT_RATE = 90;

    private static final Random RANDOM = new Random();

    private static long startTime;

    private LinkedHashMap<Integer, List<Integer>> storage;

    private long totalSum = 0;

    private final Statistics stats = new Statistics();

    public static void main(String[] args) {
        startTime = System.currentTimeMillis();
        try {
            new BigRamTester().go();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void go() throws InterruptedException {
        int keys = createData();
        accessCache(keys);
        System.out.println(totalSum);
    }

    private void accessCache(int keys) throws InterruptedException {

        int dataRange = ((keys - 1) / TARGET_HIT_RATE) * 100;
        
        while (true) {

            long startTime = System.nanoTime();
            
            int key = RANDOM.nextInt(dataRange + 1);
            
            List<Integer> data = storage.get(key);
            if (data == null) {
                data = createIntegers();
                storage.put(key, data);
                Iterator<Entry<Integer, List<Integer>>> iterator = storage.entrySet().iterator();
                iterator.next();
                iterator.remove();
                stats.registerMiss();
            } else {
                stats.registerHit();
            }
            
            long endTime = System.nanoTime();
            stats.recordTimeTaken(endTime - startTime);            

            int sum = 0;
            for (Integer dataItem : data) {
                sum += dataItem;
            }

            totalSum += sum;

        }

    }

    private int createData() {

        // Try and estimate the number of records that we will store to avoid resizing the hashmap many times.
        // This is based on the fixed APPROX_BYTES_PER_RECORD value, which is based on observing the amount of memory used
        // with a fixed value of MAX_LIST_SIZE
        int estimatedNumberOfRecords = (int) ((Runtime.getRuntime().freeMemory() - RESERVED_SPACE) / APPROX_BYTES_PER_RECORD) + 1000;
        System.out.println("Creating storage map of size = " + estimatedNumberOfRecords);
        storage = new LinkedHashMap<Integer, List<Integer>>(estimatedNumberOfRecords, 0.75f, true);

        long freeMemoryAtStart = Runtime.getRuntime().freeMemory();

        int key = 0;
        while (key < Integer.MAX_VALUE) {
            if (Runtime.getRuntime().freeMemory() < RESERVED_SPACE) {
                System.out.println("Garbage collecting...");
                System.gc();
                System.out.println("Finished; memory remaining: " + Runtime.getRuntime().freeMemory() + " bytes");
                if (Runtime.getRuntime().freeMemory() < RESERVED_SPACE) {
                    break;
                }
            }
            List<Integer> integers = createIntegers();
            storage.put(key++, integers);
            if (key % 100000 == 0) {
                printMemoryUsed(key, freeMemoryAtStart);
            }
        }

        printMemoryUsed(key, freeMemoryAtStart);

        System.out.println("DONE!");

        return key;

    }

    private void printMemoryUsed(long key, long freeMemoryAtStart) {
        long memoryUsed = freeMemoryAtStart - Runtime.getRuntime().freeMemory();
        long uptime = System.currentTimeMillis() - startTime;
        System.out.println(getDate() + "Created " + key + " keys, using " + (memoryUsed / 1024 / 1024) + "MB; that's " + (memoryUsed / key)
                + " bytes per record; free memory = " + (Runtime.getRuntime().freeMemory() / 1024 / 1024) + "MB" + "; reserved memory = " + RESERVED_SPACE
                + " bytes; uptime = " + (uptime / 1000) + " seconds");
    }

    private List<Integer> createIntegers() {
        int listSize = RANDOM.nextInt(MAX_LIST_SIZE);
        LinkedList<Integer> integerList = new LinkedList<Integer>();
        for (int i = 0; i < listSize; i++) {
            integerList.add(RANDOM.nextInt());
        }
        return integerList;
    }

    private static final class Statistics {

        private final AtomicLong hits = new AtomicLong();
        private final AtomicLong misses = new AtomicLong();
        
        private long maxTimeTakenNanos = 0;

        public Statistics() {
            Thread thread = new Thread() {

                @Override
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        synchronized (Statistics.this) {
                            long uptime = System.currentTimeMillis() - startTime;
                            if (hits.get() > 0 && misses.get() > 0) {
                                System.out.println(getDate() + "Hit rate = " + ((double)hits.get() / ((double)misses.get() + (double)hits.get())) 
                                                + "; total hits = " + hits.get()
                                                + "; total misses = " + misses.get() 
                                                + "; total reads = " + (hits.get() + misses.get())
                                                + "; free memory = " + (Runtime.getRuntime().freeMemory() / 1024 / 1024) + "MB"
                                                + "; max time taken = " + ((double)maxTimeTakenNanos / 1000 / 1000) + "ms"
                                        		+ "; uptime = " + (uptime / 1000) + " seconds");
                            }
                            hits.set(0);
                            misses.set(0);
                        }
                    }
                }
                
            };
            thread.start();
        }

        public synchronized void registerHit() {
            hits.getAndIncrement();
        }

        public synchronized void registerMiss() {
            misses.getAndIncrement();
        }
        
        public synchronized void recordTimeTaken(long timeTakenNanos) {
            if (timeTakenNanos > maxTimeTakenNanos) {
                maxTimeTakenNanos = timeTakenNanos;
            }
        }

    }
    
    private static synchronized String getDate() {
        return DATE_FORMAT.format(new Date(System.currentTimeMillis())) + " - ";
    }

}

