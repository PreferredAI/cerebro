package ai.preferred.cerebro.index.utils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

public class VecFloatHandler implements VecHandler<float[]> {
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
    public float[][] load(String vecFilename) {
        Kryo kryo = new Kryo();
        kryo.register(float[].class);
        kryo.register(float[][].class);
        try (Input input = new Input(new FileInputStream(vecFilename))) {
            return kryo.readObject(input, float[][].class);
        } catch (
                FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    public double dotProduct(float [] a, float [] b){
        float re = 0;
        for (int i=0; i < a.length; i++){
            re += a[i] * b[i];
        }
        return re;
    }

    public double vecLength(float[] vec) {
        float hold = 0;
        for (float v : vec) {
            hold += v * v;
        }
        return Math.sqrt(hold);
    }

    public byte[] vecToBytes(float[] floatarr){
        byte[] arr = new byte[floatarr.length * Float.BYTES];
        for(int i = 0; i < floatarr.length; i++){
            byte[] bytes = new byte[Float.BYTES];
            ByteBuffer.wrap(bytes).putFloat(floatarr[i]);
            System.arraycopy(bytes, 0, arr, i * Float.BYTES, bytes.length);
        }
        return arr;
    }


    public float[] getFeatureVector(byte[] data){
        assert data.length % Float.BYTES == 0;
        float[] floats = new float[data.length / Float.BYTES];
        for(int i=0;i<floats.length;i++){
            floats[i] = ByteBuffer.wrap(data, i*Float.BYTES, Float.BYTES).getFloat();
        }
        return floats;
    }
}
