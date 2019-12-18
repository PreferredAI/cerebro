package ai.preferred.cerebro.index.hnsw;

import ai.preferred.cerebro.index.common.VecHandler;

import java.util.Comparator;


/**
 * @author hpminh@apcs.vn
 */
public class HnswConfiguration {
    private static final int DEFAULT_M = 10;
    private static final int DEFAULT_EF = 10;
    private static final int DEFAULT_EF_CONSTRUCTION = 200;
    private static final boolean DEFAULT_REMOVE_ENABLED = true;
    private static final int DEFAULT_MAX_ITEM = 2_000_000;
    private static final boolean DEFAULT_MEMORY_MODE = false;
    private static final boolean DEFAULT_HEURISTIC_MODE = true;

    VecHandler handler;
    Comparator distanceComparator;

    public void setMaxItemLeaf(int maxItemLeaf) {
        this.maxItemLeaf = maxItemLeaf;
    }

    public int getMaxItemLeaf() {
        return maxItemLeaf;
    }

    int maxItemLeaf;

    public VecHandler getHandler() {
        return handler;
    }

    public int getM() {
        return m;
    }

    public int getEf() {
        return ef;
    }

    public int getEfConstruction() {
        return efConstruction;
    }

    public boolean isRemoveEnabled() {
        return removeEnabled;
    }

    int m = DEFAULT_M;
    int ef = DEFAULT_EF;
    int efConstruction = DEFAULT_EF_CONSTRUCTION;
    boolean removeEnabled = DEFAULT_REMOVE_ENABLED;

    //Each leaf will be assign one thread to execute
    //independently. That way no synchronization is needed
    //and speed up the insert time which will become very slow
    //with large #n of nodes. Recommended when on average
    // amount of nodes each leaf > 5M


    boolean useHeuristic = DEFAULT_HEURISTIC_MODE;
    boolean lowMemoryMode = DEFAULT_MEMORY_MODE;

    public HnswConfiguration(VecHandler handler) {
        this.handler = handler;
        this.distanceComparator = Comparator.naturalOrder();
        this.maxItemLeaf = DEFAULT_MAX_ITEM;
    }

    public HnswConfiguration(VecHandler handler, int maxItemCount) {
        this.handler = handler;
        this.distanceComparator = Comparator.naturalOrder();
        this.maxItemLeaf = maxItemCount;
    }

    /**
     * Sets the number of bi-directional links created for every new element during construction. Reasonable range
     * for m is 2-100. Higher m work better on datasets with high intrinsic dimensionality and/or high recall,
     * while low m work better for datasets with low intrinsic dimensionality and/or low recalls. The parameter
     * also determines the algorithm's memory consumption.
     * As an example for d = 4 random vectors optimal m for search is somewhere around 6, while for high dimensional
     * datasets (word embeddings, good face descriptors), higher M are required (e.g. m = 48, 64) for optimal
     * performance at high recall. The range mM = 12-48 is ok for the most of the use cases. When m is changed one
     * has to update the other parameters. Nonetheless, ef and efConstruction parameters can be roughly estimated by
     * assuming that m * efConstruction is a constant.
     *
     * @param m the number of bi-directional links created for every new element during construction
     */
    public void setM(int m) {
        this.m = m;
    }

    /**
     * `
     * The parameter has the same meaning as ef, but controls the index time / index precision. Bigger efConstruction
     * leads to longer construction, but better index quality. At some point, increasing efConstruction does not
     * improve the quality of the index. One way to check if the selection of ef_construction was ok is to measure
     * a recall for M nearest neighbor search when ef = efConstruction: if the recall is lower than 0.9, then
     * there is room for improvement.
     *
     * @param efConstruction controls the index time / index precision
     */
    public void setEfConstruction(int efConstruction) {
        this.efConstruction = efConstruction;
    }

    /**
     * The size of the dynamic list for the nearest neighbors (used during the search). Higher ef leads to more
     * accurate but slower search. The value ef of can be anything between k and the size of the dataset.
     *
     * @param ef size of the dynamic list for the nearest neighbors
     */
    public void setEf(int ef) {
        this.ef = ef;
    }

    /**
     * Call to enable support for the experimental remove operation. Indices that support removes will consume more
     * memory.
     */
    public void setEnableRemove(boolean removeEnabled) {
        this.removeEnabled = removeEnabled;
    }


    public boolean isLowMemoryMode() {
        return lowMemoryMode;
    }


    public void setLowMemoryMode(boolean lowMemoryMode) {
        this.lowMemoryMode = lowMemoryMode;
    }
}
