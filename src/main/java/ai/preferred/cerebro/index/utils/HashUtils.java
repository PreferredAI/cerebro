package ai.preferred.cerebro.index.utils;

import ai.preferred.cerebro.index.builder.BitAndDistance;

import static ai.preferred.cerebro.index.utils.IndexUtils.*;

public class HashUtils {

    public static boolean computeBitFloat(float[] a, float[] b){
        return dotProductFloat(a, b) > 0;
    }

    public static boolean computeBitDouble(double[] a, double[] b){
        return dotProductDouble(a, b) > 0;
    }

    public static BitAndDistance computeBitAndDistanceFloat(float[] dot, float[] plane){
        float dotproduct = dotProductFloat(dot, plane);
        float distance = Math.abs(dotproduct) / floatVecLength(plane);
        return new BitAndDistance(dotproduct > 0, distance);
    }

    public static BitAndDistance computeBitAndDistanceDouble(double[] dot, double[] plane){
        double dotproduct = dotProductDouble(dot, plane);
        float distance = (float) (Math.abs(dotproduct) / doubleVecLength(plane));
        return new BitAndDistance(dotproduct > 0, distance);
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
     * Calculate the the inner product between 2 float vectors
     */
    static public float dotProductFloat(float [] a, float [] b){
        float re = 0;
        for (int i=0; i < a.length; i++){
            re += a[i] * b[i];
        }
        return re;
    }

    /**
     * @param vec
     * @return The Euclidean length of double vector passed in
     */
    static public double doubleVecLength(double[] vec) {
        double hold = 0;
        for (int i = 0; i < vec.length; i++) {
            hold += vec[i] * vec[i];
        }
        return Math.sqrt(hold);
    }

    /**
     * @param vec
     * @return The Euclidean length of float vector passed in
     */
    static public float floatVecLength(float[] vec) {
        float hold = 0;
        for (int i = 0; i < vec.length; i++) {
            hold += vec[i] * vec[i];
        }
        return (float) Math.sqrt(hold);
    }
}
