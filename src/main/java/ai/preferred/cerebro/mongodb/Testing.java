package ai.preferred.cerebro.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.*;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author hpminh@apcs.vn
 */
public class Testing {
    /*
    public static void main(String[] args){
        String host = System.getenv("MONGO_HOST");
        String port = System.getenv("MONGO_PORT");
        String db = "todos";
        MongoClientURI connectionString = new MongoClientURI("mongodb://" + host + ":" + port);
        MongoClient mongoClient = new MongoClient(connectionString);
        MongoDatabase database = mongoClient.getDatabase(db);
        //database.createCollection("rating");
        MongoCollection<Document> collection =database.getCollection("rating") ;
        //insert(collection);
        //find(collection);
        //update(collection);
        find(collection);
    }
     */
    static void insert(MongoCollection<Document> collection){
        Document doc1 = new Document();
        doc1.put("_id", "1");
        Document doc2 = new Document();
        doc2.put("_id", "2");
        ArrayList<Document> listDocument = new ArrayList<>();
        listDocument.add(doc1);
        listDocument.add(doc2);
        collection.insertMany(listDocument);
    }
    static void find(MongoCollection<Document> collection){
        MongoCursor iterator = collection.find().iterator();
        while(iterator.hasNext()){
            Document doc = (Document) iterator.next();
            System.out.println(doc);
        }
    }
    static void update(MongoCollection<Document> collection){
        /*BsonDocument filter = new BsonDocument();
        filter.put("_id", new BsonString("1"));
        MongoCursor iterator = collection.find(filter).iterator();
        while(iterator.hasNext()){
            Document doc = (Document) iterator.next();
            System.out.println(doc);
        }

         */
        BasicDBObject filter = new BasicDBObject();
        filter.put("_id", "1");

        BasicDBObject update = new BasicDBObject("$set", new BasicDBObject("rating", 4));
        //update.put("_id", new BsonString("1"));
        collection.findOneAndUpdate(filter, update);

    }
}
