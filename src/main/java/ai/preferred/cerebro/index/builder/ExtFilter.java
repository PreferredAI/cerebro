package ai.preferred.cerebro.index.builder;

import java.io.File;
import java.io.FileFilter;

/**
 * This class acts as a filter to rule out all the file
 * extension that should not be read for indexing.
 */
public class ExtFilter implements FileFilter {
    final String [] extensions;

    /**
     * @param extensions contain the file extensions to be accepted for reading.
     */
    public ExtFilter(String... extensions) {
        this.extensions = extensions;
    }

    @Override
    public boolean accept(File pathname) {
        String name = pathname.getName().toLowerCase();
        for(String ext : extensions){
            if (name.endsWith(ext))
                return true;
        }
        return false;
    }
}
