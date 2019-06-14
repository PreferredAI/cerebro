package preferred.ai.cerebro.index.search.structure;


import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import preferred.ai.cerebro.index.store.Container;

import java.io.IOException;

/**
 * Cerebro's internal Scoring functionality.
 * @param <T> Type of Object to rank against each others.
 *
 */
public abstract class CeCollector <T extends ScoreDoc> implements Collector {

    protected static final TopDocs EMPTY_TOPDOCS = new TopDocs(0, new ScoreDoc[0], Float.NaN);

    final protected int topK;
    protected Container<T> arr;
    protected int totalHits;

    protected CeCollector(Container<T> container, int k) {
        this.arr = container;
        this.topK = k;
    }

    protected void populateResults(ScoreDoc[] results, int howMany) {
        for (int i = 0; i <= howMany - 1; i++) {
            results[i] = arr.pop();
        }
    }

    abstract protected TopDocs newTopDocs(ScoreDoc[] results, int start);

    public int getTotalHits() {
        return totalHits;
    }

    protected int topDocsSize() {
        // In case pq was populated with sentinel values, there might be less
        // results than pq.size(). Therefore return all results until either
        // pq.size() or totalHits.
        return totalHits < arr.size() ? totalHits : arr.size();
    }

    public TopDocs topDocs() {
        // In case pq was populated with sentinel values, there might be less
        // results than pq.size(). Therefore return all results until either
        // pq.size() or totalHits.
        return topDocs(0, topDocsSize());
    }


    public TopDocs topDocs(int start) {
        // In case pq was populated with sentinel values, there might be less
        // results than pq.size(). Therefore return all results until either
        // pq.size() or totalHits.
        return topDocs(start, topDocsSize());
    }

    public TopDocs topDocs(int start, int howMany) {

        // In case pq was populated with sentinel values, there might be less
        // results than pq.size(). Therefore return all results until either
        // pq.size() or totalHits.
        int size = topDocsSize();

        // Don't bother to throw an exception, just return an empty TopDocs in case
        // the parameters are invalid or out of range.
        // TODO: shouldn't we throw IAE if apps give bad params here so they dont
        // have sneaky silent bugs?
        if (start < 0 || start >= size || howMany <= 0) {
            return newTopDocs(null, start);
        }

        // We know that start < pqsize, so just fix howMany.
        howMany = Math.min(size - start, howMany);
        ScoreDoc[] results = new ScoreDoc[howMany];

        // pq's pop() returns the 'least' element in the queue, therefore need
        // to discard the first ones, until we reach the requested range.
        // Note that this loop will usually not be executed, since the common usage
        // should be that the caller asks for the last howMany results. However it's
        // needed here for completeness.
        for (int i = arr.size() - start - howMany; i > 0; i--) { arr.pop(); }

        // Get the requested results from pq.
        populateResults(results, howMany);

        return newTopDocs(results, start);
    }

}

