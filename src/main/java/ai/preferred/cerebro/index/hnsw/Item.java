package ai.preferred.cerebro.index.hnsw;

public class Item<TVector> {
    public final int externalId;
    public final TVector vector;

    public Item(int externalId, TVector vector) {
        this.externalId = externalId;
        this.vector = vector;
    }

}
