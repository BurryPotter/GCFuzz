
import com.sun.management.HotSpotDiagnosticMXBean;
import java.lang.management.ManagementFactory;


public class LogTest {
    public static void main(String[] args) {
        setLogging(args.length == 0);
        System.gc();
    }

    private static void setLogging(boolean v) {
        HotSpotDiagnosticMXBean HS_MX_BEAN = ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
        HS_MX_BEAN.setVMOption("PrintGC", "" + v);      
        HS_MX_BEAN.setVMOption("PrintGCDetails", "" + v);      
        HS_MX_BEAN.setVMOption("PrintGCTimeStamps", "" + v); 
        System.out.println("---------------------- ");
        System.out.println("PrintGC " + HS_MX_BEAN.getVMOption("PrintGC").getValue());
        System.out.println("---------------------- ");
    }
    
}
