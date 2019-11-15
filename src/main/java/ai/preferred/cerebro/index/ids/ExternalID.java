package ai.preferred.cerebro.index.ids;

import java.util.Arrays;

/**
 * Since this library only supports index creation and searching on built index,
 * There must be a database storing the original form of the data somewhere else.
 * The main idea here is that searchers from this library will return IDs of item,
 * then from these IDs go to the database and get the actual objects.
 */
public abstract class ExternalID {

    public abstract byte[] getByteValues();

    @Override
    public int hashCode() {
        return Arrays.hashCode(getByteValues());
    }

    @Override
    public boolean equals(Object obj) {
        assert obj instanceof ExternalID;
        ExternalID ext = (ExternalID) obj;
        return Arrays.equals(getByteValues(), ext.getByteValues());
    }
}
