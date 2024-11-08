import java.util.*; 

public class CatchOutOfMemoryErrorTest { 
    public static void main(String[] args) { 
        int initialCapacity = 10_000_000; 
        List<Object> stuff = new ArrayList<Object>(initialCapacity); 
        while(true) { 
            long availableMemory = getAvailableMemory(); 
            try { 
                for(int i=0;i<100;i++) { 
                    stuff.add(new byte[10_000]); 
                } 
            } catch (OutOfMemoryError e) { 
                long availableNow = getAvailableMemory(); 
                long freeMemory = Runtime.getRuntime().freeMemory(); 
                int blocksAllocated = stuff.size(); 
                stuff = null; // So that we GC now and the following methods don't throw OutOfMemoryError 
                if (availableMemory>2_000_000) { 
                    System.err.printf("Got out of memory trying to allocate memory when availableMemory was %,d and is now %,d. Free=%,d. Allocated blocks=%,d\n",availableMemory, availableNow, freeMemory, blocksAllocated); 
                    break; 
                } 
                System.out.printf("Finished when availableMemory was %,d and is now %,d. Free=%,d. Allocated blocks=%,d\n",availableMemory, availableNow, freeMemory, blocksAllocated); 
                break; 
            } 
            long usedMemory = getUsedMemory(); 
            availableMemory = getAvailableMemory(); 
            if (availableMemory>1_500_000) // Don't do this when low on memory so that the println doesn't throw OutOfMemoryError 
                System.out.printf("Used=%,d available=%,d of %,d size=%,d max=%,d total=%,d free=%,d\n", usedMemory, availableMemory, usedMemory +availableMemory, stuff.size(),Runtime.getRuntime().maxMemory(),Runtime.getRuntime().totalMemory(),Runtime.getRuntime().freeMemory()); 
            if (stuff.size()>initialCapacity) { 
                System.err.printf("initial capacity should have been higher\n"); 
                break; 
            } 
        } 
    } 

    private static long getAvailableMemory() { 
        return Runtime.getRuntime().maxMemory() - getUsedMemory(); 
    } 

    private static long getUsedMemory() { 
        Runtime runtime = Runtime.getRuntime(); 
        return runtime.totalMemory() - runtime.freeMemory(); 
    } 
} 
