package ai.preferred.cerebro.webservice.tasks;

import ai.preferred.cerebro.webservice.RecomController;
import ai.preferred.cerebro.webservice.models.Items;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

/**
 * @author hpminh@apcs.vn
 */
public class BuildTxtIdxTask extends ValidFolder implements Runnable{
    RecomController controller;
    MongoRepository<Items, String> itemRespository;

    public BuildTxtIdxTask(String idxDir, RecomController controller) {
        super(idxDir);
        this.controller = controller;
        this.itemRespository = controller.getItemsRepository();;
    }

    @Override
    public void run(){
        try {
            archiveOrMakeFolder();
        } catch (IOException e) {
            e.printStackTrace();
        }
        IndexWriter writer = null;
        try {
            Directory indexDirectory = FSDirectory.open(Paths.get(idxDir));
            IndexWriterConfig iwc = new IndexWriterConfig(new StandardAnalyzer());
            writer = new IndexWriter(indexDirectory, iwc);
            for(Items item : itemRespository.findAll()){
                Document doc = new Document();
                doc.add(new StringField("ID", item._id, Field.Store.YES));
                doc.add(new TextField("title", item.title, Field.Store.NO));
                writer.addDocument(doc);
            }
            writer.close();
            IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(Paths.get(idxDir))), Executors.newFixedThreadPool(2));
            controller.setTextSearch(searcher);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
