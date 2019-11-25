package ai.preferred.cerebro.index.extra;

//import com.preferred.ai.DumpIndexSearcher;


import ai.preferred.cerebro.index.common.DoubleCosineHandler;
import ai.preferred.cerebro.index.common.VecDoubleHandler;
import ai.preferred.cerebro.index.hnsw.HnswConfiguration;
import ai.preferred.cerebro.index.hnsw.Item;
import ai.preferred.cerebro.index.hnsw.builder.HnswIndexWriter;
import ai.preferred.cerebro.index.ids.IntID;
import ai.preferred.cerebro.index.lsh.builder.LSHIndexWriter;
import ai.preferred.cerebro.index.lsh.builder.LocalitySensitiveHash;
import ai.preferred.cerebro.index.lsh.searcher.LSHIndexSearcher;
import ai.preferred.cerebro.index.utils.IndexUtils;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.PriorityQueue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * class that house functions to measure the library performance
 * or generating data necessary to do so
 */

public class TestUtils {

    public static double entropy(HashMap<BytesRef, LinkedList<ItemFeatures>> hashMap, int nTotal){
        double res = 0.0;
        Iterator it = hashMap.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry pair = (Map.Entry) it.next();
            LinkedList<ItemFeatures> lklist = (LinkedList<ItemFeatures>) pair.getValue();
            double p = (double)lklist.size() / nTotal;
            res += -p*(Math.log(p) / Math.log(2));
        }
        return res;
    }


    public static double[][] extractQuerySet(String existingQuery){
        HashMap<double[], ArrayList<Integer>> queryAndTopK = null;
        try {
            queryAndTopK = IndexUtils.readDoubleQueryAndTopK(TestConst.DIM_50_PATH + existingQuery);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        double[][] res = new double[queryAndTopK.keySet().size()][];
        return queryAndTopK.keySet().toArray(res);
    }

    public static double[][] extractVecsFromTxt(String dir) throws IOException {
        ExtFilter filter = new ExtFilter("txt");
        File[] files = new File(dir).listFiles();
        double[][] vecs = new double[files.length][];
        for (int i = 0; i < files.length; i++) {
            BufferedReader br = new BufferedReader(new FileReader(files[i]));
            String line = br.readLine();

            line = br.readLine();
            line = line.substring(1, line.length() - 1);
            String [] doubles = line.split(", ");
            vecs[i] = Arrays.stream(doubles)
                    .mapToDouble(Double::parseDouble)
                    .toArray();
        }
        return vecs;
    }

    public void refindTop20() throws IOException {
        String itemsObjectName = "itemVec_1M.o";
        String existingQuery = "query_top20_1M.o";
        DoubleCosineHandler handler = new DoubleCosineHandler();
        double[][] itemVec = handler.load(new File(TestConst.DIM_50_PATH + itemsObjectName))[0];

        Container<ItemFeatures> itemArr = new Container<ItemFeatures>(itemVec.length) {
            @Override
            protected boolean lessThan(ItemFeatures a, ItemFeatures b) {
                if (a.similarity == b.similarity)
                    return a.docID > b.docID;
                else
                    return a.similarity < b.similarity;
            }

            @Override
            public void calculateScore(ItemFeatures target){
                //assert target.features.length == arr[0].features.length;
                Iterator<ItemFeatures> it = iterator();
                while (it.hasNext()){
                    ItemFeatures a = it.next();
                    a.similarity = handler.similarity(a.features, target.features);
                }
            }
        };

        for(int i = 0; i < itemVec.length; i++){
            itemArr.add(new ItemFeatures(i, itemVec[i]));
        }
        double[][] querySet = extractQuerySet(existingQuery);
        HashMap<double[], ArrayList<Integer>> queryAndTopK = new HashMap<>();
        for(int i = 0; i < querySet.length; i++){
            long startTime = System.currentTimeMillis();
            ArrayList<Integer> list = new ArrayList<>(20);
            double[] query = querySet[i];
            ItemFeatures queryItem = new ItemFeatures(20000001, query);
            itemArr.calculateScore(queryItem);
            itemArr.pullTopK(20, false, false);
            for(int j = 1; j <= 20; j++){
                list.add(itemArr.get(itemArr.size() - j).docID);
            }
            queryAndTopK.put(query, list);
            long endTime = System.currentTimeMillis();
            System.out.println("Whole array 20M time: " + (endTime - startTime) + "ms");
        }
        IndexUtils.saveDoubleQueryAndTopK(queryAndTopK, "E:\\data\\imdb_data\\new_query_top20_50k.o");
    }


    public static void generateQueryAndFindTopK(int nQuery, int k, String itemVecObject){
        DoubleCosineHandler handler = new DoubleCosineHandler();
        double[][] itemVec = handler.load(new File(TestConst.DIM_50_PATH + itemVecObject))[0];
        Container<ItemFeatures> itemArr = new Container<ItemFeatures>(itemVec.length) {
            @Override
            protected boolean lessThan(ItemFeatures a, ItemFeatures b) {
                if (a.similarity == b.similarity)
                    return a.docID > b.docID;
                else
                    return a.similarity < b.similarity;
            }

            @Override
            public void calculateScore(ItemFeatures target){
                assert target.features.length == arr[0].features.length;
                Iterator<ItemFeatures> it = iterator();
                while (it.hasNext()){
                    ItemFeatures a = it.next();
                    a.similarity = handler.similarity(a.features, target.features);
                }
            }
        };

        for(int i = 0; i < itemVec.length; i++){
            itemArr.add(new ItemFeatures(i, itemVec[i]));
        }
        HashMap<double[], ArrayList<Integer>> queryAndTopK = new HashMap<>();
        for(int i = 0; i < nQuery; i++){
            ArrayList<Integer> list = new ArrayList<>(k);
            double[] query = IndexUtils.randomizeQueryVector(50);
            ItemFeatures queryItem = new ItemFeatures(itemVec.length + 1, query);
            itemArr.calculateScore(queryItem);
            itemArr.pullTopK(20, false, false);
            for(int j = 1; j <= k; j++){
                list.add(itemArr.get(itemArr.size() - j).docID);
            }
            queryAndTopK.put(query, list);
        }
        IndexUtils.saveDoubleQueryAndTopK(queryAndTopK, TestConst.DIM_50_PATH +"ex.o");
    }


    public void createLSHIndex(){
        double[][] vec = null;
        int optimalLeavesNum = Runtime.getRuntime().availableProcessors();
        DoubleCosineHandler handler = new DoubleCosineHandler();
        double[][] hashingVec = handler.load(new File(TestConst.DIM_50_PATH + "splitVec_32bits\\splitVec.o"))[0];
        try (
                LSHIndexWriter<double[]> writer = new LSHIndexWriter<>(TestConst.DIM_50_PATH + "index_32bits", handler, hashingVec))
        {
            vec = handler.load(new File(TestConst.DIM_50_PATH + "itemVec_1M.o"))[0];
            writer.setMaxBufferRAMSize(2048);
            writer.setMaxBufferDocNum((vec.length/optimalLeavesNum) + 1);
            //write vector to index
            for (int i = 0; i < vec.length; i++) {
                writer.idxPersonalizedDoc(new IntID(i), vec[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void searchLSH() throws Exception {

        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        LSHIndexSearcher<double[]> searcher =  new LSHIndexSearcher<>(TestConst.DIM_50_PATH + "index_16bits", false, executorService);

        HashMap<double[], ArrayList<Integer>> queryAndTopK = null;
        try {
            queryAndTopK = IndexUtils.readDoubleQueryAndTopK(TestConst.DIM_50_PATH + "query_top20_1M.o");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        double totalTime = 0;
        double totalHit = 0;
        int totalMiss = 0;
        Iterator it = queryAndTopK.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry entry = (Map.Entry) it.next();
            double[] query = (double[]) entry.getKey();
            ArrayList<Integer> setBrute = (ArrayList<Integer>) entry.getValue();
            long startTime = System.currentTimeMillis();
            TopDocs res = searcher.personalizedSearch(query, 20);

            if(res != null && res.scoreDocs.length == 20){
                long endSearchTime = System.currentTimeMillis();
                System.out.println("Top-20 query time: " +(endSearchTime-startTime)+" ms");
                totalTime += endSearchTime - startTime;

                ArrayList<Integer> setHash = new ArrayList<>();
                for(int i = 0; i < res.scoreDocs.length; i++){
                    //Document document = searcher.doc(res.scoreDocs[i].doc);
                    int id = res.scoreDocs[i].doc; //IndexUtils.byteToInt(document.getField(IndexConst.IDFieldName).binaryValue().bytes);
                    setHash.add(id);
                }
                if(setHash.retainAll(setBrute)){
                    totalHit += setHash.size();
                    System.out.println("Overlapp between brute and and hash (over top 20) is : " + setHash.size());
                }
                System.out.println(" ");
            }
            else totalMiss++;

        }
        searcher.close();
        System.out.println("Num of misses : " + totalMiss);
        System.out.println("Average search time :" + totalTime/(1000 - totalMiss));
        System.out.println("Average overlap :" + totalHit/(1000 - totalMiss));
    }


    public void createHNSWIndex(){
        double[][] vecs = null;
        String indexDir = TestConst.HNSW_PATH_MULTI + "1M";
        DoubleCosineHandler handler = new DoubleCosineHandler();
        vecs = handler.load(new File(TestConst.DIM_50_PATH + "itemVec_1M.o"))[0];
        List<Item<double[]>> vecList = new ArrayList<>(vecs.length);
        for (int i = 0; i < vecs.length; i++) {
            vecList.add(new Item<>(new IntID(i), vecs[i]));
        }
        HnswConfiguration configuration= new HnswConfiguration(handler);
        configuration.setM(20);
        configuration.setEf(20);
        configuration.setEfConstruction(400);
        HnswIndexWriter<double[]> index = new HnswIndexWriter<>(configuration, indexDir);

        try {
            index.addAll(vecList);
            index.save();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void test(){
        DoubleCosineHandler handler = new DoubleCosineHandler();
        double[][] itemVec = handler.load(new File(TestConst.DIM_50_PATH + "itemVec_1M.o"))[0];
        double[][] splitVec = handler.load(new File(TestConst.DIM_50_PATH + "splitVec_16bits\\splitVec.o"))[0];

        ItemFeatures[] itemArr = new ItemFeatures[itemVec.length];
        for(int i = 0; i < itemVec.length; i++){
            itemArr[i] = new ItemFeatures(i, itemVec[i]);
        }

        HashMap<BytesRef, LinkedList<ItemFeatures>> hashMap = new HashMap<BytesRef, LinkedList<ItemFeatures>>();
        LocalitySensitiveHash<double[]> lsh = new LocalitySensitiveHash<>(handler, splitVec);
        for(int i =0; i < itemVec.length; i++){
            BytesRef hashcode = lsh.getHashBit(itemVec[i]);
            LinkedList<ItemFeatures> t = hashMap.get(hashcode);
            if (t == null){
                t = new LinkedList<>();
                hashMap.put(hashcode, t);
            }
            t.addFirst(itemArr[i]);

        }
        double[] query = itemArr[1000000].features;
        BytesRef hashcode = lsh.getHashBit(query);
        ItemFeatures[] bucket = (ItemFeatures[]) hashMap.get(hashcode).toArray();
        PriorityQueue<ItemFeatures> ranker = new PriorityQueue<ItemFeatures>(10, ItemFeatures::new) {
            @Override
            protected boolean lessThan(ItemFeatures a, ItemFeatures b) {
                return a.similarity < b.similarity;
            }
        };

        for (int i = 0; i < bucket.length; i++) {
            if ((bucket[i].similarity = handler.similarity(bucket[i].features, query)) > ranker.top().similarity)
                ranker.updateTop(bucket[i]);
        }

        System.out.println(bucket.length);
        System.out.println("ID of 10 items closest to the target according to LSH is :");
        Iterator<ItemFeatures> iterator = ranker.iterator();
        while (iterator.hasNext()){
            System.out.print(iterator.next().docID);
        }
    }

}

