// Running the test: 
// javac FromCardCacheTest.java 
// /opt/jdk1.8.0_144_fastdebug/bin/java -classpath . -Xms20M -Xmx20M -XX:HeapBaseMinAddress=2199011721216 -XX:+UseG1GC -verbose:gc FromCardCacheTest 

import java.lang.management.GarbageCollectorMXBean; 
import java.lang.management.ManagementFactory; 
import java.lang.reflect.Field; 

import sun.misc.Unsafe; 

public class FromCardCacheTest { 

    private static int nArrays = 24; 
    private static int arraySize = 131068; // object array, a bit less than half of a 1M region 
    private static int byteArraySize = 524270; // byte array, a bit less than half of a 1M region 
    private static final long heapBase = 2199011721216L; // less than 2TB 

    public static void main(String[] args) { 
        while (runTest()) { 
            System.gc(); 
            try { 
                Thread.sleep(2000); 
            } catch(Exception e) {} 
        } 
    } 

    public static boolean runTest() { 
        GarbageCollectorMXBean oldGc = null; 
        GarbageCollectorMXBean youngGc = null; 
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) { 
            if (gc.getName().equals("G1 Old Generation")) oldGc = gc; 
            if (gc.getName().equals("G1 Young Generation")) youngGc = gc; 
        } 
        if (oldGc == null) { 
            System.out.println("G1 oldGc = null"); 
            return false; 
        } 
        if (youngGc == null) { 
            System.out.println("G1 youngGc = null"); 
            return false; 
        } 
        long oldGcCount = oldGc.getCollectionCount(); 

        System.out.println("Running test"); 
        Object[][] arrays = new Object[nArrays][]; 
        for (int i = 0; i < nArrays; i++) { 
            arrays[i] = new Object[arraySize]; 
        } 

        Object[][] arrayWithCardMinus1 = new Object[1][]; 
        arrayWithCardMinus1[0] = findArray(arrays, true, true); 
        Object[] otherArray = findArray(arrays, false, false); 

        if (arrayWithCardMinus1[0] == null) { 
            System.out.println("Array with card -1 not found. Trying again."); 
            return true; 
        } else { 
            System.out.println("Found an array with card -1."); 
        } 

        if (otherArray == null) { 
            System.out.println("otherArray = null."); 
            return true; 
        } 

        System.out.println("Modifying the last card in the array..."); 
        final int modifyEntries = 10; 

        final long youngGcCount = youngGc.getCollectionCount(); 
        for (int i = 0; i < modifyEntries; i++) { 
           if (oldGc.getCollectionCount() > oldGcCount) { 
               System.out.println("Old gc done. Trying again."); 
               return true; 
           } 

           final int arrayIndex = arraySize - i - 1; 
           arrayWithCardMinus1[0][arrayIndex] = new Object[]{new byte[byteArraySize]}; 

           final int card = getCard32Bit(getAddress(arrayWithCardMinus1[0]) + 4 * arrayIndex); 
           System.out.println(arrayIndex + " " + String.format("0x%016x", getAddress(arrayWithCardMinus1[0]) + 16 + 4 * arrayIndex) + " set to " + 
             String.format("0x%016x", getAddress(arrayWithCardMinus1[0][arrayIndex])) + ", card: " + card); 

           if (card != -1) { 
               arrayWithCardMinus1[0] = findArray(arrays, true, true); 
               if (arrayWithCardMinus1[0] == null) { 
                   System.out.println("Array with card -1 not found. Trying again."); 
                   return true; 
               } 
           } 

           try { 
                Thread.sleep(100); 
           } catch(Exception e) {} 
        } 


        System.out.println("Producting garbage to run gc."); 

        final long t1 = System.currentTimeMillis(); 
        oldGcCount = oldGc.getCollectionCount(); 
        while (System.currentTimeMillis() - t1 < 60000) { 

           if (oldGc.getCollectionCount() > oldGcCount) { 
               System.out.println("Old gc done. Trying again."); 
               return true; 
           } 

           otherArray[0] = new byte[30000]; 
           otherArray[0] = null; 

           try { 
                Thread.sleep(100); 
           } catch(Exception e) {} 
        } 

        System.out.println("The crash didn't reproduce. Trying again."); 
        return true; 
    } 

    private static Object[] findArray(Object[][] arrays, boolean withCardMinus1, boolean printAddresses) { 
        for (int i = 0; i < arrays.length; i++) { 
            if (arrays[i] == null) continue; 
            final long startAddress = getAddress(arrays[i]); 
            final long lastCardAddress = startAddress + 4 * arrays[i].length; 
            final int card = getCard32Bit(lastCardAddress); 
            if (printAddresses) { 
                System.out.println("array " + i + ", startAddress: " + String.format("0x%016x", startAddress) + 
                    ", lastCardAddress: " + String.format("0x%016x", lastCardAddress) + ", card: " + card); 
            } 

            if ((withCardMinus1 && card == -1) || (!withCardMinus1 && card != -1)) { 
                Object[] foundArray = arrays[i]; 
                arrays[i] = null; 
                return foundArray; 
            } 
        } 
        return null; 
    } 


    // Unsafe is only used for reading object addresses. 
    private static final Unsafe UNSAFE; 
    static { 
        try { 
            Field e = Unsafe.class.getDeclaredField("theUnsafe"); 
            e.setAccessible(true); 
            UNSAFE = (Unsafe)e.get((Object)null); 
        } catch (IllegalAccessException | NoSuchFieldException var1) { 
            throw new Error(var1); 
        } 
    } 
    static Object[] addrArray = new Object[]{null}; 
    static final long baseOffset = UNSAFE.arrayBaseOffset(Object[].class); 
    static final int pageSize = UNSAFE.pageSize(); 
    public static long getAddress(Object o) { 
        addrArray[0] = o; 
        final long address = (UNSAFE.getInt(addrArray, baseOffset) << 3) + heapBase - pageSize + 1024*1024L; 
        addrArray[0] = null; 
        return address; 
    } 

    public static int getCard32Bit(final long address) { 
        final int card = (int)(address >> 9); 
        return card; 
    } 
} 