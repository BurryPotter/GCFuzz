// Run with:
// java --add-exports java.base/jdk.internal.misc=ALL-UNNAMED -Xbatch -XX:+UseZGC Reproducer.java

import jdk.internal.misc.Unsafe;

public class Reproducer {

    protected static final Unsafe UNSAFE = Unsafe.getUnsafe();

    static final class Aux {
        Aux next;
    }

    public static void test(Aux foo) {
        long offset = UNSAFE.objectFieldOffset(Aux.class, "next");
        UNSAFE.compareAndSetReference(foo, offset, foo, foo);
    }

    public static void main(String[] args) {
        Aux foo = new Aux();
        for (int i = 0; i < 10_000; i++) {
            test(foo);
        }
    }

}
