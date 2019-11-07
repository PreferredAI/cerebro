package ai.preferred.cerebro.index.hnsw.structure;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Bitset for tracking visited nodes.
 */
public class BitSet implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int[] buffer;

    /**
     * Initializes a new instance of the {@link BitSet} class.
     *
     * @param count The number of bits in the set.
     */
    public BitSet(int count) {
        this.buffer = new int[(count >> 5) + 1];
    }

    /**
     * Checks whether the id-th bit is already set to true.
     *
     * @param id The identifier.
     * @return True if the identifier is in the set.
     */
    public boolean isTrue(int id) {
        int carrier = this.buffer[id >> 5];
        return ((1 << (id & 31)) & carrier) != 0;
    }

    /**
     * Set the bit at id-th position to 1.
     *
     * @param id The position to set to true
     */
    public void flipTrue(int id)  {
        int mask = 1 << (id & 31);
        this.buffer[id >> 5] |= mask;
    }

    /**
     * Set the bit at id-th position to 0.
     *
     * @param id The position to set to false
     */
    public void flipFalse(int id) {
        int mask = 1 << (id & 31);
        this.buffer[id >> 5] &= ~mask;
    }

    /**
     * Reset the bit set.
     */
    public void clear() {
        Arrays.fill(this.buffer, 0);
    }
}