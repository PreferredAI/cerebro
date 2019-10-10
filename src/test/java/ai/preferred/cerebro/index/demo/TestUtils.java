package ai.preferred.cerebro.index.demo;

//import com.preferred.ai.DumpIndexSearcher;
import ai.preferred.cerebro.index.builder.ExtFilter;
import ai.preferred.cerebro.index.builder.LuIndexWriter;
import ai.preferred.cerebro.index.request.LoadSearcherRequest;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.util.BytesRef;
import ai.preferred.cerebro.index.builder.LocalitySensitiveHash;
import ai.preferred.cerebro.index.search.LuIndexSearcher;
import ai.preferred.cerebro.index.store.QuickSelect;
import ai.preferred.cerebro.index.utils.IndexUtils;
import org.junit.jupiter.api.Test;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static ai.preferred.cerebro.index.utils.IndexUtils.dotProduct;

/**
 * class that house functions to measure the library performance
 * or generating data necessary to do so
 */

public class TestUtils {

    @Test
    void task(){
        double[][] vectors = IndexUtils.randomizeFeatureVectors(6_000_000, 50, false);
        IndexUtils.saveVectors(vectors, TestConst.DIM_50_PATH + "itemVec_6M.o");
    }
    @Test
    void compareAlgorithms(){
        String itemsObjectName = "itemVec_1M.o";
        String existingQuery = "query_top20_1M.o";
        double[][] itemVec = null;
        try {
            itemVec = IndexUtils.readVectors(TestConst.DIM_50_PATH + itemsObjectName);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        double[][] querySet = extractQuerySet(existingQuery);
        QuickSelect<ItemFeatures> itemArr = new QuickSelect<ItemFeatures>(itemVec.length) {
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
                    a.similarity = dotProduct(target.features, a.features) / (target.vecLength * a.vecLength);
                }
            }
        };
        for(int i = 0; i < itemVec.length; i++){
            itemArr.add(new ItemFeatures(i, itemVec[i]));
        }
        for (double[] query: querySet) {
            MinHeap resultID1 = new MinHeap(20, ItemFeatures::new);
            ItemFeatures target = new ItemFeatures(2_000_001, query);

            //algorithm using orderStatistic
            ArrayList<Integer> list = new ArrayList<>(20);
            itemArr.calculateScore(target);
            itemArr.pullTopK(20, false, false);
            for(int j = 1; j <= 20; j++){
                list.add(itemArr.get(itemArr.size() - j).docID);
            }

            //algorithm using Minheap
            for(int j = 0; j < itemArr.size(); j++){
                if (resultID1.top().similarity < itemArr.get(j).similarity) {
                    resultID1.updateTop(itemArr.get(j));
                }
                //itemArr.add(new ItemFeatures(i, itemVec[i]));
            }
            list.retainAll(resultID1.getObjectIds());
            System.out.println(list.size());
        }
    }

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
            queryAndTopK = IndexUtils.readQueryAndTopK(TestConst.DIM_50_PATH + existingQuery);
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

    @Test
    public void refindTop20() throws IOException {
        String itemsObjectName = "itemVec_6M.o";
        String existingQuery = "query_top20_2M.o";
        double[][] itemVec = null;
        try {
            itemVec = IndexUtils.readVectors(TestConst.DIM_50_PATH + itemsObjectName);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        /*
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
                    a.similarity = dotProduct(target.features, a.features) / (target.vecLength * a.vecLength);
                }
            }
        };

         */

        ItemFeatures [] itemList = new ItemFeatures[itemVec.length];
        for(int i = 0; i < itemVec.length; i++){
            itemList[i] = new ItemFeatures(i, itemVec[i]);
            //itemArr.add(new ItemFeatures(i, itemVec[i]));
        }
        double[][] querySet = extractQuerySet(existingQuery);
        HashMap<double[], ArrayList<Integer>> queryAndTopK = new HashMap<>();
        for(double[] query : querySet){
            long startTime = System.currentTimeMillis();
            MinHeap resultID = new MinHeap(20, ItemFeatures::new);
            double len = IndexUtils.vecLength(query);
            for(int j = 0; j < itemVec.length; j++){
                itemList[j].calculateScore(query, len);
                if (resultID.top().similarity < itemList[j].similarity) {
                    resultID.updateTop(itemList[j]);
                }
            }

            queryAndTopK.put(query, resultID.getObjectIds());
            long endTime = System.currentTimeMillis();
            System.out.println("Whole array 6M time: " + (endTime - startTime) + "ms");
        }
        IndexUtils.saveQueryAndTopK(queryAndTopK, TestConst.DIM_50_PATH + "query_top20_6M.o");
    }


    public static void generateQueryAndFindTopK(int nQuery, int k, String itemVecObject){

        double[][] itemVec = null;
        try {
            itemVec = IndexUtils.readVectors(TestConst.DIM_50_PATH + itemVecObject);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        QuickSelect<ItemFeatures> itemArr = new QuickSelect<ItemFeatures>(itemVec.length) {
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
                    a.similarity = dotProduct(target.features, a.features) / (target.vecLength * a.vecLength);
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
        IndexUtils.saveQueryAndTopK(queryAndTopK, TestConst.DIM_50_PATH +"ex.o");
    }


    public void createIndex(){
        double[][] vec = null;
        int optimalLeavesNum = Runtime.getRuntime().availableProcessors();
        try (
                LuIndexWriter writer = new LuIndexWriter(TestConst.DIM_50_PATH + "index_32bits",
                        TestConst.DIM_50_PATH + "splitVec_32bits\\splitVec.o")
                {
                    @Override
                    public void indexFile(File file) throws IOException {}
                })
        {
            vec = IndexUtils.readVectors(TestConst.DIM_50_PATH + "itemVec_1M.o");
            writer.setMaxBufferRAMSize(2048);
            writer.setMaxBufferDocNum((vec.length/optimalLeavesNum) + 1);
            writer.createIndexFromVecData(vec);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   // @Test
    public void compareAccuracyAndSpeed() throws Exception {
        LoadSearcherRequest loadSearcherRequest = new LoadSearcherRequest(TestConst.DIM_50_PATH + "index_16bits",
                TestConst.DIM_50_PATH + "splitVec_16bits\\splitVec.o",
                                                                    false,
                                                                false);
        LuIndexSearcher searcher = (LuIndexSearcher) loadSearcherRequest.getSearcher();
        HashMap<double[], ArrayList<Integer>> queryAndTopK = null;
        try {
            queryAndTopK = IndexUtils.readQueryAndTopK(TestConst.DIM_50_PATH + "query_top20_1M.o");
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
            ScoreDoc[] res = searcher.queryVector(query, 20);
            if(res != null && res.length == 20){
                long endSearchTime = System.currentTimeMillis();
                System.out.println("Top-20 query time: " +(endSearchTime-startTime)+" ms");
                totalTime += endSearchTime - startTime;

                ArrayList<Integer> setHash = new ArrayList<>();
                for(int i = 0; i < res.length; i++){
                    //Document document = searcher.doc(res.scoreDocs[i].doc);
                    int id = res[i].doc; //IndexUtils.byteToInt(document.getField(IndexConst.IDFieldName).binaryValue().bytes);
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

    public static void generateHashVectorEntropyMetric(){
        double[][] itemVec = null;
        try {
            itemVec = IndexUtils.readVectors(TestConst.DIM_50_PATH + "itemVec_20M.o");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        ItemFeatures[] itemArr = new ItemFeatures[itemVec.length];
        for(int i = 0; i < itemVec.length; i++){
            itemArr[i] = new ItemFeatures(i, itemVec[i]);
        }
        HashMap<BytesRef, LinkedList<ItemFeatures>> hashMap = null;
        double[][] splitVec = null;
        LocalitySensitiveHash lsh = null;
        while (true){
            try {
                splitVec = IndexUtils.readVectors(TestConst.DIM_50_PATH + "splitVec_32bits\\splitVec.o");//CerebroUtilities.randomizeFeatureVectors(32, 50, true, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            lsh = new LocalitySensitiveHash(splitVec);
            hashMap = new HashMap<BytesRef, LinkedList<ItemFeatures>>();
            for(int i =0; i < itemVec.length; i++){
                BytesRef hashcode = lsh.getHashBit(itemVec[i]);
                LinkedList t = hashMap.get(hashcode);
                if (t == null){
                    t = new LinkedList();
                }
                t.addFirst(itemArr[i]);
                hashMap.put(hashcode, t);
            }
            double entropy = entropy(hashMap, itemVec.length);
            System.out.println("Unique hashcode occured: " + hashMap.keySet().size());
            System.out.println("Entrophy metric: " + entropy);
            System.out.println(" ");
            break;
//            if(entropy > 12 && entropy < 14){
//                CerebroUtilities.saveVector(splitVec, CerebroConstants.DIM_50_PATH + "splitVec.o");
//                break;
//            }

        }
    }

    public static void test(){
        double[][] itemVec = null;
        double[][] splitVec = null;
        try {
            itemVec = IndexUtils.readVectors(TestConst.DIM_50_PATH + "itemVec.o");
            splitVec = IndexUtils.readVectors(TestConst.DIM_50_PATH + "splitVec_16bits\\splitVec.o");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        ItemFeatures[] itemArr = new ItemFeatures[1000001];
        for(int i = 0; i < 1000000; i++){
            itemArr[i] = new ItemFeatures(i, itemVec[i]);
        }
//        orderStatistic(itemArr, 0, 1000000, 1000000 - 10, itemArr[1000000]);
//        System.out.println("ID of 10 items closest to the target is :");
//        for(int i = 1000000 - 10; i < 1000000; i++){
//            System.out.print(itemArr[i].docID + " ");
//        }

        HashMap<BytesRef, LinkedList<ItemFeatures>> hashMap = new HashMap<BytesRef, LinkedList<ItemFeatures>>();
        LocalitySensitiveHash lsh = new LocalitySensitiveHash(splitVec);
        for(int i =0; i < 1000000; i++){
            BytesRef hashcode = lsh.getHashBit(itemVec[i]);
            LinkedList t = hashMap.get(hashcode);
            if (t == null){
                t = new LinkedList();
            }
            t.addFirst(itemArr[i]);
            hashMap.put(hashcode, t);
        }
        BytesRef hashcode = lsh.getHashBit(itemArr[1000000].features);
        LinkedList<ItemFeatures> bucket = hashMap.get(hashcode);
        QuickSelect<ItemFeatures> arr = new QuickSelect<ItemFeatures>((ItemFeatures [])bucket.toArray()) {
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
                //target.vecLength = vecLength(target.features);
                Iterator<ItemFeatures> it = iterator();
                while (it.hasNext()){
                    ItemFeatures a = it.next();
                    //a.vecLength = vecLength(a.features);
                    a.similarity = dotProduct(target.features, a.features) / (target.vecLength * a.vecLength);
                }
            }
        };

        System.out.println(arr.size());
        arr.calculateScore(itemArr[1000000]);
        arr.pullTopK(10, false, false);
        System.out.println("ID of 10 items closest to the target according to LSH is :");
        for(int i = arr.size() - 10; i < arr.size(); i++){
            System.out.print(arr.get(i) + " ");
        }
    }


}

