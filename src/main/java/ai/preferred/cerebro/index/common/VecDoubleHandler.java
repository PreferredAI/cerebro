package ai.preferred.cerebro.index.common;

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
public abstract class VecDoubleHandler implements VecHandler<double[]> {
    @Override
    public double dotProduct(double[] a, double[] b) {
        double re = 0;
        for (int i=0; i < a.length; i++){
            re += a[i] * b[i];
        }
        return re;
    }

    @Override
    public double vecLength(double[] vec) {
        double hold = 0;
        for (double v : vec) {
            hold += v * v;
        }
        return Math.sqrt(hold);
    }

    @Override
    public byte[] vecToBytes(double[] doublearr) {
        byte[] arr = new byte[doublearr.length * Double.BYTES];
        for(int i = 0; i < doublearr.length; i++){
            byte[] bytes = new byte[Double.BYTES];
            ByteBuffer.wrap(bytes).putDouble(doublearr[i]);
            System.arraycopy(bytes, 0, arr, i * Double.BYTES, bytes.length);
        }
        return arr;
    }

    @Override
    public double[] getFeatureVector(byte[] data) {
        assert data.length % Double.BYTES == 0;
        double[] doubles = new double[data.length / Double.BYTES];
        for(int i=0;i<doubles.length;i++){
            doubles[i] = ByteBuffer.wrap(data, i*Double.BYTES, Double.BYTES).getDouble();
        }
        return doubles;
    }

    @Override
    public void saveNodes(String vecFilename, Node<double[]>[] nodes, int nodeCount) {
        double[][] vecs = new double[nodeCount][];
        Node t;
        for (int i = 0; i < nodeCount; i++) {
            t = nodes[i];
            if (t != null)
                vecs[i] = (double[]) t.vector();
            else vecs[i] = null;
        }
        this.save(vecFilename, vecs);
    }

    @Override
    public void saveNodesBlocking(String vecFilename, AtomicReferenceArray<Node<double[]>> nodes, int nodeCount) {
        double[][] vecs = new double[nodeCount][];
        Node t;
        for (int i = 0; i < nodeCount; i++) {
            t = nodes.get(i);
            if (t != null)
                vecs[i] = (double[]) t.vector();
            else vecs[i] = null;
        }
        this.save(vecFilename, vecs);
    }

    @Override
    public void save(String vecFilename, double[][] vecs) {
        Kryo kryo = new Kryo();
        kryo.register(double[].class);
        kryo.register(double[][].class);
        try (Output output = new Output(new FileOutputStream(vecFilename))){
            kryo.writeObject(output, vecs);
        } catch (
                FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public double[][] load(File vecsFile) {
        Kryo kryo = new Kryo();
        kryo.register(double[].class);
        kryo.register(double[][].class);
        try (Input input = new Input(new FileInputStream(vecsFile))) {
            return kryo.readObject(input, double[][].class);
        } catch (
                FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
