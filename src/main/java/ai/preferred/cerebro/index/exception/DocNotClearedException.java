package ai.preferred.cerebro.index.exception;

import ai.preferred.cerebro.index.builder.PersonalizedDocFactory;

/**
 * Thrown when you call {@link ai.preferred.cerebro.index.builder.PersonalizedDocFactory}'s create functions
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
