package ai.preferred.cerebro.index.store;


import org.apache.lucene.util.ArrayUtil;

import ai.preferred.cerebro.index.utils.IndexUtils;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.function.Supplier;


/**
 *
 * Base class to contain, rank and retrieve top K objects
 * based on their score. This implementation use a modified
 * version of the Quickselect algorithm.
 *
 * @author hpminh@apcs.vn
 *
 * @param <T> Type of object to be scored and retrieved.
 */
public abstract class Container <T> implements Iterable<T>{
    private int size = 0;
    private int maxSize;
    protected T[] arr;

    enum PivotScheme{
        START,
        MID,
        END,
        RAND
    }
    private final PivotScheme pivotScheme;

    public Container(int maxSize) {
        this(maxSize, () -> null);
    }

    public Container(int maxSize, Supplier<T> sentinelObjectSupplier) {
        if (maxSize < 1 || maxSize >= ArrayUtil.MAX_ARRAY_LENGTH) {
            throw new IllegalArgumentException("maxSize must be >= 1 and < " + (ArrayUtil.MAX_ARRAY_LENGTH) + "; got: " + maxSize);
        }

        // T is unbounded type, so this unchecked cast works always:
        @SuppressWarnings("unchecked") final T[] h = (T[]) new Object[maxSize];
        this.arr = h;
        this.maxSize = maxSize;
        this.pivotScheme = PivotScheme.RAND;

        // If sentinel objects are supported, populate the queue with them
        T sentinel = sentinelObjectSupplier.get();
        if (sentinel != null) {
            arr[0] = sentinel;
            for (int i = 1; i < arr.length; i++) {
                arr[i] = sentinelObjectSupplier.get();
            }
            size = maxSize;
        }
    }

    public Container(T[] arr){
        this.arr = arr;
        this.maxSize = arr.length;
        this.pivotScheme = PivotScheme.RAND;
    }

    protected abstract boolean lessThan(T a, T b);
    public abstract void calculateScore(T target);

    public final void add(T element) {
        assert size < maxSize;
        arr[size++] = element;
    }

    public final T get(int index){
        assert index < size;
        return arr[index];
    }
    public T insertWithOverflow(T element) {
        IndexUtils.notifyLazyImplementation("Container / insertWithOverflow");
        return null;
    }

    public final T top() {
        IndexUtils.notifyLazyImplementation("Container / top");
        return null;
    }

    public final T pop() {
        if (size > 0) {
            T result = arr[size - 1];
            arr[size - 1] = null;        // permit GC of objects
            size--;
            return result;
        } else {
            return null;
        }
    }

//    public final T updateTop() {
//        //downHeap(1);
//        //return heap[1];
//        IndexUtils.notifyLazyImplementation("Container / updateTop");
//        return null;
//    }
//
//
//    public final T updateTop(T newTop) {
//        //heap[1] = newTop;
//        //return updateTop();
//        IndexUtils.notifyLazyImplementation("Container / updateTop");
//        return null;
//    }

    /** Returns the number of elements currently stored in the Container. */
    public final int size() {
        return size;
    }

    /** Removes all entries from the Container. */
    public final void clear() {
        for (int i = 0; i < size; i++) {
            arr[i] = null;
        }
        arr = null;
        size = 0;
    }

    public final boolean remove(T element) {
        for (int i = 0; i <= size; i++) {
            if (arr[i] == element) {
                for(int j = i; j < size - 1;){
                    arr[j] = arr[++j];
                }
                arr[size - 1] = null; // permit GC of objects
                size--;
                return true;
            }
        }
        return false;
    }

    public void pullTopK(int k, boolean ordered, boolean trim){
        if(k < size)
            orderStatistic(0, size, size - k);
        else
            k = size;
        if(ordered)
            quicksort(size - k, size);
        if(trim){
            for(int i = 0; i < size; i++){
                if(i < k){
                    arr[i] = arr[size - k + i];
                }
                else
                    arr[i] = null;
            }
            size = k;
        }
    }

    protected void quicksort(int start, int end){
        if (end - start <= 1)
            return;
        if (end - start == 2){
            if (lessThan(arr[start + 1], arr[start]))
                swap(start, start+1);
            return;
        }
        int pivot = partition(start, end);
        quicksort(start, pivot);
        quicksort(pivot + 1, end);
    }

    protected void orderStatistic(int start, int end, int i){
        if (end - start <= 1)
            return;
        if (end - start == 2){
            if (lessThan(arr[start + 1], arr[start]))
                swap(start, start+1);
            return;
        }
        int pivot = partition(start, end);
        if (pivot == i)
            return;
        else if (pivot < i){
            orderStatistic(pivot + 1, end, i);
            return;
        }
        else{
            orderStatistic(start, pivot, i);
            return;
        }

    }

    protected int pivotScheme(int start, int end){
        switch (pivotScheme){
            case START:
                return start;
            case MID:
                return (start + end) >>> 1;
            case END:
                return end - 1;
            case RAND:
                return (new Random()).nextInt(end - start) + start;
        }
        //this is only for syntax
        //the return here is never executed
        return 0;
    }

    protected int partition(int start, int end){
        int pivot = pivotScheme(start, end);
        swap(start, pivot);
        int pIndex = start;
        for (int seen = start + 1; seen < end; seen++){
            if (lessThan(arr[seen], arr[start])){
                pIndex++;
                swap(pIndex, seen);
            }
        }
        swap(pIndex, start);
        return pIndex;
    }

    protected void swap(int a, int b){
        T hold = arr[a];
        arr[a] = arr[b];
        arr[b] = hold;
    }



    protected final Object[] getArray() {
        return (Object[]) arr;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {

            int i = 0;

            @Override
            public boolean hasNext() {
                return i < size;
            }

            @Override
            public T next() {
                if (hasNext() == false) {
                    throw new NoSuchElementException();
                }
                return arr[i++];
            }

        };
    }

}

