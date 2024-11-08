import java.util.HashMap;
import java.util.Map;

public class JNICriticalStressTest {
    static private final int LARGE_MAP_SIZE = 64 * 1024;

    static private final int MAP_ARRAY_LENGTH = 4;
    static private final int MAP_SIZE = 1024;

    static private final int INT_ARRAY_LENGTH = 1024;

    static private void println(String str) { System.out.println(str); }
    static private void println()           { System.out.println();    }
    static private void exit(int code)      { System.exit(code);       }

    static Map<Integer,String> populateMap(int size) {
        Map<Integer,String> map = new HashMap<Integer,String>();
        for (int i = 0; i < size; i += 1) {
            Integer keyInt = new Integer(i);
            String valStr = "value is [" + i + "]";
            map.put(keyInt,valStr);
        }
        return map;
    }

    static private class AllocatingWorker implements Runnable {
        private final Object[] array = new Object[MAP_ARRAY_LENGTH];
        private int arrayIndex = 0;
        
        private void doStep() {
            Map<Integer,String> map = populateMap(MAP_SIZE);
            array[arrayIndex] = map;
            arrayIndex = (arrayIndex + 1) % MAP_ARRAY_LENGTH;
        }

        public void run() {
            while (true) {
                doStep();
            }
        }
    }

    static {
        System.loadLibrary("jnicriticalstresstest");
    }

    static native private void transformArrays(int[] prev, int[] curr);

    static private class JNICriticalWorker implements Runnable {
        private int[] prev;
        private int count;

        int[] newArray() {
            int[] arr = new int[INT_ARRAY_LENGTH];
            for (int i = 0; i < arr.length; i += 1) {
                arr[i] = count + i;
            }            
            count += 1;
            return arr;
        }

        private void doStep() {
            int[] curr = newArray();
            transformArrays(prev, curr);
            prev = curr;
        }

        public void run() {
            while (true) {
                doStep();
            }
        }

        JNICriticalWorker() {
            prev = newArray();
        }
    }

    static public Map<Integer,String> largeMap;

    static public void main(String args[]) {
        if (args.length < 3) {
            println("usage: JNICriticalStressTest <duration sec> <alloc threads> <jni critical threads>");
            exit(-1);
        }

        long durationSec = Long.parseLong(args[0]);
        int allocThreadNum = Integer.parseInt(args[1]);
        int jniCriticalThreadNum = Integer.parseInt(args[2]);

        println("Running for " + durationSec + " secs");

        largeMap = populateMap(LARGE_MAP_SIZE);

        println("Starting " + allocThreadNum + " allocating threads");
        for (int i = 0; i < allocThreadNum; i += 1) {
            new Thread(new AllocatingWorker()).start();
        }

        println("Starting " + jniCriticalThreadNum + " jni critical threads");
        for (int i = 0; i < jniCriticalThreadNum; i += 1) {
            new Thread(new JNICriticalWorker()).start();
        }

        long durationMS = (long) (1000 * durationSec);
        long start = System.currentTimeMillis();
        long now = start;
        long soFar = now - start;
        while (soFar < durationMS) {
            try {
                Thread.sleep(durationMS - soFar);
            } catch (Exception e) {
            }
            now = System.currentTimeMillis();
            soFar = now - start;
        }
        println("Done.");
        exit(0);
    }
}
