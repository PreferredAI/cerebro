package performance_test;

import ai.preferred.cerebro.index.common.VecFloatHandler;

public class FloatDotHandler extends VecFloatHandler {

    @Override
    public double similarity(float[] a, float[] b) {
        return dotProduct(a, b);
    }
}


