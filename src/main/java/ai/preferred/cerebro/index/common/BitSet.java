package ai.preferred.cerebro.index.common;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Bitset for tracking visited nodes.
 */
public class BitSet implements Serializable {
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
     * Reverse the value at the id-th bit
     * @param id
     */
    public void flip(int id){
        if(isTrue(id)){
            flipFalse(id);
            return;
        }
        flipTrue(id);
    }

    /**
     * Set the bit at id-th position to the specified value.
     * @param id
     * @param val
     */
    public void set(int id, boolean val){
        if(val){
            flipTrue(id);
            return;
        }
        flipFalse(id);
    }

    /**
     * convert the Bitset into a byte array
     * @return
     */
    public byte[] toByteArray(){
        byte[] res = new byte[buffer.length * Integer.BYTES];
        for(int i = 0; i < buffer.length; i++){
            byte[] bytes = new byte[Integer.BYTES];
            ByteBuffer.wrap(bytes).putInt(buffer[i]);
            System.arraycopy(bytes, 0, res, i * Integer.BYTES, Integer.BYTES);
        }
        return res;
    }

    /**
     * Reset the bit set.
     */
    public void clear() {
        Arrays.fill(this.buffer, 0);
    }
}