package ai.preferred.cerebro.index.similarity;


import org.apache.lucene.document.Document;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.BytesRef;

import ai.preferred.cerebro.index.store.VectorField;
import ai.preferred.cerebro.index.utils.IndexConst;
import ai.preferred.cerebro.index.utils.IndexUtils;

import java.io.IOException;
import java.util.*;

/**
 *
 * Class to compute cosine similarity.
 *
 * @author hpminh@apcs.vn
 */
public class CosineSimilarity extends Similarity {

    public CosineSimilarity(){

    }

    @Override
    public long computeNorm(FieldInvertState state) {
        IndexUtils.notifyLazyImplementation("CosineSimilarity / computeNorm");
        return 0;
    }

    @Override
    public SimWeight computeWeight(float boost, CollectionStatistics collectionStats, TermStatistics... termStats) {
        IndexUtils.notifyLazyImplementation("CosineSimilarity / computeWeight");
        return null;
    }


    public SimWeight computeWeight(double[] queryVec, IndexReader reader, CollectionStatistics collectionStats) {
        return new CosineStats(collectionStats.field(), queryVec, reader);
    }



    @Override
    public SimScorer simScorer(SimWeight stats, LeafReaderContext context) throws IOException {
        CosineStats cosineStats = (CosineStats) stats;
        return new CosineDocScorer(cosineStats, context.docBase);
    }

    private class CosineDocScorer extends SimScorer{
        private final CosineStats stats;
        private final int docBase;
        CosineDocScorer(CosineStats stats, int docBase){
            this.stats = stats;
            this.docBase = docBase;
        }

        @Override
        public float score(int doc, float freq) throws IOException {
            //Document document = stats.reader.document(doc, IndexConst.fieldsRetrieve);
            doc += docBase;
            Document document = stats.reader.document(doc);
            double[] tarVec = VectorField.getFeatureVector(document.getField(IndexConst.VecFieldName).binaryValue().bytes);
            //double tarVecLen = DoubleStoredField.bytesToDouble(document.getField(IndexConst.VecLenFieldName).binaryValue().bytes);//IndexUtils.vecLength(tarVec);
            double tarVecLen = IndexUtils.vecLength(tarVec);
            double cosineScore = IndexUtils.dotProduct(stats.vquery, tarVec) / (stats.vecLength * tarVecLen);
            return (float) cosineScore;
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
    private static class CosineStats extends SimWeight{
        private final String field;
        private final double[] vquery;
        private final double vecLength;
        private final IndexReader reader;

        CosineStats(String field, double[] vquery, IndexReader reader){
            this.field = field;
            this.vquery = vquery;
            this.vecLength = IndexUtils.vecLength(vquery);
            this.reader = reader;
        }

    }
}
