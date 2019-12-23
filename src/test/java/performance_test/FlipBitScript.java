package performance_test;

import ai.preferred.cerebro.index.ids.IntID;
import ai.preferred.cerebro.index.lsh.builder.LSHIndexWriter;
import ai.preferred.cerebro.index.lsh.searcher.FlipBitSearcher;
import ai.preferred.cerebro.index.utils.IndexUtils;
import org.apache.lucene.search.TopDocs;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FlipBitScript {
    static String netflix_rootPath = "E:\\netflix_pmf_50d\\items_as_query\\lsh-%dbit";
    static String netflix_labelFile = "E:\\netflix_pmf_50d\\items_as_query\\trueTop20_itemsQuery.o";
    static String netflix_dataIdx = "E:\\netflix_pmf_50d\\users.o";

    static String yahoo_rootPath = "E:\\yahoo_pmf_10d\\item_as_query\\lsh-%dbit";
    static String yahoo_labelFile = "E:\\yahoo_pmf_10d\\item_as_query\\trueTop20_itemsQuery.o";
    static String yahoo_dataIdx = "E:\\yahoo_pmf_10d\\users.o";

    static int nHashBit = 64;
    static int nSegment = 6;


    @Test
    public void testBuildLSH(){
        String idxDir = String.format(yahoo_rootPath, nHashBit);
        IndexUtils.ensureDirExist(idxDir);
        float[][] vecs = null;

        FloatDotHandler handler = new FloatDotHandler();
        vecs = handler.load(new File(yahoo_dataIdx))[0];

        try(LSHIndexWriter<float[]> writer = new LSHIndexWriter<>(idxDir, handler,
                IndexUtils.randomizeFloatFeatureVectors(nHashBit, 50, true))){
            writer.setMaxBufferDocNum(vecs.length / nSegment + 1);
            writer.setMaxBufferRAMSize(1024);
            for (int i =0; i < vecs.length; i++) {
                writer.idxPersonalizedDoc(new IntID(i), vecs[i]);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void testSearchFlipBit() throws Exception {
        int k = 20;
        String idxDir = String.format(yahoo_rootPath, nHashBit);
        ExecutorService executorService = Executors.newFixedThreadPool(nSegment);
        FlipBitSearcher<float[]> index = new FlipBitSearcher<>(idxDir, false, executorService);


        HashMap<float[], int[]> hashmap = IndexUtils.loadHashMap(yahoo_labelFile, float[].class, int[].class);
        Set<Map.Entry<float[], int[]>> entries = hashmap.entrySet();

        double totalTime = 0;
        double totalHit = 0;
        for (Map.Entry<float[], int[]> entry : entries) {
            float[] query = entry.getKey();
            int[] truthIds = entry.getValue();

            long startTime = System.currentTimeMillis();
            TopDocs res = index.personalizedSearch(query, k);
            long endSearchTime = System.currentTimeMillis();
            System.out.println("Top-20 query time: " +(endSearchTime-startTime)+" ms");
            totalTime += endSearchTime - startTime;

            int overlap = 0;
            if(res != null){
                int[] returnedIds = new int[res.scoreDocs.length];
                for (int j = 0; j < res.scoreDocs.length; j++) {
                    returnedIds[j] = res.scoreDocs[j].doc;
                }
                overlap = IndexUtils.countIntersection(returnedIds, truthIds);
                totalHit+= overlap;
            }
            System.out.println("Overlapp between brute and hash (over top 20) is : " + overlap);
            System.out.println(" ");
        }
        System.out.println("Average search time :" + totalTime/(entries.size()));
        System.out.println("Average overlap :" + totalHit/(entries.size()));
    }
}
