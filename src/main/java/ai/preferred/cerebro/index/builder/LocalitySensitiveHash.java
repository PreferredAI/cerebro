package ai.preferred.cerebro.index.builder;

import org.apache.lucene.util.BytesRef;

import ai.preferred.cerebro.index.utils.IndexUtils;

import java.util.BitSet;

/**
 * This class calculate the hashcode of a vector
 * given which set of hashing vectors it was
 * created with.
 *
 * @author hpminh@apcs.vn
 */
public class LocalitySensitiveHash<TVector> {
    protected final int numHashBit;
    protected final HashBitComputer<TVector> bitComputer;
    protected final TVector[] splitVecs;

    /**
     * Instantiate with a set of hashing vectors.
     * @param bitComputer
     * @param splitVecs the set of hashing vectors.
     */
    public LocalitySensitiveHash(HashBitComputer<TVector> bitComputer, TVector[] splitVecs){
        this.bitComputer = bitComputer;
        assert splitVecs.length > 0;
        this.splitVecs =splitVecs;
        this.numHashBit = splitVecs.length;
    }

    /**
     * Calculate the hashcode of a vector.
     *
     * @param features the vector to compute hashcode.
     * @return the hashcode of the vector.
     */
    public BytesRef getHashBit(TVector features){
        BitSet hashbits = new BitSet(numHashBit);
        for(int i=0; i < numHashBit; i++){
            hashbits.set(i, bitComputer.compute(features, splitVecs[i]));
        }
        return new BytesRef(hashbits.toByteArray());
    }
}
