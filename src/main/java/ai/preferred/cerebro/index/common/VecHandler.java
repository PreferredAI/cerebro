package ai.preferred.cerebro.index.common;

import ai.preferred.cerebro.index.hnsw.Node;
import ai.preferred.cerebro.index.utils.IndexConst;
import org.apache.lucene.document.Document;

import java.io.File;
import java.util.concurrent.atomic.AtomicReferenceArray;
/**
 * @author hpminh@apcs.vn
 */
public interface VecHandler<TVector> {
    void saveNodes(String vecFilename, Node<TVector>[] nodes, int nodeCount);

    void saveNodesBlocking(String vecFilename, AtomicReferenceArray<Node<TVector>> nodes, int nodeCount);

    void save(String vecFilename, TVector[] vecs);

    TVector[] load(File vecsFile);

    double similarity(TVector a, TVector b);

    default double distance(TVector a, TVector b){
        return 1 - similarity(a, b);
    }


    default boolean computeBit(TVector a, TVector b){
        return dotProduct(a, b) > 0;
    }

    double dotProduct(TVector a, TVector b);

    double vecLength(TVector vec);

    default BitAndDistance computeBitAndDistance(TVector dot, TVector plane){
        double dotproduct = dotProduct(dot, plane);
        double distance = Math.abs(dotproduct) / vecLength(plane);
        return new BitAndDistance(dotproduct > 0, distance);
    }

    /**
     * Encoding a vector into an array of byte.
     *
     * @param arr The vector to be encoded to bytes.
     * @return byte encoding of the vector.
     */
    byte[] vecToBytes(TVector arr);

    /**
     * Decode a byte array back into a vector.
     *
     * @param data The data to be decoded back to a vector.
     * @return vector values of a byte array.
     */
    TVector getFeatureVector(byte[] data);

    /**
     * Function to conform to Lucene's internal working.
     * @param query
     * @param doc
     * @return
     */
    default float scoreDocument(TVector query, Document doc){
        TVector vec = getFeatureVector(doc.getField(IndexConst.VecFieldName).binaryValue().bytes);
        double dot = dotProduct(query, vec);
        return (float) (dot /(vecLength(query) * vecLength(vec)));
    }
}

