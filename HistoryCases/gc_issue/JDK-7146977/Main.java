package driver;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    private enum RefType {
        ClassLoader {
            @Override
            public java.lang.Object get(URLClassLoader child,
                                        java.lang.Class test, java.lang.Object o) {
                return child;
            }
        }, Class {
            @Override
            public java.lang.Object get(URLClassLoader child,
                                        java.lang.Class test, java.lang.Object o) {
                return test;
            }
        }, Object {
            @Override
            public java.lang.Object get(URLClassLoader child,
                                        java.lang.Class test, java.lang.Object o) {
                return o;
            }
        };

        abstract public Object get(URLClassLoader child, Class test, Object o);
    }

    public static void main(String[] args) throws Exception {
        RefType refType = null;
        try {
            refType = RefType.valueOf(args[0]);

        } catch (Exception e) {
            System.err.println("Expected argument: 'ClassLoader' or 'Class' or 'Object'");
            return;
        }


        URLClassLoader parent = (URLClassLoader) Main.class.getClassLoader();
        URL testCode = null;
        for (URL u : parent.getURLs()) {
            String uu = u.toString();
            if (uu.contains("memLeakTestDriver")) {
                testCode = new URL(uu.replace("memLeakTestDriver", "memLeakTest"));
            }
        }
        URLClassLoader child = URLClassLoader.newInstance(new URL[]{testCode}, parent);
        Class test = child.loadClass("test.Test");
        Object o = test.newInstance();
        Map<ClassLoader, Reference<Class>> weak = new WeakHashMap<ClassLoader, Reference<Class>>();
        weak.put(child, new WeakReference(test));

        o = refType.get(child, test, o);

        ReferenceQueue queue = new ReferenceQueue();
        PhantomReference ref = new PhantomReference(
                o, // can be changed to 'test' or 'child' and problem goes away
                queue);
        AtomicInteger problemCount = (AtomicInteger) test.getDeclaredMethod("getProblemCount").invoke(null);
        o = null;
        test = null;
        child = null;
        int cnt = 0;
        while (problemCount.get() != 0 && cnt < 50) {
// queue.poll()==null && cnt < 30) {
            System.gc();
            System.err.println("GC " + cnt + " " + problemCount.get());
            cnt++;
        }
        ref.toString();

    }

}
