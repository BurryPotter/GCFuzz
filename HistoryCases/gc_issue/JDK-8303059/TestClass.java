import sun.hotspot.WhiteBox;

public class TestClass {

    public static void main(String[] args) {
        WhiteBox wb = WhiteBox.getWhiteBox();
        wb.g1StartConcMarkCycle();
    }
}
