import java.util.concurrent.locks.LockSupport;
import java.util.UUID;
public class StringInterner {
    public static volatile String lastString;

    public static void main(String[] args) {
        for (int iterations = 0; iterations < 40;) {
            String baseName = UUID.randomUUID().toString();
            for (int i = 0; i < 1_000_000; i++) {
                lastString = (baseName + i).intern();
            }
            if (++iterations % 10 == 0) {
                System.gc();
            }
            LockSupport.parkNanos(500_000_000);
        }
    }
}