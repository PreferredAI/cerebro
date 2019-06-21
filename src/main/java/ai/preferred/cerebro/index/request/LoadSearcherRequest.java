package ai.preferred.cerebro.index.request;

import ai.preferred.cerebro.index.search.Searcher;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import ai.preferred.cerebro.index.search.LuIndexSearcher;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * A class to streamline the creation of a {@link Searcher}.
 *
 * @author hpminh@apcs.vn
 */
public class LoadSearcherRequest {
    String indexDir;
    String lshVecDir;
    boolean loadToRAM;

    /**
     * Create a request to load searcher with RAM usage configuration.
     *
     * @param indexDir directory to the folder containing index
     * @param lshVecDir directory to the file object containing the
     *                  hashing vectors
     * @param loadToRAM whether or not to load entire index to RAM.
     * <p>
     * Note that setting loadToRAM = true is only necessary as a warm-up step
     * to speed up searcher. Usually after running a few queries with an in-RAM
     * searcher we should close and then use a non-RAM searcher on the same index.
     * The new searcher will always outperform the in-RAM one due to caching mechanism
     * while also consumes less memory.
     * <p>
     * You can also use a non-RAM searcher from the start and let the operation system
     * eventually caches your index file but this usually takes a longer time than using
     * a in-RAM searcher first. Plus doing so means that the first dozens (or hundreds)
     * queries will be very slow.
     */
    public LoadSearcherRequest(String indexDir, String lshVecDir, boolean loadToRAM) {
        this.indexDir = indexDir;
        this.lshVecDir = lshVecDir;
        this.loadToRAM = loadToRAM;
    }

    /**
     * Load index on hard disk and create a {@link ai.preferred.cerebro.index.search.Searcher} object.
     * @return Searcher on the index in the provided directory
     * @throws IOException thrown if the index is corrupted or can not be read.
     */
    public Searcher<ScoreDoc> getSearcher() throws IOException {
        Directory indexDirectory;
        if(loadToRAM){
            indexDirectory = new RAMDirectory(FSDirectory.open(Paths.get(indexDir)), null);
            return new LuIndexSearcher(DirectoryReader.open(indexDirectory), lshVecDir);
        }
        else {
            indexDirectory = FSDirectory.open(Paths.get(indexDir));
            return new LuIndexSearcher(DirectoryReader.open(indexDirectory), lshVecDir);
        }

    }
}
