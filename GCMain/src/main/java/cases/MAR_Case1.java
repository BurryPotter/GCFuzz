package cases;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.ServiceConfigurationError;

public class MAR_Case1 {
    private int[][] field_large = new int[512][1024];
    protected volatile Object field_small = new Object();
    public static void main(String[] args) {
        MAR_Case1 local_small = new MAR_Case1();
        Object[][] local_large = new Object[1024][128];
    }
}
