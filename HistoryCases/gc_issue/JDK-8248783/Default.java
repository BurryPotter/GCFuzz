 import java.util.TreeMap;
import java.util.Map;
import java.util.LinkedList;

class HumongousAllocationTest {
    private static Map<String, String> longLiveObjectsMap = new TreeMap<>();
    private static LinkedList<Object> oldGarbageList = new LinkedList<Object>();

    private static void allocateLiveObjects() {
        for (int i = 0; i < 2000000; i++) {
            longLiveObjectsMap.put("key" + i, "value" + i);
        }
    }

    private static byte[] array;

    private static void allocateGarbage() {
        for (int i = 0; i < 1000000; i++) {
            array = new byte[512 * 1024];
            oldGarbageList.add(new byte[10 * 1024]);
            for (int j = 0; j < 50; j++) {
                array = new byte[10 * 1024];
            }
            if (oldGarbageList.size() > 5000) {
                oldGarbageList.removeFirst();
            }
            
        }
    }

    public static void main(String[] args) {
        allocateLiveObjects();
        allocateGarbage();
    }
} 
