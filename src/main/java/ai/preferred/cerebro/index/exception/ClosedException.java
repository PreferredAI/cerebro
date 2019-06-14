package ai.preferred.cerebro.index.exception;

/**
 * Thrown when you are trying to use a closed {@link ai.preferred.cerebro.index.builder.LuIndexWriter}.
 */
public class ClosedException extends Exception {
    public ClosedException(){
        super("Object is closed, instantiate a new instance with the same parameter to continue");
    }
}