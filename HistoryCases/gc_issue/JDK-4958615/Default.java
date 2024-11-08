import java.util.concurrent.atomic.AtomicLong;

class Leak3 {
    public static class Resource {
public static AtomicLong births = new AtomicLong(0);
public static AtomicLong deaths = new AtomicLong(0);

public Resource() { births.getAndIncrement(); }
protected void finalize() { deaths.getAndIncrement(); }
    }

    public static void main(String[] args) throws Exception {
long loops = 0;
while (true) {
Resource x = new Resource();
if ((++loops % 10) == 0) {
long before = Resource.births.get() - Resource.deaths.get();
System.gc();
System.runFinalization();
Thread.sleep(3000);
long after = Resource.births.get() - Resource.deaths.get();
System.out.println(before + " ==> " + after);
}
}
    }
}