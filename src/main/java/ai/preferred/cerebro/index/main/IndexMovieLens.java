package ai.preferred.cerebro.index.main;

import ai.preferred.cerebro.index.builder.LSHIndexWriter;
import ai.preferred.cerebro.index.request.LoadSearcherRequest;
import ai.preferred.cerebro.index.search.LSHIndexSearcher;
import ai.preferred.cerebro.index.utils.IndexConst;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class IndexMovieLens {
    public static void main(String[] args) {
    }

    static public void testSearch(){
        MongoClientURI connectionString = new MongoClientURI("mongodb://localhost:27017");
        MongoClient mongoClient = new MongoClient(connectionString);
        MongoDatabase database = mongoClient.getDatabase("movieLens");
        MongoCollection<Document> collection = database.getCollection("items");

        LoadSearcherRequest request = new LoadSearcherRequest("E:\\movieLens_idx", null, false);
        LSHIndexSearcher searcher = null;
        Query query = new TermQuery(new Term("title", "nightmare"));
        try {
            searcher= (LSHIndexSearcher) request.getSearcher();
            TopDocs result = searcher.search(query, 20);
            ScoreDoc[] hits = result.scoreDocs;
            for (ScoreDoc score: hits) {
                String ID = searcher.doc(score.doc).get(IndexConst.IDFieldName);
                ObjectId objectId = new ObjectId(ID);
                Document queryDB = new Document("_id", objectId);
                List results = new ArrayList<>();
                collection.find(queryDB).into(results);
                System.out.println(results);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    /*
    ignore all code that use old API for now
    static public void createIndex(){
        try (LSHIndexWriter writer = new LSHIndexWriter("E:\\movieLens_idx", 8, 50) {
            @Override
            public void indexFile(File file) throws IOException {
                MongoClientURI connectionString = new MongoClientURI("mongodb://localhost:27017");
                MongoClient mongoClient = new MongoClient(connectionString);
                MongoDatabase database = mongoClient.getDatabase("movieLens");
                MongoCollection<Document> collection = database.getCollection("items");

                FindIterable<Document> iterable = collection.find();
                for (Document doc: iterable) {
                    String ID = doc.getObjectId("_id").toString();
                    double[] vec = ArrayUtils.toPrimitive((Double[])((List<Double>)doc.get("vec")).toArray(new Double[50]));

                    TextField title = new TextField("title", (String) doc.get("title"), Field.Store.NO);
                    TextField genre = new TextField("genre", (String) doc.get("genre"), Field.Store.NO);
                    try {
                        docFactory.createPersonalizedDoc(ID, vec);
                        docFactory.addField(title, genre);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    delegate.addDocument(docFactory.getDoc());
                }
            }
        })
        {
            writer.indexFile(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
     */
}
