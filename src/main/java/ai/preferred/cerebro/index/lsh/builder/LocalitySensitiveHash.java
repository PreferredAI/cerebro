package ai.preferred.cerebro.index.lsh.builder;

import ai.preferred.cerebro.index.common.BitAndDistance;
import ai.preferred.cerebro.index.common.VecHandler;
import org.apache.lucene.util.BytesRef;

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

    public VecHandler<TVector> getHandler() {
        return handler;
    }

    protected final VecHandler<TVector> handler;
    protected final TVector[] splitVecs;


    /**
     * Instantiate with a set of hashing vectors.
     * Calculating normal hashcodes
     * @param splitVecs the set of hashing vectors.
     * @param handler dummy class holding detailed implementations of many vector operations
     */
    public LocalitySensitiveHash(VecHandler<TVector> handler, TVector[] splitVecs){
        this.handler = handler;
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
            hashbits.set(i, handler.computeBit(features, splitVecs[i]));
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

        double curDistance = Double.MAX_VALUE;
        int indexShortest = -1;
        for(int i=0; i < numHashBit; i++){
            BitAndDistance bitAndDistance = handler.computeBitAndDistance(features, splitVecs[i]);
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
}
