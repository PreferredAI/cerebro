package ai.preferred.cerebro.index.handler;

import ai.preferred.cerebro.index.hnsw.Node;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReferenceArray;
/**
 * @author hpminh@apcs.vn
 */
public abstract class VecFloatHandler implements VecHandler<float[]> {
    @Override
    public double dotProduct(float[] a, float[] b) {
        float re = 0;
        for (int i=0; i < a.length; i++){
            re += a[i] * b[i];
        }
        return re;
    }

    @Override
    public double vecLength(float[] vec) {
        float hold = 0;
        for (float v : vec) {
            hold += v * v;
        }
        return Math.sqrt(hold);
    }

    @Override
    public byte[] vecToBytes(float[] floatarr) {
        byte[] arr = new byte[floatarr.length * Float.BYTES];
        for(int i = 0; i < floatarr.length; i++){
            byte[] bytes = new byte[Float.BYTES];
            ByteBuffer.wrap(bytes).putFloat(floatarr[i]);
            System.arraycopy(bytes, 0, arr, i * Float.BYTES, bytes.length);
        }
        return arr;
    }

    @Override
    public float[] getFeatureVector(byte[] data) {
        assert data.length % Float.BYTES == 0;
        float[] floats = new float[data.length / Float.BYTES];
        for(int i=0;i<floats.length;i++){
            floats[i] = ByteBuffer.wrap(data, i*Float.BYTES, Float.BYTES).getFloat();
        }
        return floats;
    }

    @Override
    public void saveNodes(String vecFilename, Node<float[]>[] nodes, int nodeCount) {
        float[][] vecs = new float[nodeCount][];
        Node t;
        for (int i = 0; i < nodeCount; i++) {
            t = nodes[i];
            if (t != null)
                vecs[i] = (float[]) t.vector();
            else vecs[i] = null;
        }
        this.save(vecFilename, vecs);
    }

    @Override
    public void saveNodesBlocking(String vecFilename, AtomicReferenceArray<Node<float[]>> nodes, int nodeCount) {
        float[][] vecs = new float[nodeCount][];
        Node t;
        for (int i = 0; i < nodeCount; i++) {
            t = nodes.get(i);
            if (t != null)
                vecs[i] = (float[]) t.vector();
            else vecs[i] = null;
        }
        this.save(vecFilename, vecs);
    }

    @Override
    public void save(String vecFilename, float[][] vecs) {
        Kryo kryo = new Kryo();
        kryo.register(float[].class);
        kryo.register(float[][].class);
        try (Output output = new Output(new FileOutputStream(vecFilename))){
            kryo.writeObject(output, vecs);
        } catch (
                FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public float[][] load(File vecsFile) {
        Kryo kryo = new Kryo();
        kryo.register(float[].class);
        kryo.register(float[][].class);
        try (Input input = new Input(new FileInputStream(vecsFile))) {
            return kryo.readObject(input, float[][].class);
        } catch (
                FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
