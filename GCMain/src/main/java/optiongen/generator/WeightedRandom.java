package optiongen.generator;

import config.FuzzingConfig;
import config.GCConfiguration;

import java.util.ArrayList;
import java.util.List;

public class WeightedRandom {
    public static int pick(List<Double> weight) {
        double total = 0;
        List<Double> newWeight = new ArrayList<>(weight.size());
        for (int i = 0; i < weight.size(); i++) {
            if (weight.get(i) > 0) {
                total += weight.get(i);
                newWeight.add(total);
//                weight.set(i, total);
            }

        }
        // TODO if enable feedback , please divide random type 'OptionPoolRandom' and 'SeedPoolRandom'
        double l = (FuzzingConfig.getRandom(FuzzingConfig.RandomType.OPTION_POOL).nextDouble() * total);
        if (l < GCConfiguration.EPS) {
            System.out.println("weight too low. use random");
            return FuzzingConfig.nextChoice(weight.size(), FuzzingConfig.RandomType.OPTION_POOL);
        }
        for (int i = 0; i < newWeight.size(); i++) {
            if (newWeight.get(i) - l > -GCConfiguration.EPS) {
                return i;
            }
        }
        System.err.println("should not reach here");
        return 0;
    }

    public static List<Integer> pick(List<Double> weight, int num) {
        if (num <= 0) {
            return new ArrayList<>();
        }
        if (num == weight.size()) {
            List<Integer> res = new ArrayList<>(num);
            for (int i = 0; i < num; i++) {
                res.add(i);
            }
            return res;
        }
        double total = 0;
        for (int i = 0; i < weight.size(); i++) {
            if (weight.get(i) <= 0) {
                continue;
            }
            total += weight.get(i);
            weight.set(i, total);
        }
        List<Integer> result = new ArrayList<>();
        if (total < GCConfiguration.EPS) {
            System.out.println("weight too low. use random");
            while (num > 0) {
                int index = FuzzingConfig.nextChoice(weight.size(), FuzzingConfig.RandomType.OPTION_POOL);
                if (!result.contains(index)) {
                    result.add(index);
                    num--;
                }
            }
        } else {
            while (num > 0) {
                double l = (FuzzingConfig.getRandom(FuzzingConfig.RandomType.OPTION_POOL).nextDouble() * total);
                for (int j = 0; j < weight.size(); j++) {
                    if (!result.contains(j) && l < weight.get(j) + GCConfiguration.EPS) {
                        result.add(j);
                        break;
                    }
                }
                num--;
            }
        }
        return result;
    }

    public static List<Integer> randomPick(int size, int num) {
        if (num <= 0) {
            return new ArrayList<>();
        }
        if (num == size) {
            List<Integer> res = new ArrayList<>(num);
            for (int i = 0; i < num; i++) {
                res.add(i);
            }
            return res;
        }

        List<Integer> result = new ArrayList<>();

        while (num > 0) {
            int l = (FuzzingConfig.nextChoice(size, FuzzingConfig.RandomType.OPTION_POOL));
            if (!result.contains(l)) {
                result.add(l);
            }
            num--;
        }
        return result;
    }
}
