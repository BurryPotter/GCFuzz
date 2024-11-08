public class Case {
    public String str;
    public float[][][] floatArray = new float[32][32][256];
    protected long[][][] longArray = new long[64][256][256];
    private Object[][][] objArray = new Object[256][64][256];

    public Case() {
    }

    public static void main(String[] var0) {
        Case instance = new Case();
        new Case();
        instance.str = new String();
        new Case();
    }

}