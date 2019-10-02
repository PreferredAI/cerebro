package ai.preferred.cerebro.index.utils;

@FunctionalInterface
public interface ByteToVec<TVector> {
    TVector change(byte[] data);
}
