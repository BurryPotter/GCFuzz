import java.util.HashSet;
import java.util.Set;
 
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
 
// java -Xmx64m -Xms64m -XX:+PrintAdaptiveSizePolicy -XX:+PrintGC -XX:+PrintGCDetails
// -XX:+PrintGCTimeStamps -XX:+PrintHeapAtGC -XX:+UseG1GC GcTest
//
// Use jconsole to attach to the GcTest process, click on MBeans,
// java.lang, MemoryPool, G1 Old Gen, CollectionUsage.
// 'used' will be zero.

public class GcTest {
 
    public static void main(String[] args) {
        Set<String> persistentStrings = new HashSet<>(1000);
        for (int i = 0; i < 1000; i++) {
            persistentStrings.add(RandomStringUtils.random(10240));
        }
        while (true) {
            Set<String> strings = new HashSet<>(1000);
                //            for (int i = 0; i < 200; i++) {
                //            for (int i = 0; i < 50; i++) {
            for (int i = 0; i < 1000; i++) {
                strings.add(RandomStringUtils.random(10240));
            }
        }
    }
}
