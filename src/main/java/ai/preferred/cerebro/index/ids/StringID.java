package ai.preferred.cerebro.index.ids;

/**
 * @author hpminh@apcs.vn
 */
public class StringID extends ExternalID{
    private String val;

    public String getVal() {
        return val;
    }

    public StringID(){};

    public StringID(String val) {
        this.val = val;
    }

    public byte[] getByteValues() {
        return val.getBytes();
    }

    @Override
    public boolean equals(Object obj) {
        return val.equals(obj);
    }
}
