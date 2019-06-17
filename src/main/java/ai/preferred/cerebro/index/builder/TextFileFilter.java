package ai.preferred.cerebro.index.builder;

import java.io.File;
import java.io.FileFilter;

/**
 * A Filter class that only accept .txt files.
 * Example on how to apply {@link FileFilter}
 * to read only files that you want.
 *
 * @author hpminh@apcs.vn
 */
public class TextFileFilter implements FileFilter {
    @Override
    public boolean accept(File pathname){
        return pathname.getName()
                        .toLowerCase()
                        .endsWith(".txt");
    }
}
