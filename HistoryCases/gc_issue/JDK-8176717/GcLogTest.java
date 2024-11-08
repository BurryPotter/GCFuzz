import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class GcLogTest { 

    // VM options: 
    // -verbose:gc -Xloggc:D:/2015/gc.log 

    public static void main(String[] args) throws InterruptedException, IOException { 
        // Start sub-process, but don't wait for it to exit 
        List<String> command = Arrays.asList("D:\\2015\\wait_test.bat", "180"); 
        ProcessBuilder pb = new ProcessBuilder(command); 
        Process childProcess = pb.start(); 

        // Wait 30 seconds 
        Thread.sleep(30 * 1000L); 

        // Exit without killing process 
    } 
}