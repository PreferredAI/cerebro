package ai.preferred.cerebro.index.lsh;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * @author hpminh@apcs.vn
 */
public class DirectoryFactory {
    public static Directory getDirectory(String dir, boolean useRAM){
        FSDirectory fsDirectory = null;
        try {
            fsDirectory = FSDirectory.open(Paths.get(dir));
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (useRAM) {
            try {
                return new RAMDirectory(fsDirectory, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fsDirectory;
    }
}
