package ai.preferred.cerebro.index.ids;

import ai.preferred.cerebro.index.utils.IndexUtils;


public class IntID extends ExternalID {
    private final int val;
    public IntID(int val){
        this.val = val;
    }

    public byte[] getByteValues() {
        byte[] bytes = IndexUtils.intToByte(val);
        return bytes;
    }

    @Override
    public int hashCode() {
        return val;
    }

    @Override
    public boolean equals(Object obj) {
        assert obj instanceof IntID;
        return val == ((IntID) obj).val;
    }
}
