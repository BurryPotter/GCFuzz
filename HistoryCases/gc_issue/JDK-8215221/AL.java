import java.util.*;

public class AL {
    static List<Object> l;
    public static void main(String... args) throws Throwable {
        l = new ArrayList<>();
        for (int c = 0; c < 100_000_000; c++) {
            l.add(new Object());
        }
        System.out.println(l.hashCode());
System.in.read();
    }
}