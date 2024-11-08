public class GetArrayCritical {
  static {
    System.loadLibrary("critical");
  }

  public static native void get_critical_array();
  public static void main(String[] args) {
    get_critical_array();
    System.out.println("ready");
    while(true);
    
  }
}
