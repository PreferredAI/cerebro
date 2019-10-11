package ai.preferred.cerebro.index.scoring;

import ai.preferred.cerebro.index.utils.IndexUtils;
import ai.preferred.cerebro.index.utils.VecHandler;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.Similarity;

import java.io.IOException;

public abstract class VectorSimilarity<TVector> extends Similarity {
    public VectorSimilarity(VecHandler<TVector> handler) {
        this.handler = handler;
    }

    VecHandler<TVector> handler;

    /**
     * Garbage API, don't use. Will remove or change soon.
     */
    @Override
    public long computeNorm(FieldInvertState state) {
        IndexUtils.notifyLazyImplementation("VectorSimilarity / computeNorm");
        return 0;
    }

    /**
     * Garbage API, don't use. Will remove or change soon.
     */
    @Override
    public SimWeight computeWeight(float boost, CollectionStatistics collectionStats, TermStatistics... termStats) {
        IndexUtils.notifyLazyImplementation("CosineSimilarity / computeWeight");
        return null;
    }


    public abstract SimWeight computeWeight(TVector queryVec, IndexReader reader, CollectionStatistics collectionStats);
}
