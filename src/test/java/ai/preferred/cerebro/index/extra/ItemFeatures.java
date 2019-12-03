package ai.preferred.cerebro.index.extra;

/**
 * This class is created for testing purposes.
 * Not to be used in real deployment.
 */
public class ItemFeatures<TVector> extends Object{
    public final int docID;
    public final TVector features;
    public double similarity = 0;
    //public ItemFeatures nextItem;
    public ItemFeatures(){
        docID = -1;
        features = null;
    }
    public ItemFeatures(int id,TVector features){
        this.docID = id;
        this.features = features;
    }

    @Override
    public String toString(){
        return Integer.toString(docID);
    }
}

