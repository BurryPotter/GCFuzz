/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

import java.util.concurrent.*;
import java.util.*;

class ArraysSort {
    static final long NPS = (1000L * 1000 * 1000);
    static int THRESHOLD;
    static Long[] numbers;

    static final Comparator<Object> cmp = (Object x, Object y) ->
        ((Long)x).compareTo((Long)y);


    public static void main(String[] args) throws Exception {
        int n = 1 << 22;
        int reps = 30;
        int sreps = 2;
        try {
            if (args.length > 0)
                n = Integer.parseInt(args[0]);
            if (args.length > 1)
                reps = Integer.parseInt(args[1]);
        }
        catch (Exception e) {
            System.out.println("Usage: java ArraysSort n reps");
            return;
        }

        numbers = new Long[n];
        for (int i = 0; i < n; ++i)
            numbers[i] = Long.valueOf(i);

        int thr = ((n + 7) >>> 3) / ForkJoinPool.getCommonPoolParallelism();
        THRESHOLD = (thr <= 1 << 13) ? 1 << 13 : thr;

        System.out.println("Threshold = " + THRESHOLD);

        Long[] a = new Long[n];
        ForkJoinPool pool = ForkJoinPool.commonPool();
        seqTest(a, n, 1);
        System.out.println(pool);
        parTest(a, n, reps);
        System.out.println(pool);
        seqTest(a, n, 2);
        System.out.println(pool);
        cseqTest(a, n, 1);
        System.out.println(pool);
        cparTest(a, n, reps);
        System.out.println(pool);
        cseqTest(a, n, 2);
        System.out.println(pool);
    }

    static void seqTest(Long[] a, int n, int reps) {
        System.out.printf("Sorting %d longs, %d replications\n", n, reps);
        long start = System.nanoTime();
        for (int i = 0; i < reps; ++i) {
            new RandomRepacker(null, numbers, a, 0, n, n).invoke();
            long last = System.nanoTime();
            Arrays.sort(a);
            long now = System.nanoTime();
            double total = (double)(now - start) / NPS;
            double elapsed = (double)(now - last) / NPS;
            System.out.printf("Arrays.sort   time:  %7.3f total %9.3f\n",
                              elapsed, total);
            new OrderChecker(null, a, 0, n, n).invoke();
        }
    }

    static void cseqTest(Long[] a, int n, int reps) {
        System.out.printf("Sorting %d longs, %d replications\n", n, reps);
        long start = System.nanoTime();
        for (int i = 0; i < reps; ++i) {
            new RandomRepacker(null, numbers, a, 0, n, n).invoke();
            long last = System.nanoTime();
            Arrays.sort(a, cmp);
            long now = System.nanoTime();
            double total = (double)(now - start) / NPS;
            double elapsed = (double)(now - last) / NPS;
            System.out.printf("Arrays.cmp sort time:  %7.3f total %9.3f\n",
                              elapsed, total);
            new OrderChecker(null, a, 0, n, n).invoke();
        }
    }

    static void parTest(Long[] a, int n, int reps) throws Exception {
        System.out.printf("Sorting %d longs, %d replications\n", n, reps);
        long start = System.nanoTime();
        for (int i = 0; i < reps; ++i) {
            new RandomRepacker(null, numbers, a, 0, n, n).invoke();
            long last = System.nanoTime();
            Arrays.parallelSort(a);
            long now = System.nanoTime();
            double total = (double)(now - start) / NPS;
            double elapsed = (double)(now - last) / NPS;
            System.out.printf("Parallel sort time:  %7.3f total %9.3f\n",
                              elapsed, total);
            new OrderChecker(null, a, 0, n, n).invoke();
        }
    }

    static void cparTest(Long[] a, int n, int reps) throws Exception {
        System.out.printf("Sorting %d longs, %d replications\n", n, reps);
        long start = System.nanoTime();
        for (int i = 0; i < reps; ++i) {
            new RandomRepacker(null, numbers, a, 0, n, n).invoke();
            long last = System.nanoTime();
            Arrays.parallelSort(a, cmp);
            long now = System.nanoTime();
            double total = (double)(now - start) / NPS;
            double elapsed = (double)(now - last) / NPS;
            System.out.printf("Par cmp  sort time:  %7.3f total %9.3f\n",
                              elapsed, total);
            new OrderChecker(null, a, 0, n, n).invoke();
        }
    }

    static void checkSorted(Long[] a) {
        int n = a.length;
        long x = ((Long)a[0]).longValue(), y;
        for (int i = 0; i < n - 1; i++) {
            if (x > (y = ((Long)a[i+1]).longValue()))
                throw new Error("Unsorted at " + i + ": " + x + " / " + y);
            x = y;
        }
    }

    static final class RandomRepacker extends CountedCompleter<Void> {
        final Long[] src;
        final Long[] dst;
        final int lo, hi, size;
        RandomRepacker(CountedCompleter<?> par, Long[] src, Long[] dst,
                       int lo, int hi, int size) {
            super(par);
            this.src = src; this.dst = dst;
            this.lo = lo; this.hi = hi; this.size = size;
        }

        public final void compute() {
            Long[] s = src;
            Long[] d = dst;
            int l = lo, h = hi, n = size;
            while (h - l > THRESHOLD << 1) {
                int m = (l + h) >>> 1;
                addToPendingCount(1);
                new RandomRepacker(this, s, d, m, h, n).fork();
                h = m;
            }
            ThreadLocalRandom rng = ThreadLocalRandom.current();
            Long dl = d[l];
            int m = (dl == null) ? h : rng.nextInt(l, h);
            for (int i = l; i < m; ++i)
                d[i] = s[rng.nextInt(n)];
            if (dl != null) {
                dl = d[l];
                for (int i = m; i < h; ++i)
                    d[i] = dl;
            }
            tryComplete();
        }
    }

    static final class OrderChecker extends CountedCompleter<Void> {
        final Long[] array;
        final int lo, hi, size;
        OrderChecker(CountedCompleter<?> par, Long[] a, int lo, int hi, int size) {
            super(par);
            this.array = a;
            this.lo = lo; this.hi = hi; this.size = size;
        }

        public final void compute() {
            Long[] a = this.array;
            int l = lo, h = hi, n = size;
            while (h - l > THRESHOLD) {
                int m = (l + h) >>> 1;
                addToPendingCount(1);
                new OrderChecker(this, a, m, h, n).fork();
                h = m;
            }
            int bound = h < n ? h : n - 1;
            int i = l;
            long x = ((Long)a[i]).longValue(), y;
            while (i < bound) {
                if (x > (y = ((Long)a[++i]).longValue()))
                    throw new Error("Unsorted " + x + " / " + y);
                x = y;
            }
            tryComplete();
        }
    }

}
