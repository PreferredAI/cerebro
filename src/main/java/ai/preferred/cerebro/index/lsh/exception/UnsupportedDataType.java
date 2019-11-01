package ai.preferred.cerebro.index.lsh.exception;


/**
 *
 * Thrown whenever there is a mismatch in the datatype
 * of objects.
 *
 * @author hpminh@apcs.vn
 */
public class UnsupportedDataType extends Exception {
    public UnsupportedDataType(Class classType) {
        super("the data type \"" + classType +"\" you are working with is not " +
                "supported by default. Define your own function to parse the data to byte[] and vice versa, and pass " +
                "them to the constructor.");
    }
    public UnsupportedDataType(Class... classTypes) {
        super("the data type \"" + classTypes[0] +"\" you are working with is not supported by default.\n " +
                "Types supported for this operations are: \n" + paramsToString(1 ,classTypes));
    }

    private static String paramsToString(int start, Class... classTypes){
        assert classTypes.length > start;
        StringBuffer buffer = new StringBuffer();
        for (int i = start; i < classTypes.length; i++) {
            buffer.append("\t");
            buffer.append(classTypes[i]);
            buffer.append("\n");
        }
        return buffer.toString();
    }
}
