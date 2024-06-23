package cases;

public class MAR_Case2 {
    public static void main(String[] args) {
        GCObj l0 = new GCObj(65536);
        GCObj l1 = new GCObj(l0);
        GCObj l2 = new GCObj(l1);
        GCObj l3 = new GCObj(l2);
    }
}
