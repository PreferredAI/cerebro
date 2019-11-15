package ai.preferred.cerebro.index.ids;

public class StringID extends ExternalID{
    private final String val;

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
