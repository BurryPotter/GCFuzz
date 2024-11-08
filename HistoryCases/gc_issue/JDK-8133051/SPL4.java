/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

import java.util.*;
import java.util.concurrent.*;

public class SPL4 {
    /*
     * Create PRODUCERS publishers each with PROCESSORS processors
     * each with CONSUMERS subscribers, each sent ITEMS items, with
     * max CAP buffering; repeat REPS times
     */
    static final int  ITEMS      = 1 << 22;
    static final int  PRODUCERS  = 32;
    static final int  PROCESSORS = 32;
    static final int  CONSUMERS  = 32;
    static final int  CAP        = Flow.defaultBufferSize();
    static final int  REPS       = 12;
    static final int  SINKS      = PRODUCERS * PROCESSORS * CONSUMERS;
    static final long NEXTS      = (long)ITEMS * SINKS;
    static final Phaser phaser = new Phaser(SINKS + 1);

    public static void main(String[] args) throws Exception {
        System.out.println("ITEMS: " + ITEMS + 
                           " PRODUCERS: " + PRODUCERS + 
                           " PROCESSORS: " + PROCESSORS + 
                           " CONSUMERS: " + CONSUMERS +
                           " CAP: " + CAP);
        for (int reps = 0; reps < REPS; ++reps) {
            long startTime = System.nanoTime();
            for (int i = 0; i < PRODUCERS; ++i)
                new Pub().fork();
            phaser.arriveAndAwaitAdvance();
            long elapsed = System.nanoTime() - startTime;
            double secs = ((double)elapsed) / (1000L * 1000 * 1000);
            double ips = NEXTS / secs;
            System.out.printf("Time: %7.2f", secs);
            System.out.printf(" items per sec: %14.2f\n", ips);
            System.out.println(ForkJoinPool.commonPool());
        }
    }

    static final class Sub implements Flow.Subscriber<Boolean> {
        int count;
        Flow.Subscription subscription;
        public void onSubscribe(Flow.Subscription s) { 
            (subscription = s).request(CAP); 
        }
        public void onNext(Boolean b) { 
            if (b && (++count & ((CAP >>> 1) - 1)) == 0)
                subscription.request(CAP >>> 1);
        }
        public void onComplete() { 
            if (count != ITEMS)
                System.out.println("Error: remaining " + (ITEMS - count));
            phaser.arrive(); 
        }
        public void onError(Throwable t) { t.printStackTrace(); }
    }

    static final class Proc extends SubmissionPublisher<Boolean>
        implements Flow.Processor<Boolean,Boolean> {
        Flow.Subscription subscription;
        int count;
        Proc(Executor executor, int maxBufferCapacity) {
            super(executor, maxBufferCapacity);
        }
        public void onSubscribe(Flow.Subscription subscription) {
            (this.subscription = subscription).request(CAP);
        }
        public void onNext(Boolean item) {
            if ((++count & ((CAP >>> 1) - 1)) == 0)
                subscription.request(CAP >>> 1);
            submit(item);
        }
        public void onError(Throwable ex) { closeExceptionally(ex); }
        public void onComplete() { close(); }
    }

    static final class Pub extends RecursiveAction {
        final SubmissionPublisher<Boolean> pub = 
            new SubmissionPublisher<Boolean>(ForkJoinPool.commonPool(), CAP);
        public void compute() {
            SubmissionPublisher<Boolean> p = pub;
            for (int j = 0; j < PROCESSORS; ++j) {
                Proc t = new Proc(ForkJoinPool.commonPool(), CAP);
                for (int i = 0; i < CONSUMERS; ++i)
                    t.subscribe(new Sub());
                p.subscribe(t);
            }
            for (int i = 0; i < ITEMS; ++i) 
                p.submit(Boolean.TRUE);
            p.close();
        }
    }
}

