package oom;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Test {

	public static void main(String[] args) {

        for (int i = 0; i < 100; i++) {
            String name = "round #" + i;

            System.out.println(name);

            IntStream
                    .range(0, 10000000)
                    .parallel().mapToObj((c) -> name)
                    .collect(Collectors.toList());
        }

        System.out.println("PASSED");

    }
}
