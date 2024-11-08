

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;


public class GCDownTest {

    public static void main(String args[]) {
        System.exit(new GCDownTest().test());
    }

    private MemoryPoolMXBean pool = getMemoryPool("Metaspace");

    public int test() {
        long used_p = 0;
        long committed_p = 0;
        long used = getUsed();
        long committed = getCommitted();
        System.out.println("Starting...");
        while (used > used_p) {
            loadNewClass();
            used_p = used;
            committed_p = committed;
            used = getUsed();
            committed = getCommitted();
        }
        System.out.println("The first GC!");
        System.out.println("  Used:      " + used_p + " --> " + used);
        System.out.println("  Committed: " + committed_p + " --> " + committed);

        long used1 = used_p;
        long committed1 = committed_p;

        used_p = 0;
        committed_p = 0;
        while (used > used_p) {
            loadNewClass();
            used_p = used;
            committed_p = committed;
            used = getUsed();
            committed = getCommitted();
        }
        System.out.println("The second GC!");
        System.out.println("  Used:      " + used_p + " -- >" + used);
        System.out.println("  Committed: " + committed_p + " -- >" + committed);

        if (used1 < used_p) {
            System.out.println("Test failed: " + used1 + " less than " + used_p);
            return 2;
        } else {
            System.out.println("Test passed");
            return 0;
        }


    }


    // just to have a unique URL
    private int counter = 0;
    
    protected Foo loadNewClass() {
        try {
            String jarUrl = "file://" + counter + ".jar";
            counter++;
            URL[] urls = new URL[]{new URL(jarUrl)};
            URLClassLoader cl = new URLClassLoader(urls);
            return (Foo) Proxy.newProxyInstance(cl,
                    new Class[]{Foo.class},
                    new FooInvocationHandler(new FooBar()));

        } catch (java.net.MalformedURLException badThing) {
            // should never occur
            System.err.println("Unexpeted error: " + badThing);
            throw new RuntimeException(badThing);
        }

    }

    /**
     * @return amount of used memory
     */
    public long getUsed() {
        return pool.getUsage().getUsed();
    }

    /**
     * @return amount of committed memory
     */
    public long getCommitted() {
        return pool.getUsage().getCommitted();
    }

    private static MemoryPoolMXBean getMemoryPool(String name) {
        List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean pool : pools) {
            if (pool.getName().equals(name)) {
                return pool;
            }
        }
        return null;
    }


    public static interface Foo {
    }

    public static class FooBar implements Foo {
    }

    class FooInvocationHandler implements InvocationHandler {
        private final Foo foo;

        FooInvocationHandler(Foo foo) {
            this.foo = foo;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return method.invoke(foo, args);
        }
    }

}

