package ai.preferred.cerebro.index.scoring;


import ai.preferred.cerebro.index.utils.VecHandler;
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
public class CosineSimilarity<TVector> extends VectorSimilarity<TVector> {

    public CosineSimilarity(VecHandler<TVector> handler) {
        super(handler);
    }


    public SimWeight computeWeight(TVector queryVec, IndexReader reader, CollectionStatistics collectionStats) {
        return new CosineStats<>(collectionStats.field(), queryVec, reader);
    }


    @Override
    public SimScorer simScorer(SimWeight stats, LeafReaderContext context) throws IOException {
        CosineStats cosineStats = (CosineStats) stats;
        return new CosineDocScorer<>(cosineStats, context.docBase, handler);
    }

    private class CosineDocScorer<TVector> extends SimScorer{
        private final CosineStats<TVector> stats;
        private final int docBase;
        private final DocumentStoredFieldVisitor fieldVisitor;
        private final VecHandler<TVector> handler;

        CosineDocScorer(CosineStats stats, int docBase, VecHandler<TVector> handler){
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
            return handler.cosineScore(stats.vquery, document);
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
    private static class CosineStats<TVector> extends SimWeight{
        private final String field;
        private final TVector vquery;
        private final IndexReader reader;

        CosineStats(String field, TVector vquery, IndexReader reader){
            this.field = field;
            this.vquery = vquery;
            this.reader = reader;
        }

    }
}
