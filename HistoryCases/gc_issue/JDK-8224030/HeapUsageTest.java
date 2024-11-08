import java.lang.management.ManagementFactory; 
import java.lang.management.MemoryPoolMXBean; 
import java.lang.management.MemoryUsage; 
import java.util.HashSet; 

public class HeapUsageTest { 

public HeapUsageTest() 
{ 
        System.out.println("Before "); 
        printHeapUsage(); 
        HashSet<String> set = new HashSet<>(); 
        for (int i = 0; i<10000000; i++) { 
            set.add(String.valueOf(i)); 
        } 
        System.out.println("After"); 
        printHeapUsage(); 

} 
    public static void main(String[] args) throws InterruptedException { 
    	new HeapUsageTest(); 
        Thread.sleep(1000000); 
    } 

    private void printHeapUsage() { 
        for (MemoryPoolMXBean mpbean : 
             ManagementFactory.getMemoryPoolMXBeans()) { 
            MemoryUsage usage = mpbean.getCollectionUsage(); 
            if (usage != null) { 
                long max = usage.getMax(); 
                long used = usage.getUsed(); 
                System.out.println(mpbean.getName() + " Used " + used + ", Max " + max); 
            } 
        } 

    } 
} 
