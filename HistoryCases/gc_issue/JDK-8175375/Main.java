import java.lang.management.ManagementFactory; 
import java.lang.management.MemoryPoolMXBean; 
import java.lang.management.MemoryType; 
import java.util.ArrayList; 
import java.util.List; 

public class Main 
{ 

public static void main(String[] args) throws Exception { 
MemoryPoolMXBean tenuredGenPool = null; 
List<byte[]> data = new ArrayList<>(); 
// heuristic to find the tenured pool (largest heap) as seen on 
// http://www.javaspecialists.eu/archive/Issue092.html 
for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) { 
if (pool.getType() == MemoryType.HEAP && pool.isUsageThresholdSupported()) { 
tenuredGenPool = pool; 
} 
} 
System.out.println(tenuredGenPool.getObjectName()); 
for (int i = 0; i < 5; i++) { 
data.add(new byte[1024 * 1024 * 50]); 
Thread.sleep(100); 
System.gc(); 
System.out.println("Collection usage = " + tenuredGenPool.getCollectionUsage().getUsed() 
+ ", usage = " + tenuredGenPool.getUsage().getUsed()); 
} 
} 
} 