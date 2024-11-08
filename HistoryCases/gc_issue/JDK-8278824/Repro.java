import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
 
public final class Repro {
    // java -Xmx20G -Xms20G -XX:+AlwaysPreTouch -XX:+UseLargePages \
    // -Xlog:gc*,gc+phases=debug,gc+task+stats=trace:stdout:uptime,level,tags \
    // Repro
    public static void main(String[] args) throws Exception {
        Repro r = new Repro();
        try {
            r.run();
        } finally {
            r.close();
        }
    }
 
    public static final long tps = 10_000;
    public static final long targetSize = 400_000;
    public static volatile byte[] hole;
 
    final ExecutorService root = Executors.newCachedThreadPool();
    final ScheduledExecutorService timer = Executors.newScheduledThreadPool(28);
    final BlockingQueue<Runnable> queue = ((ScheduledThreadPoolExecutor)timer).getQueue();
    final AtomicLong count = new AtomicLong();
 
    public void run() {
        timer.scheduleAtFixedRate(this::report, 1, 1, TimeUnit.SECONDS);
 
        final long timeoutSecs = targetSize / tps;
        final long delay = TimeUnit.SECONDS.toNanos(1) / tps;
        long startNs = System.nanoTime();
        long waitNs;
        for (;;) {
            final ScheduledFuture<?> timeoutJob = timer.schedule(Repro::onTimeout, timeoutSecs, TimeUnit.SECONDS);
 
            root.execute(() -> {
                count.incrementAndGet();
                hole = new byte[64 << 10];
                timeoutJob.cancel(true);
            });
 
            startNs += delay;
            if ((waitNs = startNs - System.nanoTime()) > 0) {
                LockSupport.parkNanos(waitNs);
            }
        }
    }
 
    void report() {
        System.out.println(new Date() + " count: " + count.getAndSet(0) + " queue: " + queue.size() + " hole: " + hole);
    }
 
    public static void onTimeout() {
        System.out.println("timeout");
    }
 
    public void close() {
        root.shutdown();
        timer.shutdown();
    }
}
