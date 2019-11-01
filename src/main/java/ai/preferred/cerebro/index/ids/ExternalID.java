package ai.preferred.cerebro.index.ids;

/**
 * Since this library only supports index creation and searching on built index,
 * There must be a database storing the original form of the data somewhere else.
 * The main idea here is that searchers from this library will return IDs of item,
 * then from these IDs go to the database and get the actual objects.
 */
public interface ExternalID {
    byte[] getByteValues();
}
