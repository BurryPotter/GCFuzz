public class Case {
    static class A {
        int a = 10;
    }

    static Object o;

    static void f() {

    }
    Object o1;
    public static void main(String[] args) {
        Case.o = new Object();
        Case c = new Case();
        Case.f();
        Case.o.hashCode();
        c.o1 = new Object();
        int[] arr = new int[10];
        if (arr[4] > 0) {
            arr = new int[11];
            arr[1] = 10;
        }

    }
}