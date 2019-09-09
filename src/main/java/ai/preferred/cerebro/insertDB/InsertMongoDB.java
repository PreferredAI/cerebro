package ai.preferred.cerebro.insertDB;

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.bson.Document;

import java.io.IOException;
import java.io.Reader;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Class to read from csv files and insert into mongoDB
 */
public class InsertMongoDB {
    /**
     * Change to read from file application.property later
     */
    private final static String CSV_FILE_PATH = "E:\\demo_data";

    public static void main(String[] args) {

    }

    /**
     * Read and insert into user collection
     */
    public static void insertUsers() {
        try (
                Reader reader = Files.newBufferedReader(Paths.get(CSV_FILE_PATH  + "\\user_table.csv"));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);
                )
        {
            MongoClientURI connectionString = new MongoClientURI("mongodb://localhost:27017");
            MongoClient mongoClient = new MongoClient(connectionString);
            MongoDatabase database = mongoClient.getDatabase("movieLens");
            MongoCollection<Document> collection = database.getCollection("users");


            for (CSVRecord csvRecord : csvParser) {
                // Accessing Values by Column Index
                String id = csvRecord.get(0);
                String [] vecStr = csvRecord.get(1).split(":");
                List<Double> vecs = new ArrayList<>(vecStr.length);
                for (int i = 0; i < vecStr.length; i++) {
                    vecs.add(i, Double.parseDouble(vecStr[i]));
                }
                Document doc = new Document("vec", vecs);
                collection.insertOne(doc);
            }


            Document myDoc = collection.find().first();
            System.out.println(myDoc.toJson());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (MongoException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void insertItems() {
        try (
                Reader reader = Files.newBufferedReader(Paths.get(CSV_FILE_PATH  + "\\item_table.csv"));
                CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withQuote(null));
        )
        {
            MongoClientURI connectionString = new MongoClientURI("mongodb://localhost:27017");
            MongoClient mongoClient = new MongoClient(connectionString);
            MongoDatabase database = mongoClient.getDatabase("movieLens");
            MongoCollection<Document> collection = database.getCollection("items");


            System.out.println();
            for (CSVRecord csvRecord : csvParser) {
                // Accessing Values by Column Index

                String [] vecStr = csvRecord.get(1).split(":");
                List<Double> vecs = new ArrayList<>(vecStr.length);
                for (int i = 0; i < vecStr.length; i++) {
                    vecs.add(i, Double.parseDouble(vecStr[i]));
                }
                String title = csvRecord.get(2);
                String genre = null;
                if(csvRecord.size() == 4)
                    genre = csvRecord.get(3);
                else
                    genre = csvRecord.get(4);
                Document doc = new Document("title", title)
                        .append("genre", genre)
                        .append("vec", vecs);
                collection.insertOne(doc);
            }
            Document myDocm = collection.find().first();
            System.out.println(myDocm.toJson());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (MongoException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }


}
