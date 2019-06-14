package preferred.ai.cerebro.index.demo;

import preferred.ai.cerebro.index.utils.IndexUtils;

/**
 * This class is created for testing purposes.
 * Not to be used in real deployment.
 */
public class ItemFeatures extends Object{
    public final int docID;
    public final double [] features;
    public double vecLength = -1.0;
    public double similarity = Double.MIN_VALUE;
    //public ItemFeatures nextItem;
    public ItemFeatures(int id, double[] features){
        this.docID = id;
        this.features = features;
        vecLength = IndexUtils.vecLength(features);
    }

    @Override
    public String toString(){
        return Integer.toString(docID);
    }
}

