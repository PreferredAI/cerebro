package ai.preferred.cerebro.index.utils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import ai.preferred.cerebro.core.entity.AbstractVector;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Utility class to facilitate all necessary function
 * in cerebro.index like I/O, vector computations..., etc.
 *
 * @author hpminh@apcs.vn
 */
public class IndexUtils {


    public static void notifyFutureImplementation(){
        System.out.println("Warning: To be Implemented");
    }

    public static void notifyLazyImplementation(String msg){
        System.out.println("Lazy impl: " + msg);
    }

    /**
     *
     * @param n the number of vector to generate.
     * @param nFeatures the number of dimension each vector has.
     * @param splitFeature flag to specify whether the vector to
     *                     be used as hashing function or not.
     *                     If so the range of distribution range
     *                     from -1 to 1.
     * @return A set of randomized vectors.
     */
    public static double[][] randomizeFeatureVectors(int n, int nFeatures, boolean splitFeature){
        Random random = new Random();
        double[][] res = new double[n][nFeatures];
        for (int i =0; i < n; i++){
            for(int j = 0; j < nFeatures; j++){
                if(splitFeature)
                    res[i][j] = random.nextGaussian();
                else {
                    if(j == 0)
                        res[i][j] = 1.0;
                    else
                        res[i][j] = random.nextDouble();
                }
            }
        }
        return res;
    }

    /**
     * @param aVector
     * @return
     *
     * Function to transform a {@link AbstractVector} instance to
     * a double vector
     */
    public static double[] toDoubles(AbstractVector aVector){
        double[] elements = new double[aVector.length()];
        for (int id = 0; id < aVector.length(); id ++){
            elements[id] = aVector.getElement(id);
        }
        return elements;
    }


    /**
     *
     * @param nFeatures
     * @return a single vector
     */
    public static double[] randomizeQueryVector(int nFeatures){
        Random random = new Random();
        double[] re = new double[nFeatures];
        for(int i =0; i < nFeatures; i++){
            re[i] = random.nextDouble() * 2 - 1;
        }
        return re;
    }

    /**
     * @param splitVector
     * @param filename
     *
     * Save a set of vectors to hard disk in the specified path
     */
    public static void saveVectors(double [][] splitVector, String filename){
        Kryo kryo = new Kryo();
        kryo.register(double[][].class);
        kryo.register(double[].class);
        try {
            Output output = new Output(new FileOutputStream(filename));
            kryo.writeObject(output, splitVector);
            output.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param filename
     * @return A set of vectors from the specified filename
     * @throws IOException
     *
     * Load a set of vectors from the specified filename
     */
    public static double[][] readVectors(String filename) throws IOException {
        Kryo kryo = new Kryo();
        kryo.register(double[][].class);
        kryo.register(double[].class);
        Input input = new Input(new FileInputStream(filename));
        double[][] arr= kryo.readObject(input, double[][].class);
        input.close();
        return arr;
    }

    /**
     * @param data
     * @param filename
     *
     * Utility function for testing
     */
    public static void saveQueryAndTopK(HashMap<double[], ArrayList<Integer>> data, String filename){
        Kryo kryo = new Kryo();
        kryo.register(HashMap.class);
        kryo.register(double[].class);
        kryo.register(ArrayList.class);
        kryo.register(Integer.class);
        try {
            Output output = new Output(new FileOutputStream(filename));
            kryo.writeObject(output, data);
            output.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param filename
     * @return
     * @throws FileNotFoundException
     *
     * Utility function for testing
     */
    public static HashMap readQueryAndTopK(String filename) throws FileNotFoundException {
        Kryo kryo = new Kryo();
        kryo.register(HashMap.class);
        kryo.register(double[].class);
        kryo.register(ArrayList.class);
        kryo.register(Integer.class);
        Input input = new Input(new FileInputStream(filename));
        HashMap arr= kryo.readObject(input, HashMap.class);
        input.close();
        return arr;
    }


    /**
     * Calculate the the inner product between 2 double vectors
     */
    static public double dotProductDouble(double [] a, double [] b){
        double re = 0;
        for (int i=0; i < a.length; i++){
            re += a[i] * b[i];
        }
        return re;
    }

    /**
     * Calculate the the inner product between 2 double vectors
     */
    static public double dotProductFloat(float [] a, float [] b){
        double re = 0;
        for (int i=0; i < a.length; i++){
            re += a[i] * b[i];
        }
        return re;
    }

    /**
     * @param vec
     * @return The Euclidean length of vector passed in
     */
    static public double vecLength(double[] vec) {
        double hold = 0;
        for (int i = 0; i < vec.length; i++) {
            hold += vec[i] * vec[i];
        }
        return Math.sqrt(hold);
    }

    /**
     * @param num
     * @return The byte encoding of an integer
     */
    public static byte[] intToByte(int num){
        byte[] bytes = new byte[Integer.BYTES];
        ByteBuffer.wrap(bytes).putInt(num);
        return bytes;
    }

    /**
     * @param bytes
     * @return The corresponding integer value to a byte array
     */
    public static int byteToInt(byte[] bytes){
        return ByteBuffer.wrap(bytes, 0, Integer.BYTES).getInt();
    }



    /**
     * Encoding a vector into an array of byte.
     *
     * @param doublearr The vector to be encoded to bytes.
     * @return byte encoding of the vector.
     */
    public static byte[] doubleVecToBytes(double[] doublearr){
        byte[] arr = new byte[doublearr.length * Double.BYTES];
        for(int i = 0; i < doublearr.length; i++){
            byte[] bytes = new byte[Double.BYTES];
            ByteBuffer.wrap(bytes).putDouble(doublearr[i]);
            System.arraycopy(bytes, 0, arr, i * Double.BYTES, bytes.length);
        }
        return arr;
    }

    /**
     * Decode a byte array back into a vector.
     *
     * @param data The data to be decoded back to a vector.
     * @return vector values of a byte array.
     */
    public static double[] getDoubleFeatureVector(byte[] data){
        assert data.length % Double.BYTES == 0;
        double[] doubles = new double[data.length / Double.BYTES];
        for(int i=0;i<doubles.length;i++){
            doubles[i] = ByteBuffer.wrap(data, i*Double.BYTES, Double.BYTES).getDouble();
        }
        return doubles;
    }

    /**
     * Encoding a vector into an array of byte.
     *
     * @param floatarr The vector to be encoded to bytes.
     * @return byte encoding of the vector.
     */
    public static byte[] floatVecToBytes(float[] floatarr){
        byte[] arr = new byte[floatarr.length * Float.BYTES];
        for(int i = 0; i < floatarr.length; i++){
            byte[] bytes = new byte[Float.BYTES];
            ByteBuffer.wrap(bytes).putFloat(floatarr[i]);
            System.arraycopy(bytes, 0, arr, i * Float.BYTES, bytes.length);
        }
        return arr;
    }

    /**
     * Decode a byte array back into a vector.
     *
     * @param data The data to be decoded back to a vector.
     * @return vector values of a byte array.
     */
    public static float[] getFloatFeatureVector(byte[] data){
        assert data.length % Float.BYTES == 0;
        float[] floats = new float[data.length / Float.BYTES];
        for(int i=0;i<floats.length;i++){
            floats[i] = ByteBuffer.wrap(data, i*Float.BYTES, Float.BYTES).getFloat();
        }
        return floats;
    }

    public static boolean computeBitFloat(float[] a, float[] b){
        return dotProductFloat(a, b) > 0;
    }

    public static boolean computeBitDouble(double[] a, double[] b){
        return dotProductDouble(a, b) > 0;
    }

}

