public class GCWaitGC {
public static void main(String[] arg) {
System.gc();
System.out.print("Waiting ...");
try {
int read = System.in.read();
} catch (java.io.IOException ioe) {
System.err.println("Oops: " + ioe.toString());
}
System.gc();
}
    }