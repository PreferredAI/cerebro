package ai.preferred.cerebro.index.similarity;


import ai.preferred.cerebro.index.exception.UnsupportedDataType;
import ai.preferred.cerebro.index.field.LSHVectorField;
import ai.preferred.cerebro.index.utils.HashUtils;
import ai.preferred.cerebro.index.utils.VecToByte;
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
public class CosineSimilarity<TVector> extends Similarity {

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


    public SimWeight computeWeight(TVector queryVec, IndexReader reader, CollectionStatistics collectionStats) {
        return new CosineStats<>(collectionStats.field(), queryVec, reader);
    }



    @Override
    public SimScorer simScorer(SimWeight stats, LeafReaderContext context) throws IOException {
        CosineStats cosineStats = (CosineStats) stats;
        return new CosineDocScorer(cosineStats, context.docBase);
    }

    private class CosineDocScorer<TVector> extends SimScorer{
        private final CosineStats<TVector> stats;
        private final int docBase;
        private final DocumentStoredFieldVisitor fieldVisitor;
        private final CosineCalculator<TVector> calculator;
        CosineDocScorer(CosineStats stats, int docBase){
            this.stats = stats;
            this.docBase = docBase;
            this.fieldVisitor = new DocumentStoredFieldVisitor(IndexConst.VecFieldName);
            if (stats.vquery instanceof float[]){
                calculator = (CosineCalculator<TVector>)((CosineCalculator<float[]>)CosineSimilarity::cosineFloat);
            }
            else if(stats.vquery instanceof double[]){
                calculator = (CosineCalculator<TVector>)((CosineCalculator<double[]>)CosineSimilarity::cosineDouble);
            }
            else{
                calculator = null;
                try {
                    throw new UnsupportedDataType(stats.vquery.getClass());
                } catch (UnsupportedDataType unsupportedDataType) {
                    unsupportedDataType.printStackTrace();
                }
            }

        }

        @Override
        public float score(int doc, float freq) throws IOException {
            doc += docBase;
            stats.reader.document(doc, fieldVisitor);
            Document document = fieldVisitor.getDocument();
            return calculator.calculate(stats.vquery, document);
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

    @FunctionalInterface
    private interface CosineCalculator<TVector>{
        float calculate(TVector query, Document b);
    }

    private static float cosineFloat(float[] query, Document doc){
        float[] vec = IndexUtils.getFloatFeatureVector(doc.getField(IndexConst.VecFieldName).binaryValue().bytes);
        float dot = HashUtils.dotProductFloat(query, vec);
        return dot /(HashUtils.floatVecLength(query) * HashUtils.floatVecLength(vec));
    }

    private static float cosineDouble(double[] query, Document doc){
        double[] vec = IndexUtils.getDoubleFeatureVector(doc.getField(IndexConst.VecFieldName).binaryValue().bytes);
        double dot = HashUtils.dotProductDouble(query, vec);
        return (float) (dot /(HashUtils.doubleVecLength(query) * HashUtils.doubleVecLength(vec)));
    }
}
