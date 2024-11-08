import jdk.test.lib.process.ProcessTools;
import sun.hotspot.WhiteBox;

public class Test {
    public Test() {
    }

    public static void main(String[] var0) throws Exception {
        for(int var2 = 0; var2 < 2; ++var2) {
            WhiteBox var10000 = WhiteBox.getWhiteBox();
            var10000.youngGC();
            var10000.fullGC();
            ProcessBuilder var1 = ProcessTools.createJavaProcessBuilder(new String[]{"-version"});
        }

    }
}