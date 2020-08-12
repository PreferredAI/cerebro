package ai.preferred.cerebro.index.bruteforce;

import ai.preferred.cerebro.index.common.VecFloatHandler;
import ai.preferred.cerebro.index.hnsw.Item;
import ai.preferred.cerebro.index.hnsw.structure.BoundedMaxHeap;
import ai.preferred.cerebro.index.hnsw.structure.Candidate;
import ai.preferred.cerebro.index.ids.ExternalID;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.PriorityQueue;
import org.apache.lucene.util.ThreadInterruptedException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author hpminh@apcs.vn
 * Run linear search using multiple core at once
 */
public class BruteIndex {
    int numThreads = 6;
    Item<float[]>[] items;
    VecFloatHandler handler;
    ExecutorService executor;

    public BruteIndex(int numThreads, Item<float[]>[] items, VecFloatHandler handler) {
        this.numThreads = numThreads;
        executor = Executors.newFixedThreadPool(numThreads);
        this.items = items;
        this.handler = handler;
    }

    public List<ExternalID> search(float[] query, int k){
        final int cappedNumHits = k;
        final List<Future<TopDocs>> topDocsFutures = new ArrayList<>(numThreads);
        int base =0;
        int numPerThread = items.length / numThreads;

        for (int i = 0; i < numThreads; ++i) {
            final int start = base;
            final int stop = i == numThreads - 1 ? items.length : base + numPerThread;
            topDocsFutures.add(executor.submit(new Callable<TopDocs>() {
                @Override
                public TopDocs call() throws Exception {
                    BoundedMaxHeap heap = new BoundedMaxHeap(k, () -> null);
                    Candidate holder;
                    for (int j = start; j < stop; j++) {
                        holder = new Candidate(j, handler.distance(items[j].vector, query), null);
                        if(heap.size() < k)
                            heap.add(holder);
                        else{
                            if(heap.top().distance > holder.distance){
                                heap.updateTop(holder);
                            }
                        }
                    }
                    ScoreDoc[] hits = new ScoreDoc[k];
                    for (int i = k - 1; i > -1; i--) {
                        Candidate h = heap.pop();
                        hits[i] = new ScoreDoc(h.nodeId, (float) (1 - h.distance));
                    }
                    return new TopDocs(k, hits, hits[0].score);
                }
            }));
            base += numPerThread;
        }
        int i = 0;
        final TopDocs[] collectedTopdocs = new TopDocs[numPerThread];
        for (Future<TopDocs> future : topDocsFutures) {
            try {
                collectedTopdocs[i] = future.get();
                i++;
            } catch (InterruptedException e) {
                throw new ThreadInterruptedException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        TopDocs ret = TopDocs.merge(0, cappedNumHits, collectedTopdocs, true);
        List<ExternalID> list = new ArrayList<>();
        for(ScoreDoc doc : ret.scoreDocs){
            list.add(items[doc.doc].externalId);
        }
        return list;
    }

}
