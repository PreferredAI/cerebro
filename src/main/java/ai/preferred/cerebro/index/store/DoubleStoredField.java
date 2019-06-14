package ai.preferred.cerebro.index.store;

import org.apache.lucene.document.StoredField;
import org.apache.lucene.util.BytesRef;

import ai.preferred.cerebro.index.utils.IndexConst;

import java.nio.ByteBuffer;

/**
 * Cerebro's class to store a double number into Lucene's index.
 */
public class DoubleStoredField extends StoredField {
    private DoubleStoredField(String name, double d){
        super(name, new BytesRef(doubleToBytes(d)));
    }

    public DoubleStoredField(double d){
        super(IndexConst.VecLenFieldName, new BytesRef(doubleToBytes(d)));
    }

    public static byte[] doubleToBytes(double d){
        byte[] arr = new byte[Double.BYTES];
        ByteBuffer.wrap(arr).putDouble(d);
        return arr;
    }

    public static double bytesToDouble(byte[] data){
        assert data.length == Double.BYTES;
        return ByteBuffer.wrap(data, 0, Double.BYTES).getDouble();
    }
}
