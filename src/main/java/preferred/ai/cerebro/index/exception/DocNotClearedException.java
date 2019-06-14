package preferred.ai.cerebro.index.exception;

import preferred.ai.cerebro.index.builder.PersonalizedDocFactory;

/**
 * Thrown when you call {@link preferred.ai.cerebro.index.builder.PersonalizedDocFactory}'s create functions
 * but forget to pair a {@link PersonalizedDocFactory#getDoc()} with a previous create call.
 */
public class DocNotClearedException extends Exception{
    public DocNotClearedException(){
        super("Doc not returned before being cleared for creating another doc");
    }
    public DocNotClearedException(String message){
        super(message);
    }
}
