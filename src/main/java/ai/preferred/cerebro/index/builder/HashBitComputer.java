package ai.preferred.cerebro.index.builder;
@FunctionalInterface
public interface HashBitComputer<TVector> {
    boolean compute(TVector a, TVector b);
}
