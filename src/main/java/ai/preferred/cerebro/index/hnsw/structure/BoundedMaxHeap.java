package ai.preferred.cerebro.index.hnsw.structure;

import org.apache.lucene.util.PriorityQueue;

import java.util.function.Supplier;

/**
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

    public int[] getCandidateIds(){
        Object [] arr = getHeapArray();
        //always call size() to know the number of elements
        //in the heap, don't get it directly from the size
        //of its internal array.
        int[] ids = new int[size()];
        for (int i = 0; i < size(); i++) {
            //lucene's PriorityQueue start its array from 1
            ids[i] = ((Candidate)arr[i + 1]).nodeId;
        }
        return ids;
    }

}
