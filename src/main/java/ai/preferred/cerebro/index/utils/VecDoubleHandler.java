package ai.preferred.cerebro.index.utils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

public class VecDoubleHandler implements VecHandler<double[]> {
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

    public double[][] load(String vecFilename) {
        Kryo kryo = new Kryo();
        kryo.register(double[].class);
        kryo.register(double[][].class);
        try (Input input = new Input(new FileInputStream(vecFilename))) {
            return kryo.readObject(input, double[][].class);
        } catch (
                FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public double dotProduct(double[] a, double[] b){
        double re = 0;
        for (int i=0; i < a.length; i++){
            re += a[i] * b[i];
        }
        return re;
    }


    public double vecLength(double[] vec) {
        double hold = 0;
        for (double v : vec) {
            hold += v * v;
        }
        return Math.sqrt(hold);
    }

    public byte[] vecToBytes(double[] doublearr){
        byte[] arr = new byte[doublearr.length * Double.BYTES];
        for(int i = 0; i < doublearr.length; i++){
            byte[] bytes = new byte[Double.BYTES];
            ByteBuffer.wrap(bytes).putDouble(doublearr[i]);
            System.arraycopy(bytes, 0, arr, i * Double.BYTES, bytes.length);
        }
        return arr;
    }

    public double[] getFeatureVector(byte[] data){
        assert data.length % Double.BYTES == 0;
        double[] doubles = new double[data.length / Double.BYTES];
        for(int i=0;i<doubles.length;i++){
            doubles[i] = ByteBuffer.wrap(data, i*Double.BYTES, Double.BYTES).getDouble();
        }
        return doubles;
    }
}
