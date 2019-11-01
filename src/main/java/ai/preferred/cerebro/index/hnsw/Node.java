package ai.preferred.cerebro.index.hnsw;

import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;

public class Node<TVector> {
    public final int internalId;

    public final IntArrayList[] outConns;

    public final IntArrayList[] inConns;

    public final Item<TVector> item;


    public Node(int id, IntArrayList[] outgoingConnections, IntArrayList[] incomingConnections, Item item) {
        assert item != null;
        this.internalId = id;
        this.outConns = outgoingConnections;
        this.inConns = incomingConnections;
        this.item = item;
    }

    public int maxLevel() {
        return this.outConns.length - 1;
    }

    int externalID(){
        return item.externalId;
    }

    public TVector vector(){
        return item.vector;
    }

}
