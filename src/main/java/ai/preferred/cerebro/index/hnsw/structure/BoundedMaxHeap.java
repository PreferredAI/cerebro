package ai.preferred.cerebro.index.hnsw.structure;

import org.apache.lucene.util.PriorityQueue;

import java.util.function.Supplier;

/**
 * @author hpminh@apcs.vn
 * Class extending lucene's binary heap (minheap) implementation {@link PriorityQueue}
 */
public class BoundedMaxHeap extends PriorityQueue<Candidate> {
    public BoundedMaxHeap(int maxSize, Supplier<Candidate> sentinelObjectSupplier) {
        super(maxSize, sentinelObjectSupplier);
    }

    @Override
    protected boolean lessThan(Candidate a,Candidate b) {
        //originally lucene's implementation of PriorityQueue is a MinHeap
        //and this function is used to determine if a get moved up in the
        //heap. Here we want a max heap so we should check if a > b
        return a.distance > b.distance;
    }
}
