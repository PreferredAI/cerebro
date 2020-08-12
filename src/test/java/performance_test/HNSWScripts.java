package performance_test;

import ai.preferred.cerebro.index.hnsw.HnswConfiguration;
import ai.preferred.cerebro.index.hnsw.Item;
import ai.preferred.cerebro.index.hnsw.builder.HnswIndexWriter;
import ai.preferred.cerebro.index.hnsw.searcher.HnswIndexSearcher;
import ai.preferred.cerebro.index.ids.IntID;
import ai.preferred.cerebro.index.utils.IndexUtils;
import org.apache.lucene.search.TopDocs;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.*;


public class HNSWScripts {
    static int nconns = 50;
    static int nseg = 1;

    static String netflix_rootPath = "E:\\netflix_pmf_50d\\items_as_query\\hnsw-%dconn-%dseg";
    static String netflix_labelFile = "E:\\netflix_pmf_50d\\items_as_query\\trueTop20_itemsQuery.o";
    static String netflix_dataIdx = "E:\\netflix_pmf_50d\\users.o";


    static String yahoo_rootPath = "E:\\yahoo_pmf_10d\\item_as_query\\hnsw-%dconn-%dseg";
    static String yahoo_labelFile = "E:\\yahoo_pmf_10d\\item_as_query\\trueTop20_itemsQuery.o";
    static String yahoo_dataIdx = "E:\\yahoo_pmf_10d\\users.o";
    @Test
    public void testBuildHNSWIdx(){

        float[][] vecs = null;
        String indexDir = String.format(yahoo_rootPath, nconns, nseg);
        IndexUtils.ensureDirExist(indexDir);
        FloatDotHandler handler = new FloatDotHandler();
        vecs = handler.load(new File(yahoo_dataIdx))[0];

        List<Item<float[]>> vecList = new ArrayList<>(vecs.length);
        for (int i = 0; i < vecs.length; i++) {
            vecList.add(new Item<>(new IntID(i), vecs[i]));
        }
        HnswConfiguration configuration= new HnswConfiguration(handler);
        configuration.setM(nconns);
        configuration.setEf(nconns);
        configuration.setLeaves(nseg);
        if (nseg == 1)
            configuration.setLowMemoryMode(true);
        //configuration.setEfConstruction(500);
        HnswIndexWriter<float[]> index = new HnswIndexWriter<>(configuration, indexDir);

        try {
            index.addAll(vecList);
            index.save();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSearchHNSWIdx(){
        int k = 20;
        String idxDir = String.format(yahoo_rootPath, nconns, nseg);
        String queryLabelDir = yahoo_labelFile;
        HnswIndexSearcher<float[]> index = new HnswIndexSearcher(idxDir);
        FloatDotHandler handler = new FloatDotHandler();

        HashMap<float[], int[]> hashmap = IndexUtils.loadHashMap(queryLabelDir, float[].class, int[].class);
        Set<Map.Entry<float[], int[]>> entries = hashmap.entrySet();

        double totalTime = 0;
        double totalHit = 0;
        for (Map.Entry<float[], int[]> entry: entries) {
            float[] query = entry.getKey();
            int[] truthIds = entry.getValue();

            long startTime = System.currentTimeMillis();
            TopDocs res = index.search(query, 20);
            long endSearchTime = System.currentTimeMillis();
            System.out.println("Top-20 query time: " +(endSearchTime-startTime)+" ms");
            totalTime += endSearchTime - startTime;

            int overlap = 0;
            if(res != null){
                int[] returnedIds = new int[res.scoreDocs.length];
                for (int j = 0; j < res.scoreDocs.length; j++) {
                    IntID realId = (IntID) index.getExternalID(res.scoreDocs[j].doc);
                    returnedIds[j] = realId.getVal();
                }
                overlap = IndexUtils.countIntersection(returnedIds, truthIds);
                totalHit+= overlap;
            }
            System.out.println("Overlapp between brute and index (over top 20) is : " + overlap);
            System.out.println(" ");
        }
        System.out.println("Average search time :" + totalTime/(entries.size()));
        System.out.println("Average overlap :" + totalHit/(entries.size()));
    }
}
