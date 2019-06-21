package ai.preferred.cerebro.index.exception;

import ai.preferred.cerebro.index.utils.IndexConst;

/**
 *
 * Thrown when one of your custom fields has name similar
 * to one of Cerebro's reserved keyword. For more information,
 * see {@link IndexConst}.
 *
 * @author hpminh@apcs.vn
 */
public class SameNameException extends Exception{
    public SameNameException(){
        super("Field's name matches with preserved keyword");
    }
//    public SameNameException(String message){
//        super(message);
//    }
}
