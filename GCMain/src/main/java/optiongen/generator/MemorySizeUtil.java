package optiongen.generator;

import config.FuzzingConfig;

public class MemorySizeUtil {
    private static final long[] preparedMemorySize = new long[]{1024, 2048, 4096, 8192};
//    private static final long[] preparedMemorySize = new long[]{128};

    private static final String MEMORY_UNIT = "m";

    public static String generate() {
        long size1 = preparedMemorySize[FuzzingConfig.nextChoice(preparedMemorySize.length)];
        long size2 = preparedMemorySize[FuzzingConfig.nextChoice(preparedMemorySize.length)];
        if (size1 > size2) {
            long temp = size1;
            size1 = size2;
            size2 = temp;
        }
        return " -Xms" + size1 + MEMORY_UNIT + " -Xmx" + size2 + MEMORY_UNIT;
    }
}
