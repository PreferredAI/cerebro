package ai.preferred.cerebro.index.hnsw;

import ai.preferred.cerebro.index.handler.VecHandler;
import ai.preferred.cerebro.index.hnsw.structure.BitSet;
import ai.preferred.cerebro.index.ids.ExternalID;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;
import static ai.preferred.cerebro.index.utils.IndexConst.Sp;

/**
 * @author hpminh@apcs.vn
 * @param <TVector>
 */
abstract public class HnswManager<TVector> {
    protected static final String globalConfigFileName = Sp + "global_config.o";
    protected static final String globalLookupFileName = Sp + "global_lookup.o";

    protected String idxDir;
    protected HnswConfiguration configuration;
    protected int nleaves;
    protected ConcurrentHashMap<ExternalID, Integer> lookup;
    protected GenericObjectPool<BitSet> visitedBitSetPool;
    protected LeafSegment<TVector>[] leaves;

    public HnswManager(){
    }

    //Load Up configuration and lookup table
    public HnswManager(String dir){
        idxDir = dir;
        Kryo kryo = new Kryo();
        kryo.register(Integer.class);
        kryo.register(ConcurrentHashMap.class);
        kryo.register(String.class);
        //Load up configuration
        Input input = null;
        try {
            input = new Input(new FileInputStream(idxDir + globalConfigFileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String className = kryo.readObject(input, String.class);
        VecHandler handler = null;
        try {
            Class<?> clazz = Class.forName(className);
            Constructor<?> constructor = clazz.getConstructor();
            handler = (VecHandler) constructor.newInstance(new Object[] {});
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        configuration = new HnswConfiguration(handler);
        configuration.setM(kryo.readObject(input, int.class));
        configuration.setEf(kryo.readObject(input, int.class));
        configuration.setEfConstruction(kryo.readObject(input, int.class));
        configuration.setEnableRemove(kryo.readObject(input, boolean.class));
        configuration.setLowMemoryMode(kryo.readObject(input, boolean.class));
        configuration.setMaxItemLeaf(kryo.readObject(input, int.class));
        nleaves = kryo.readObject(input, int.class);
        input.close();
        //Load up lookup table
        try {
            input = new Input(new FileInputStream(idxDir + globalLookupFileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        lookup = kryo.readObject(input, ConcurrentHashMap.class);
        input.close();
    }

    public HnswConfiguration getConfiguration() {
        return configuration;
    }
    public ConcurrentHashMap<ExternalID, Integer> getLookup(){
        return lookup;
    }
    public BitSet getBitsetFromPool(){
        return visitedBitSetPool.borrowObject();
    }

    public void returnBitsetToPool(BitSet bitSet){
        visitedBitSetPool.returnObject(bitSet);
    }

    public Node getNodeByExternalID(ExternalID extID){
        int globalID = lookup.getOrDefault(extID, -1);
        return getNode(globalID);
    }

    public Node getNode(int globalID){
        int leafNum = globalID / configuration.maxItemLeaf;
        int internalID = globalID % configuration.maxItemLeaf;
        if(leafNum >= 0 && leafNum < nleaves){
            return leaves[leafNum].getNode(internalID).get();
        }
        return null;
    }

    public ExternalID getExternalID(int globalID){
        return getNode(globalID).externalID();
    }

    static public void printIndexInfo(String idxFolder){
        Kryo kryo = new Kryo();
        kryo.register(Integer.class);
        kryo.register(ConcurrentHashMap.class);
        //Load up configuration
        Input input = null;
        try {
            input = new Input(new FileInputStream(idxFolder + globalConfigFileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int nConnsInHighLayer = kryo.readObject(input, int.class);

        kryo.readObject(input, int.class);
        kryo.readObject(input, int.class);
        boolean removeAllowed = kryo.readObject(input, boolean.class);
        boolean addSegmentOneByOne = kryo.readObject(input, boolean.class);
        int maxNodeCount = kryo.readObject(input, int.class);
        int numleaves = kryo.readObject(input, int.class);
        input.close();

        System.out.println("Index Info:");
        System.out.println("Number of outward connections per nodes in higher layer: " + nConnsInHighLayer);
        System.out.println("Number of outward connections per nodes in base layer: " + nConnsInHighLayer * 2);
        System.out.println("Node removal allowed: " + (removeAllowed ? "Yes" : "No"));
        System.out.println("Node insert modes: " + (addSegmentOneByOne ? "fill up one segment at a time" : "many segments at a time"));
        System.out.println("Maximum capacity of each leaf segment: " + maxNodeCount);
        System.out.println("Number of leaf segment: " + numleaves);
        System.out.println("Leaf segment info: ");
        for (int i = 0; i < numleaves; i++) {
            System.out.println(LeafSegment.capacityInfo(i, maxNodeCount, idxFolder));
        }

    }
}
