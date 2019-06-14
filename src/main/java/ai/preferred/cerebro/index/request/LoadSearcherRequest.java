package ai.preferred.cerebro.index.request;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import ai.preferred.cerebro.index.search.structure.LuIndexSearcher;
import ai.preferred.cerebro.index.search.structure.VersatileSearcher;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * A class to streamline the creation of a {@link VersatileSearcher}.
 */
public class LoadSearcherRequest {
    String indexDir;
    String lshVecDir;
    boolean loadToRAM;

    /**
     *
     * @param indexDir
     * @param lshVecDir
     * @param loadToRAM
     *
     * Note that setting loadToRAM = true is only necessary as a warm-up step
     * to speed up searcher. Usually after running a few queries with an in-RAM
     * searcher we should close and then use a non-RAM searcher on the same index.
     * The new searcher will always outperform the in-RAM one due to caching mechanism
     * while also consumes less memory.
     *
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

    public VersatileSearcher getSearcher() throws IOException {
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
