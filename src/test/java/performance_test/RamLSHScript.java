package performance_test;

import ai.preferred.cerebro.index.common.FloatCosineHandler;
import ai.preferred.cerebro.index.lsh.RamLSH;
import ai.preferred.cerebro.index.utils.IndexUtils;
import org.apache.lucene.search.TopDocs;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RamLSHScript {

    @Test
    public void testBuildLSH(){
        String idxDir = "E:\\test";//"E:\\netflix_experiment_50d\\items_as_query\\lsh-8bit";
        String dataDir = "E:\\netflix_pmf_50d\\users.o";
        String hashvecDir = "E:\\netflix_pmf_50d\\items_as_query\\lsh-8bit\\splitVec.o";
        int nHashBit = 8 ,numSegments = 6;
        float[][] vecs = null;
        float[][] hashingVecs = null;
        FloatCosineHandler handler = new FloatCosineHandler();
        vecs = handler.load(new File(dataDir))[0];
        hashingVecs = handler.load(new File(hashvecDir))[0];
        try(RamLSH<float[]> writer = new RamLSH<>(idxDir, vecs.length / numSegments + 1,
                hashingVecs, handler)){
            for (int i =0; i < vecs.length; i++) {
                writer.insert(vecs[i]);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void testSearchLSH() throws Exception {
        int k = 20;
        String idxDir = "E:\\netflix_pmf_50d\\items_as_query\\lsh-8bit";
        String queryLabelDir = "E:\\netflix_pmf_50d\\items_as_query\\trueTop20_itemsQuery.o";
        int nCores = 6;
        ExecutorService executorService = Executors.newFixedThreadPool(nCores);
        RamLSH<float[]> index = new RamLSH<>(idxDir, executorService);


        HashMap<float[], int[]> hashmap = IndexUtils.loadHashMap(queryLabelDir, float[].class, int[].class);
        Set<Map.Entry<float[], int[]>> entries = hashmap.entrySet();

        double totalTime = 0;
        double totalHit = 0;
        for (Map.Entry<float[], int[]> entry : entries) {
            float[] query = entry.getKey();
            ArrayList<Integer> setBrute = new ArrayList<>(k);
            for (int t: entry.getValue()) {
                setBrute.add(t);
            }

            long startTime = System.currentTimeMillis();
            TopDocs res = index.search(query, 20);
            long endSearchTime = System.currentTimeMillis();

            System.out.println("Top-20 query time: " +(endSearchTime-startTime)+" ms");
            totalTime += endSearchTime - startTime;
            ArrayList<Integer> returnIDs = new ArrayList<>();
            if(res != null){
                for (int j = 0; j < res.scoreDocs.length; j++) {
                    returnIDs.add(res.scoreDocs[j].doc);
                }
            }
            if(returnIDs.retainAll(setBrute)){
                totalHit += returnIDs.size();
                System.out.println("Overlapp between brute and hash (over top 20) is : " + returnIDs.size());
            }
            System.out.println(" ");
        }
        System.out.println("Average search time :" + totalTime/(entries.size()));
        System.out.println("Average overlap :" + totalHit/(entries.size()));
    }
}
