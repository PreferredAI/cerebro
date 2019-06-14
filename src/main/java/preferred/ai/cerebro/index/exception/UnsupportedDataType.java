package preferred.ai.cerebro.index.exception;


/**
 * Thrown whenever there is a mismatch in the datatype
 * of objects.
 */
public class UnsupportedDataType extends Exception {
    public UnsupportedDataType() {
        super("There is problem with data type");
    }
}
