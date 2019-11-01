package ai.preferred.cerebro.index.lsh.search;


import ai.preferred.cerebro.index.handler.VecHandler;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DocumentStoredFieldVisitor;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.BytesRef;


import ai.preferred.cerebro.index.utils.IndexConst;
import ai.preferred.cerebro.index.utils.IndexUtils;

import java.io.IOException;

/**
 *
 * Class to compute cosine similarity.
 *
 * @author hpminh@apcs.vn
 */
public class VectorSimilarity<TVector> extends Similarity{
    VecHandler<TVector> handler;
    public VectorSimilarity(VecHandler<TVector> handler) {
        this.handler = handler;
    }


    public SimWeight computeWeight(TVector queryVec, IndexReader reader, CollectionStatistics collectionStats) {
        return new VectorStats<>(collectionStats.field(), queryVec, reader);
    }

    /**
     * Garbage API, will remove in the next version
     */
    @Override
    public long computeNorm(FieldInvertState state) {
        IndexUtils.notifyLazyImplementation("VectorSimilarity / computeNorm");
        return 0;
    }

    /**
     * Garbage API, will remove in the next version
     */
    @Override
    public SimWeight computeWeight(float boost, CollectionStatistics collectionStats, TermStatistics... termStats) {
        IndexUtils.notifyLazyImplementation("VectorSimilarity / computeWeight");
        return null;
    }

    public SimScorer simScorer(Similarity.SimWeight stats, LeafReaderContext context) throws IOException {
        VectorStats vectorStats = (VectorStats) stats;
        return new VectorDocScorer<>(vectorStats, context.docBase, handler);
    }

    private class VectorDocScorer<TVector> extends Similarity.SimScorer {
        private final VectorStats<TVector> stats;
        private final int docBase;
        private final DocumentStoredFieldVisitor fieldVisitor;
        private final VecHandler<TVector> handler;

        VectorDocScorer(VectorStats stats, int docBase, VecHandler<TVector> handler){
            this.stats = stats;
            this.docBase = docBase;
            this.fieldVisitor = new DocumentStoredFieldVisitor(IndexConst.VecFieldName);
            this.handler = handler;
        }

        @Override
        public float score(int doc, float freq) throws IOException {
            doc += docBase;
            stats.reader.document(doc, fieldVisitor);
            Document document = fieldVisitor.getDocument();
            return handler.scoreDocument(stats.vquery, document);
        }

        @Override
        public float computeSlopFactor(int distance) {
            IndexUtils.notifyLazyImplementation("CosineDocScorer / computeSlopFactor");
            return 0;
        }

        @Override
        public float computePayloadFactor(int doc, int start, int end, BytesRef payload) {
            IndexUtils.notifyLazyImplementation("CosineDocScorer / computePayloadFactor");
            return 0;
        }
    }
    private static class VectorStats<TVector> extends Similarity.SimWeight {
        private final String field;
        private final TVector vquery;
        private final IndexReader reader;

        VectorStats(String field, TVector vquery, IndexReader reader){
            this.field = field;
            this.vquery = vquery;
            this.reader = reader;
        }

    }
}
