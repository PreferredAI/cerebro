package ai.preferred.cerebro.index.request;

import ai.preferred.cerebro.index.search.LSHIndexSearcher;
import ai.preferred.cerebro.index.search.Searcher;
import ai.preferred.cerebro.index.utils.VecDoubleHandler;
import ai.preferred.cerebro.index.utils.VecFloatHandler;
import ai.preferred.cerebro.index.utils.VecHandler;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A class to streamline the creation of a {@link Searcher}.
 *
 * @author hpminh@apcs.vn
 */
public class LoadSearcherRequest<TVector> {
    private String indexDir;
    private String lshVecDir;
    private boolean loadToRAM;
    private boolean multithreadEnabled;
    private VecHandler<TVector> handler;

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
        this(indexDir, lshVecDir, loadToRAM, false);
    }
    public LoadSearcherRequest(String indexDir, String lshVecDir, boolean loadToRAM, boolean multithreadEnabled) {
        this(indexDir, lshVecDir, loadToRAM, multithreadEnabled, null);
    }

    public LoadSearcherRequest(String indexDir, String lshVecDir, boolean loadToRAM, boolean multithreadEnabled, VecHandler<TVector> handler) {
        this.indexDir = indexDir;
        if(lshVecDir == null){
            File f = new File(indexDir + "\\splitVec.o");
            if(f.exists() && !f.isDirectory())
                lshVecDir = indexDir + "\\splitVec.o";
        }
        this.lshVecDir = lshVecDir;
        this.loadToRAM = loadToRAM;
        this.multithreadEnabled = multithreadEnabled;
        if (handler == null){
            Exception hold = null;
            try{
                this.handler = (VecHandler<TVector>) new VecFloatHandler();
            }catch (Exception e){
                hold = e;
            }
            if(hold != null){
                try{
                    this.handler = (VecHandler<TVector>) new VecDoubleHandler();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        else
            this.handler = handler;
    }
    /**
     * Load index on hard disk and create a {@link ai.preferred.cerebro.index.search.LSHIndexSearcher} object.
     * @return Searcher on the index in the provided directory
     * @throws IOException thrown if the index is corrupted or can not be read.
     */
    public Searcher<TVector> getSearcher() throws IOException {
        ExecutorService executorService = null;
        if(multithreadEnabled){
            executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
        }
        Directory indexDirectory;
        if(loadToRAM){
            indexDirectory = new RAMDirectory(FSDirectory.open(Paths.get(indexDir)), null);
            return new LSHIndexSearcher<>(DirectoryReader.open(indexDirectory), executorService, lshVecDir, handler);
        }
        else {
            indexDirectory = FSDirectory.open(Paths.get(indexDir));
            return new LSHIndexSearcher<>(DirectoryReader.open(indexDirectory), executorService, lshVecDir, handler);
        }

    }
}
