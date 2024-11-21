package com.ylli.common.common.uid;

import java.util.random.RandomGenerator;

public class RandomLongGenerator implements IDGenerator<Long> {

    // java 17 Enhanced Pseudo-Random Number Generators
    public static Long nextLong(long origin, long bound) {
        return RandomGenerator.getDefault().nextLong(origin, bound);
    }

    @Override
    public Long next() {
        return nextLong(1, Long.MAX_VALUE);
    }
}
