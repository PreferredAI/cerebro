package ai.preferred.cerebro.index.utils;

import ai.preferred.cerebro.index.common.VecHandler;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;


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
    public static void serializeObject(Serializable obj, String filename){
        try
        {
            //Saving of object in a file
            FileOutputStream file = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(file);

            // Method for serialization of object
            out.writeObject(obj);

            out.close();
            file.close();

            System.out.println("Object has been serialized");

        }

        catch(IOException ex)
        {
            System.out.println("IOException is caught");
        }
    }

    public static Object deserializeObject(String filename){
        Object obj = null;
        try
        {
            // Reading the object from a file
            FileInputStream file = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(file);

            // Method for deserialization of object
            obj = in.readObject();

            in.close();
            file.close();
        }

        catch(IOException ex)
        {
            System.out.println("IOException is caught");
        }

        catch(ClassNotFoundException ex)
        {
            System.out.println("ClassNotFoundException is caught");
        }
        return obj;
    }
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
                    res[i][j] = 2 * random.nextFloat() - 1;
                    /*if(j == 0)
                        res[i][j] = 1.0f;
                    else
                        res[i][j] = (float) random.nextDouble();

                     */
                }
            }
        }
        return res;
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
     *
     * @param dir
     * @param hashMap
     * @param subclasses
     */
    public static void saveHashMap(String dir, HashMap hashMap, Class... subclasses){
        Kryo kryo = new Kryo();
        kryo.register(HashMap.class);
        for (Class clazz: subclasses) {
            kryo.register(clazz);
        }
        try {
            Output output = new Output(new FileOutputStream(dir));
            kryo.writeObject(output, hashMap);
            output.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param dir
     * @param subclasses
     * @return
     */
    public static HashMap loadHashMap(String dir, Class... subclasses){
        Kryo kryo = new Kryo();
        kryo.register(HashMap.class);
        for (Class clazz: subclasses) {
            kryo.register(clazz);
        }
        Input input = null;
        try {
            input = new Input(new FileInputStream(dir));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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

    public static void ensureDirExist(String dir){
        File file = new File(dir);
        if (!file.exists() || !file.isDirectory()){
            file.mkdir();
        }
    }


    public static void saveFloat2D(String vecFilename, float[][] vecs) {
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


    public static float[][] loadFloat2D(File vecsFile) {
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

    public static void saveDouble2D(String vecFilename, double[][] vecs) {
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


    public static double[][] loadDouble2D(File vecsFile) {
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

    public static int countIntersection(int[] returnedIds, int[] truthIds){
        int res = 0;
        HashSet<Integer> hashSet = new HashSet<>(truthIds.length);
        for (int id: truthIds) {
            hashSet.add(id);
        }
        for (int id: returnedIds) {
            if (hashSet.contains(id))
                res++;
        }
        return res;
    }
}

