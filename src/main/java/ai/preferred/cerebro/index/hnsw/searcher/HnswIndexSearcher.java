package ai.preferred.cerebro.index.hnsw.searcher;

import ai.preferred.cerebro.index.Searcher;
import ai.preferred.cerebro.index.common.BitSet;
import ai.preferred.cerebro.index.hnsw.GenericObjectPool;
import ai.preferred.cerebro.index.hnsw.HnswManager;
import ai.preferred.cerebro.index.ids.StringID;
import ai.preferred.cerebro.index.lsh.searcher.VectorSimilarity;
import org.apache.lucene.search.*;
import org.apache.lucene.util.ThreadInterruptedException;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author hpminh@apcs.vn
 * @param <TVector>
 */
public class HnswIndexSearcher<TVector> extends HnswManager<TVector> implements Searcher<TVector> {
    ExecutorService executor;
    public HnswIndexSearcher(String idxDir){
        super(idxDir);
        executor = Executors.newFixedThreadPool(nleaves);
        int maxNodeCount = 0;
        leaves = new LeafSegmentSearcher[nleaves];
        //load all leaves
        for (int i = 0; i < nleaves; i++) {
            leaves[i] = new LeafSegmentSearcher<>(this, i, idxDir);
            if (leaves[i].getNodeCount() > maxNodeCount)
                maxNodeCount = leaves[i].getNodeCount();
        }
        int finalMaxNodeCount = maxNodeCount;
        this.visitedBitSetPool = new GenericObjectPool<>(() -> new BitSet(finalMaxNodeCount), nleaves);
    }

    public TopDocs search(TVector query, int k){
        final int limit = Math.max(1, configuration.getMaxItemLeaf());
        final int cappedNumHits = Math.min(k, limit);

        final List<Future<TopDocs>> topDocsFutures = new ArrayList<>(nleaves);
        for (int i = 0; i < nleaves; ++i) {
            LeafSegmentSearcher<TVector> leaf = (LeafSegmentSearcher<TVector>) leaves[i];
            topDocsFutures.add(executor.submit(new Callable<TopDocs>() {
                @Override
                public TopDocs call() throws Exception {
                    return leaf.findNearest(query, k);
                }
            }));
        }
        int i =0;
        final TopDocs[] collectedTopdocs = new TopDocs[nleaves];
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
        return TopDocs.merge(0, cappedNumHits, collectedTopdocs, true);
    }

    /**
     * for LSH use only. Dummy implementation.
     * @return
     */
    @Override
    public VectorSimilarity getVectorSimilarity() {
        return null;
    }

    @Override
    public String[] similaritySearch(TVector vQuery, int resultSize) throws Exception {
        TopDocs res = search(vQuery, resultSize);
        if(res == null)
            return null;
        String[] ret = new String[res.scoreDocs.length];
        for (int j = 0; j < res.scoreDocs.length; j++) {
            ret[j] =  ((StringID)getExternalID(res.scoreDocs[j].doc)).getVal();
        }
        return ret;
    }
}
