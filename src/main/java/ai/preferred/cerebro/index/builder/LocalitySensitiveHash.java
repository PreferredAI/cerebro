package ai.preferred.cerebro.index.builder;

import org.apache.lucene.util.BytesRef;

import ai.preferred.cerebro.index.utils.IndexUtils;

import java.util.BitSet;

/**
 * This class calculates the hashcode of a vector
 * given which set of hashing vectors it was
 * created with.
 *
 * @author hpminh@apcs.vn
 */
public class LocalitySensitiveHash<TVector> {
    protected final int numHashBit;
    protected HashBitComputer<TVector> bitComputer;
    protected FlipBitComputer<TVector> flipComputer;
    protected final TVector[] splitVecs;

    /**
     * Instantiate with a set of hashing vectors.
     * Calculating normal hashcodes
     * @param bitComputer
     * @param splitVecs the set of hashing vectors.
     */
    public LocalitySensitiveHash(HashBitComputer<TVector> bitComputer, TVector[] splitVecs){
        this(bitComputer, null, splitVecs);
    }

    /**
     * Instantiate with a set of hashing vectors.
     * Calculating both normal hashcodes and hashcodes with bit of the closest hashvec flipped
     * @param bitComputer
     * @param splitVecs the set of hashing vectors.
     */
    public LocalitySensitiveHash(FlipBitComputer<TVector> bitComputer, TVector[] splitVecs){
        this(null, bitComputer, splitVecs);
    }

    public LocalitySensitiveHash(HashBitComputer<TVector> bitComputer, FlipBitComputer<TVector> flipComputer, TVector[] splitVecs){
        this.bitComputer = bitComputer;
        this.flipComputer = flipComputer;
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

    /**
     * Return with two hashcodes - the usual and one with nearest bit flipped
     * @param features the vector to compute hashcode.
     * @return two hashcodes
     */
    public BytesRef[] getFlipHashBit(TVector features){
        BytesRef[] result = new BytesRef[2];
        BitSet hashbits = new BitSet(numHashBit);
        BitSet hashbitsflip = new BitSet(numHashBit);

        float curDistance = Float.MAX_VALUE;
        int indexShortest = -1;
        for(int i=0; i < numHashBit; i++){
            BitAndDistance bitAndDistance = flipComputer.compute(features, splitVecs[i]);
            hashbits.set(i,  bitAndDistance.bit);
            hashbitsflip.set(i,  bitAndDistance.bit);

            if(bitAndDistance.distance < curDistance){
                curDistance = bitAndDistance.distance;
                indexShortest = i;
            }
        }
        hashbitsflip.flip(indexShortest);
        result[0] = new BytesRef(hashbits.toByteArray());
        result[1] = new BytesRef(hashbitsflip.toByteArray());
        return result;
    }

    public interface HashBitComputer<TVector> {
        boolean compute(TVector a, TVector b);
    }

    public interface FlipBitComputer<TVector>{
        BitAndDistance compute(TVector a, TVector b);
    }
}
