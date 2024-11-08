import java.util.HashMap;
import java.util.Map;

import java.util.zip.Deflater;

public class JNICriticalStressTest2 {
    static private final int LARGE_MAP_SIZE = 64 * 1024;

    static private final int MAP_ARRAY_LENGTH = 4;
    static private final int MAP_SIZE = 1024;

    static private final int BYTE_ARRAY_LENGTH = 128 * 1024;

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

    static private class JNICriticalWorker implements Runnable {
        private int count;

        private void doStep() {
            byte[] inputArray = new byte[BYTE_ARRAY_LENGTH];
            for (int i = 0; i < inputArray.length; i += 1) {
                inputArray[i] = (byte) (count + i);
            }

            Deflater deflater = new Deflater();
            deflater.setInput(inputArray);
            deflater.finish();

            byte[] outputArray = new byte[2 * inputArray.length];
            deflater.deflate(outputArray);

            count += 1;
        }

        public void run() {
            while (true) {
                doStep();
            }
        }
    }

    static public Map<Integer,String> largeMap;

    static public void main(String args[]) {
        if (args.length < 3) {
            println("usage: JNICriticalStressTest2 <duration sec> <alloc threads> <jni critical threads>");
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
