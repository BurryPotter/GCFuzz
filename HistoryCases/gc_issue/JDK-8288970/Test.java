import jdk.internal.vm.annotation.Stable;

public class Test {
    static Object[] garbageObjs = new Object[100_000];

    static @Stable Object stableField = null;

    static Object test() {
        return stableField; // Constant folded (FoldStableValues)
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10_000_000; ++i) {
            garbageObjs[i % 100_000] = new Object();
            test();
            stableField = i;
        }
    }
}
