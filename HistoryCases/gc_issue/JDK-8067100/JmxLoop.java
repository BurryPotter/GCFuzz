import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.util.List;

/**
 * Created by E333939 on 11/6/2014.
 */
public class JmxLoop
{
  public static void main(String[] args)
  {
    try
    {
      while (true)
      {
        System.out.println("JMX Class Loading Info");
        ClassLoadingMXBean classLoadingBean = ManagementFactory.getClassLoadingMXBean();
        System.out.println("loaded class count \t\t\t" +  classLoadingBean.getLoadedClassCount());
        System.out.println("total loaded class count \t\t" + classLoadingBean.getTotalLoadedClassCount());
        System.out.println("unloaded class count \t\t\t" + classLoadingBean.getUnloadedClassCount());

        //  Display Info
        System.out.println("JMX Memory Pool Info");
        List<MemoryPoolMXBean> poolList = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean pool : poolList)
        {
          MemoryUsage currentUsage = pool.getUsage();
          MemoryUsage peakUsage = pool.getPeakUsage();

          System.out.println(pool.getName());
          System.out.println("currentUsage\n" + currentUsage);
          System.out.println("peakUsage\n" + peakUsage);
        }

        Thread.sleep(15000);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}


