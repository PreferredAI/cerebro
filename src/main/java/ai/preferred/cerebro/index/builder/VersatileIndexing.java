package ai.preferred.cerebro.index.builder;

/**
 *
 * An interface to enforce the functionality of Cerebro's IndexWriter.
 *
 * @author hpminh@apcs.vn
 */
public interface VersatileIndexing {
    void indexLatentVectors(Object... params) throws Exception;
    void indexKeyWords(Object... params) throws Exception;
}
