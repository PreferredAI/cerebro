package ai.preferred.cerebro.common;

import ai.preferred.cerebro.index.utils.IndexUtils;


public class IntID implements ExternalID {
    private final int val;
    public IntID(int val){
        this.val = val;
    }

    public byte[] getByteValues() {
        byte[] bytes = IndexUtils.intToByte(val);
        return bytes;
    }
}
