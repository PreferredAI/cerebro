package ai.preferred.cerebro.webservice.tasks;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Date;

/**
 * @author hpminh@apcs.vn
 */
public abstract class ValidFolder {
    protected String idxDir;

    public ValidFolder(String idxDir) {
        this.idxDir = idxDir;
    }
    public void archiveOrMakeFolder() throws IOException {
        File folder = new File(idxDir);
        if(folder.exists()){
            if(folder.isDirectory()){
                String newName = folder.getCanonicalPath() + "_" + Date.from(Instant.now()).toString();
                folder.renameTo(new File(newName));
                File dir = new File(idxDir);
                dir.mkdir();
                return;
            }
        }
        folder.mkdir();
    }
}
