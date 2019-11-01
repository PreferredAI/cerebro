package ai.preferred.cerebro.index.handler;

public class DoubleCosineHandler extends VecDoubleHandler {
    @Override
    public double distance(double[] a, double[] b) {
        double dot = 0.0f;
        double nru = 0.0f;
        double nrv = 0.0f;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            nru += a[i] * a[i];
            nrv += b[i] * b[i];
        }

        double similarity = dot / (Math.sqrt(nru) * Math.sqrt(nrv));
        return 1 - similarity;
    }
}
