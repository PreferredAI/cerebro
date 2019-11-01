package ai.preferred.cerebro.index.ids;

public class StringID implements ExternalID{
    private final String val;

    public StringID(String val) {
        this.val = val;
    }

    public byte[] getByteValues() {
        return val.getBytes();
    }
}
