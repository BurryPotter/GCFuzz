package jfr_serial;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import jdk.jfr.Recording;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordedObject;
import jdk.jfr.consumer.RecordingFile;

/**
 *
 * @author Dmitry Fazunenko
 */
public class FirstGC {
    MemoryPoolMXBean pool = getMemoryPool();
    public static void main(String args[]) throws Exception {
        new FirstGC().go();
    }
    public void go() throws Exception {
        
        Recording recording = new Recording();
        recording.enable("com.oracle.jdk.MetaspaceSummary");
        recording.start();
        
        System.out.println("%%%% Loading classes");
        long p_used = 0;
        long used = 1;
        while (p_used < used) {
            //System.out.println("% p_used: " + p_used);
            //System.out.println("%   used: " + used);
            loadNewClass();
            p_used = used;
            used = getUsed();
        }
        Thread.sleep(200); // to not lose events
        recording.stop();
        List<RecordedEvent> events = new ArrayList<>();
        Path f = Paths.get("meta.jfr");
        recording.dump(f);
        RecordingFile.readAllEvents(f).stream().forEach(events::add);
        events.stream().sorted(GC_ID_COMPARATOR).forEach(this::printMetaspaceSummary);
    }
    
    void printMetaspaceSummary(RecordedEvent e) {
        int id = e.getValue("gcId");
        String when = e.getValue("when");
        long gcThreshold  = e.getValue("gcThreshold");
        long committed = ((RecordedObject)(e.getValue("metaspace"))).getValue("committed");
        //System.out.println("#" + id + "  " + when + "  c=" + committed + " t=" + gcThreshold);
        System.out.printf("#%1d  when=%-10s committed=%-15d  gcThreshold=%-15d\n", id, when, committed, gcThreshold);
    }

    public long getUsed() {
        return pool.getUsage().getUsed();
    }
    
    private static MemoryPoolMXBean getMemoryPool() {
        List<MemoryPoolMXBean> pools = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean pool : pools) {
            if (pool.getName().equals("Metaspace")) {
                return pool;
            }
        }
        return null;
    }
    
    private static int counter = 0;
    protected void loadNewClass() {
        try {
            String jarUrl = "file:" + counter + ".jar";
            counter++;
            URL[] urls = new URL[]{new URL(jarUrl)};
            URLClassLoader cl = new URLClassLoader(urls);
            Foo foo = (Foo) Proxy.newProxyInstance(cl,
                    new Class[]{Foo.class},
                    new FirstGC.FooInvocationHandler(new FooBar()));
            if (foo == null) {
                counter--; // will never happen
            }
        } catch (java.net.MalformedURLException badThing) {
            // should never occur
            System.err.println("Unexpeted error: " + badThing);
            throw new RuntimeException(badThing);
        }
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
    public static final Comparator<RecordedEvent> GC_ID_COMPARATOR = new Comparator<RecordedEvent>() {
        @Override
        public int compare(RecordedEvent o1, RecordedEvent o2) {
            int id1 = o1.getValue("gcId");
            int id2 = o2.getValue("gcId");
            if (id1 == id2) {
                String w1 = o1.getValue("when");
                String w2 = o2.getValue("when");
                if (w1 != null && w2 != null) {
                    return w2.compareTo(w1);
                }
            }
            return id1 - id2;
        }        
    };
}

