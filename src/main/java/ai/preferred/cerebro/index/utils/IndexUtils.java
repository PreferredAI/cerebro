package ai.preferred.cerebro.index.utils;

import ai.preferred.cerebro.index.common.VecHandler;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import ai.preferred.cerebro.core.entity.AbstractVector;

import java.io.*;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Utility class to facilitate all necessary function
 * in cerebro.index like I/O, vector computations..., etc.
 *
 * @author hpminh@apcs.vn
 */
public class IndexUtils{

    public static void saveVectorHandler(String filepath, VecHandler handler){
        Kryo kryo = new Kryo();
        kryo.register(String.class);
        try (Output output = new Output(new FileOutputStream(filepath))){
            kryo.writeObject(output, handler.getClass().getCanonicalName());
        } catch (
                FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static VecHandler loadVectorHandler(String filepath){
        VecHandler handler = null;
        Kryo kryo = new Kryo();
        kryo.register(String.class);
        try (Input input = new Input(new FileInputStream(filepath))){
            String className = kryo.readObject(input, String.class);
            Class<?> clazz = Class.forName(className);
            Constructor<?> constructor = clazz.getConstructor();
            handler = (VecHandler) constructor.newInstance(new Object[] {});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return handler;
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
    public static double[][] randomizeDoubleFeatureVectors(int n, int nFeatures, boolean splitFeature){
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
     *
     * @param n the number of vector to generate.
     * @param nFeatures the number of dimension each vector has.
     * @param splitFeature flag to specify whether the vector to
     *                     be used as hashing function or not.
     *                     If so the range of distribution range
     *                     from -1 to 1.
     * @return A set of randomized vectors.
     */
    public static float[][] randomizeFloatFeatureVectors(int n, int nFeatures, boolean splitFeature){
        Random random = new Random();
        float[][] res = new float[n][nFeatures];
        for (int i =0; i < n; i++){
            for(int j = 0; j < nFeatures; j++){
                if(splitFeature)
                    res[i][j] = (float) random.nextGaussian();
                else {
                    if(j == 0)
                        res[i][j] = 1.0f;
                    else
                        res[i][j] = (float) random.nextDouble();
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
     * @param data
     * @param filename
     *
     * Utility function for testing
     */
    public static void saveDoubleQueryAndTopK(HashMap<double[], ArrayList<Integer>> data, String filename){
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
    public static HashMap readDoubleQueryAndTopK(String filename) throws FileNotFoundException {
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
     * @param data
     * @param filename
     *
     * Utility function for testing
     */
    public static void saveFloatQueryAndTopK(HashMap<float[], ArrayList<Integer>> data, String filename){
        Kryo kryo = new Kryo();
        kryo.register(HashMap.class);
        kryo.register(float[].class);
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
    public static HashMap readFloatQueryAndTopK(String filename) throws FileNotFoundException {
        Kryo kryo = new Kryo();
        kryo.register(HashMap.class);
        kryo.register(float[].class);
        kryo.register(ArrayList.class);
        kryo.register(Integer.class);
        Input input = new Input(new FileInputStream(filename));
        HashMap arr= kryo.readObject(input, HashMap.class);
        input.close();
        return arr;
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

    public static boolean checkFileExist(File file){
        return file.exists() && !file.isDirectory();
    }
}

