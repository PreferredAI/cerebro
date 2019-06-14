package ai.preferred.cerebro.index.store;

import org.apache.lucene.document.StoredField;
import org.apache.lucene.util.BytesRef;

import ai.preferred.cerebro.index.utils.IndexConst;

import java.nio.ByteBuffer;

/**
 * Cerebro's class to store a vector into Lucene's index.
 */
public class VectorField extends StoredField {

    private VectorField(String name, double[] data){
        super(name, new BytesRef(vecToBytes(data)));
    }

    public VectorField(double[] data){
        super(IndexConst.VecFieldName, new BytesRef(vecToBytes(data)));
    }

    /**
     * @param doublearr
     * @return byte encoding of the vector.
     *
     * Encoding a vector into an array of byte.
     */
    public static byte[] vecToBytes(double[] doublearr){
        byte[] arr = new byte[doublearr.length * Double.BYTES];
        for(int i = 0; i < doublearr.length; i++){
            byte[] bytes = new byte[Double.BYTES];
            ByteBuffer.wrap(bytes).putDouble(doublearr[i]);
            System.arraycopy(bytes, 0, arr, i * Double.BYTES, bytes.length);
        }
        return arr;
    }

    /**
     * @param data
     * @return vector values of a byte array.
     *
     * Decode a byte array back into a vector.
     */
    public static double[] getFeatureVector(byte[] data){
        assert data.length % Double.BYTES == 0;
        double[] doubles = new double[data.length / Double.BYTES];
        for(int i=0;i<doubles.length;i++){
            doubles[i] = ByteBuffer.wrap(data, i*Double.BYTES, Double.BYTES).getDouble();
        }
        return doubles;
    }


}
