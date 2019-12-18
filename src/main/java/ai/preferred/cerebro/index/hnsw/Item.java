package ai.preferred.cerebro.index.hnsw;

import ai.preferred.cerebro.index.ids.ExternalID;

public class Item<TVector> {
    public final ExternalID externalId;
    public final TVector vector;

    public Item(ExternalID externalId, TVector vector) {
        this.externalId = externalId;
        this.vector = vector;
    }

}
