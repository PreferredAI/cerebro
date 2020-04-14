package ai.preferred.cerebro.index.common;

/**
 * @author hpminh@apcs.vn
 */
public class DoubleDotHandler extends VecDoubleHandler {
    @Override
    public double similarity(double[] a, double[] b) {
        return dotProduct(a, b);
    }
}
