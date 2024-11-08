public class StrainingZGC {
    private static final LongAdder total = new LongAdder();
    private static final int SIZE = 20000;
    private static final int THREADS = 200;

    public static void main(String... args) throws InterruptedException {
        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            public void run() {
                System.exit(0);
            }
        }, 60_000);
        long start = System.nanoTime();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.printf("Shut down initiated - total time = %dms%n",
                    ((System.nanoTime() - start) / 1_000_000));
        }));


        for (int i = 0; i < THREADS; i++) {
            int from = i * (SIZE / THREADS);
            int to = Math.min(from + (SIZE / THREADS) * 10, SIZE);
            Thread thread = new Thread(() -> {
                List<List<Integer>> listOfLists = new ArrayList<>();
                for (int j = 0; j < SIZE / THREADS; j++) {
                    listOfLists.add(
                            IntStream.range(0, SIZE)
                                    .boxed()
                                    .collect(Collectors.toCollection(LinkedList::new)));
                }

                while (true) {
                    total.add(listOfLists.stream().mapToLong(
                            list -> list.stream().mapToLong(Integer::longValue).sum()
                    ).sum());
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        throw new CancellationException("interrupted");
                    }
                }
            });
            thread.start();
        }

        while (true) {
            System.out.println("total = " + total);
            Thread.sleep(5000);
        }
    }
}