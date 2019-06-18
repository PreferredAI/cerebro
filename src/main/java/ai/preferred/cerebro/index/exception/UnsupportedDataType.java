package ai.preferred.cerebro.index.exception;


/**
 *
 * Thrown whenever there is a mismatch in the datatype
 * of objects.
 *
 * @author hpminh@apcs.vn
 */
public class UnsupportedDataType extends Exception {
    public UnsupportedDataType() {
        super("There is problem with data type");
    }
}
