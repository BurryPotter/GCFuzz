import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.util.HashSet;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;

import com.sun.management.GarbageCollectionNotificationInfo;
import java.util.concurrent.atomic.AtomicLong;

public class TestMxBean implements NotificationListener {
    private AtomicLong value =new AtomicLong();
    public TestMxBean() {
    }

    private void runTest() {
        System.out.println("Before ");
        for (int index = 0; index < 200; index++) {
            HashSet<String> set = new HashSet<>();
            for (int i = 0; i < 10050000; i++) {
                set.add(String.valueOf(i));
            }
        }
        System.out.println("After");
    }

    public static void main(String[] args) throws InterruptedException {
        TestMxBean t = new TestMxBean();
        registerSelf(t);
        t.runTest();
    }


    private static final String PS_OLD_GENERATION_BEAN_NAME = "PS MarkSweep";
    private static final String CMS_OLD_GENERATION_BEAN_NAME = "ConcurrentMarkSweep";

    private static final String PS_OLD_GENERATION_NOTIFICATION_NAME = "PS Old Gen";
    private static final String CMS_OLD_GENERATION_NOTIFICATION_NAME = "CMS Old Gen";


    private static void registerSelf(TestMxBean listener) {
        for (java.lang.management.GarbageCollectorMXBean bean : ManagementFactory.getGarbageCollectorMXBeans()) {
            if (bean instanceof NotificationEmitter) {
                String name = null;
                switch (bean.getName()) {
                    case PS_OLD_GENERATION_BEAN_NAME:
                        name = PS_OLD_GENERATION_NOTIFICATION_NAME;
                        break;
                    case CMS_OLD_GENERATION_BEAN_NAME:
                        name = CMS_OLD_GENERATION_NOTIFICATION_NAME;
                        break;
                }

                if (name != null) {
                    System.out.println("Bean name: " + bean.getName());
                    NotificationEmitter notificationEmitter = (NotificationEmitter) bean;
                    notificationEmitter.addNotificationListener(listener, null, name);
                }
            }

        }
    }

    public void handleNotification(final Notification notification, final Object handback) {
        if (notification.getType().equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)) {
            // Unwrap GC data
            MemoryUsage usage = GarbageCollectionNotificationInfo.from((CompositeData) notification.getUserData())
                    .getGcInfo()
                    .getMemoryUsageAfterGc()
                    .get(handback);

            System.out.println(handback + ": " + usage);

        }
    }

    private static double round(final double value, final int places) {
        final double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }

}
