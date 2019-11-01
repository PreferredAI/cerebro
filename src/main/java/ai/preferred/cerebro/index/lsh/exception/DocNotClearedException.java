package ai.preferred.cerebro.index.lsh.exception;


/**
 *
 * Thrown when you call {@link ai.preferred.cerebro.index.lsh.builder.PersonalizedDocFactory}'s createPersonalizedDoc functions
 * but forget to pair a {@link ai.preferred.cerebro.index.lsh.builder.PersonalizedDocFactory#getDoc()} with a previous createPersonalizedDoc call.
 *
 * @author hpminh@apcs.vn
 */
public class DocNotClearedException extends Exception{
    public DocNotClearedException(){
        super("Doc not returned before being cleared for creating another doc");
    }
}
