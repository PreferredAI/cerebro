package preferred.ai.cerebro.index.exception;

import preferred.ai.cerebro.index.utils.IndexConst;

/**
 * Thrown when one of your custom fields has name similar
 * to one of Cerebro's reserved keyword. For more information,
 * see {@link IndexConst}.
 */
public class SameNameException extends Exception{
    public SameNameException(){
        super("Field's name matches with preserved keyword exception");
    }
    public SameNameException(String message){
        super(message);
    }
}
