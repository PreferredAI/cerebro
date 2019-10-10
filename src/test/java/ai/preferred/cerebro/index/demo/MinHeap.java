package ai.preferred.cerebro.index.demo;

import org.apache.lucene.util.PriorityQueue;

import java.util.ArrayList;
import java.util.function.Supplier;

public class MinHeap extends PriorityQueue<ItemFeatures> {
    public MinHeap(int maxSize, Supplier<ItemFeatures> sentinelObjectSupplier) {
        super(maxSize, sentinelObjectSupplier);
    }

    @Override
    protected boolean lessThan(ItemFeatures a, ItemFeatures b) {
        return a.similarity < b.similarity;
    }

    public ArrayList<Integer> getObjectIds(){
        Object [] arr = getHeapArray();
        ArrayList<Integer> ids = new ArrayList<>(size());
        for (int i = 0; i < size(); i++) {
            ids.add(((ItemFeatures)arr[i + 1]).docID);
        }
        return ids;
    }

}
