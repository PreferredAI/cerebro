package ai.preferred.cerebro.index.hnsw;

/**
 * Implementation of {@link ProgressListener} that print progress
 * to your Command Line Interface.
 */
public class CLIProgressListener implements ProgressListener {

    /**
     * Singleton instance of {@link CLIProgressListener}.
     */
    public static final CLIProgressListener INSTANCE = new CLIProgressListener();

    private CLIProgressListener() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateProgress(int workDone, int max) {
        System.out.println("Work done: " + workDone + "/" + max);
        // do nothing
    }

}
