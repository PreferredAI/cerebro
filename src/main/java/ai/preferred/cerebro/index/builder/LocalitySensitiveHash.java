package ai.preferred.cerebro.index.builder;

import org.apache.lucene.util.BytesRef;

import ai.preferred.cerebro.index.utils.IndexUtils;

import java.util.BitSet;

/**
 * This class calculate the hashcode of a vector
 * given which set of hashing vectors it was
 * created with.
 */
public class LocalitySensitiveHash {
    protected final int numHashBit;
    protected final int dimension;
    protected final double [][] splitVecs;

    /**
     *
     * @param splitVecs the set of hashing vectors.
     */
    public LocalitySensitiveHash(double [][] splitVecs){
        assert splitVecs.length > 0;
        this.splitVecs =splitVecs;
        this.numHashBit = splitVecs.length;
        this.dimension = splitVecs[0].length;
    }

    /**
     *
     * @param features the vector to compute hashcode.
     * @return the hashcode of the vector.
     */
    public BytesRef getHashBit(double [] features){
        assert features.length == dimension;
        BitSet hashbits = new BitSet(numHashBit);
        for(int i=0; i < numHashBit; i++){
            hashbits.set(i, IndexUtils.dotProduct(features, splitVecs[i]) > 0);
        }
        return new BytesRef(hashbits.toByteArray());
    }
}
