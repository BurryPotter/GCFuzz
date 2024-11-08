import java.lang.management.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

public class CMSGCALot
{
    private static ArrayList arrayList;
    private static TmpClass[] tmp = new TmpClass[1024];

    public static void main(String args[]) throws Exception {
        // Causes lots of CMSGC
        int MX = Integer.parseInt(args[0]);
        consumeHeap(MX);

        // execute jstat -gctuil
        String PID = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        String JAVA_HOME = System.getProperty("java.home");
        ProcessBuilder pb = new ProcessBuilder(
            (JAVA_HOME + File.separator + "bin" + File.separator + "jstat"),
             "-gcutil",
             PID
        );
        Process process = pb.start();
        process.waitFor();

        // print the result of jstat
        InputStream is = process.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
    }

    static void consumeHeap(int MX) throws Exception {

        Random rand = new Random();
        arrayList = new ArrayList();

        int notFreed = (int)(MX * 0.65);
        // Use 0.65% of -Xmx which is not freed 
        for (int i = 0 ; i < (1024 * notFreed) ; i++) { // Allocate 1K * 1024 * (MX*0.65) bytes
            byte[] b = new byte[1024];                  // For instance, if MX=64mx,
            arrayList.add(b);                           // consume 1K * 1024 * (64*0.65) = 41.6MB
        }

        // Use 0.35% of -Xmx which is freed 
        for (int i = 0 ; i < 1000 ; i++) { // new 1000 * 100000 * 1KB = 100GB which is freed by GC
            Thread.sleep(10);
            for (int j = 0 ; j < 100000 ; j++) {
                tmp[rand.nextInt(1024)] = new TmpClass(1);  // The alive objects are at most 1KB * 1024 bytes
            }                                               // and the other objects are freed by GC.
        }
    }

    static class TmpClass
    {
        ArrayList arrayList;
        TmpClass(int n) {
            arrayList = new ArrayList();
            for (int i = 0 ; i < n ; ++i) {
                byte[] b = new byte[1024];    // new 1KB
                arrayList.add(b);
            }
        }
    }
}
