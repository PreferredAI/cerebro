package ai.preferred.cerebro.index.utils;

import ai.preferred.cerebro.index.builder.BitAndDistance;
import org.apache.lucene.document.Document;

import java.nio.ByteBuffer;

public interface VecHandler<TVector> {
    void save(String vecFilename, TVector[] vecs);
    TVector[] load(String vecFilename);

    default boolean computeBit(TVector a, TVector b){
        return dotProduct(a, b) > 0;
    }

    default BitAndDistance computeBitAndDistance(TVector dot, TVector plane){
        double dotproduct = dotProduct(dot, plane);
        double distance = Math.abs(dotproduct) / vecLength(plane);
        return new BitAndDistance(dotproduct > 0, distance);
    }

    double dotProduct(TVector a, TVector b);

    double vecLength(TVector vec);

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
     * lucence score only accepts float number
     * @param query
     * @param doc
     * @return
     */
    default float cosineScore(TVector query, Document doc){
        TVector vec = getFeatureVector(doc.getField(IndexConst.VecFieldName).binaryValue().bytes);
        double dot = dotProduct(query, vec);
        return (float) (dot /(vecLength(query) * vecLength(vec)));
    }
}
