package ai.preferred.cerebro.index.common;

/**
 * @author hpminh@apcs.vn
 */
public class FloatDotHandler extends VecFloatHandler {
    @Override
    public double similarity(float[] a, float[] b) {
        return dotProduct(a, b);
    }
}
