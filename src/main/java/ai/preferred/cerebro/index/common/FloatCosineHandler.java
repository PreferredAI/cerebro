package ai.preferred.cerebro.index.common;

import ai.preferred.cerebro.index.common.VecFloatHandler;

/**
 * @author hpminh@apcs.vn
 */

public class FloatCosineHandler extends VecFloatHandler {
    @Override
    public double similarity(float[] a, float[] b) {
        float dot = 0.0f;
        float nru = 0.0f;
        float nrv = 0.0f;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            nru += a[i] * a[i];
            nrv += b[i] * b[i];
        }

        return dot / (float)(Math.sqrt(nru) * Math.sqrt(nrv));
    }
}
