package ai.preferred.cerebro.index.utils;

@FunctionalInterface
public interface VecToByte<TVector> {
    byte[] change(TVector arr);

}
