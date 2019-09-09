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
public class LocalitySensitiveHash {
    protected final int numHashBit;
    protected final int dimension;
    protected final double [][] splitVecs;

    /**
     * Instantiate with a set of hashing vectors.
     * @param splitVecs the set of hashing vectors.
     */
    public LocalitySensitiveHash(double [][] splitVecs){
        assert splitVecs.length > 0;
        this.splitVecs =splitVecs;
        this.numHashBit = splitVecs.length;
        this.dimension = splitVecs[0].length;
    }

    /**
     * Calculate the hashcode of a vector.
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

    /**
     * Return with two hashcodes - the usual and one with nearest bit flipped
     * @param features the vector to compute hashcode.
     * @return two hashcodes
     */
    public BytesRef[] getHashBitWithFlipClosest(double [] features){
        BytesRef[] result = new BytesRef[2];
        BitSet hashbits = new BitSet(numHashBit);
        BitSet hashbitsflip = new BitSet(numHashBit);

        double curDistance = Double.MAX_VALUE;
        int indexShortest = -1;
        for(int i=0; i < numHashBit; i++){
            double dotProduct = IndexUtils.dotProduct(features, splitVecs[i]);
            hashbits.set(i,  dotProduct > 0);
            hashbitsflip.set(i,  dotProduct > 0);
            double distance = Math.abs(dotProduct) / IndexUtils.vecLength(splitVecs[i]);
            if(distance < curDistance){
                curDistance = distance;
                indexShortest = i;
            }
        }
        hashbitsflip.flip(indexShortest);
        result[0] = new BytesRef(hashbits.toByteArray());
        result[1] = new BytesRef(hashbitsflip.toByteArray());
        return result;
    }
}
