package performance_test;

import ai.preferred.cerebro.index.bruteforce.BruteIndex;
import ai.preferred.cerebro.index.extra.ItemFeatures;
import ai.preferred.cerebro.index.hnsw.Item;
import ai.preferred.cerebro.index.ids.ExternalID;
import ai.preferred.cerebro.index.ids.IntID;
import ai.preferred.cerebro.index.utils.IndexUtils;
import org.apache.lucene.util.PriorityQueue;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class TruthIds {
    static String fileBeIdx = "E:\\yahoo_pmf_10d\\users.o";
    static String fileBeQuery = "E:\\yahoo_pmf_10d\\items.o";
    static String fileQueryAndTruth = "E:\\yahoo_pmf_10d\\item_as_query\\query_and_truth_top%s.o";
    static int k = 20;
    @Test
    public void generateQueryLabel(){
        FloatDotHandler handler = new FloatDotHandler();

        float[][] queries = handler.load(new File(fileBeQuery))[0];

        float[][] data = handler.load(new File(fileBeIdx))[0];
        Item<float[]>[] itemArr = new Item[data.length];
        for (int i = 0; i < data.length; i++) {
            itemArr[i] = new Item<>(new IntID(i), data[i]);
        }
        BruteIndex searcher = new BruteIndex(6, itemArr, handler);
        HashMap<float[], int[]> queryLabel = new HashMap<>(queries.length);

        for (float[] query: queries) {
            List<ExternalID> exIds = searcher.search(query, k);
            int i = 0;
            int[] ids = new int[k];
            for (ExternalID exid : exIds) {
                ids[i++] = ((IntID)exid).getVal();
            }
            queryLabel.put(query, ids);
        }
        IndexUtils.saveHashMap(String.format(fileQueryAndTruth, k), queryLabel, float[].class, int[].class);

    }
}
