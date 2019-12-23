package performance_test;

import ai.preferred.cerebro.index.extra.ItemFeatures;
import ai.preferred.cerebro.index.utils.IndexUtils;
import org.apache.lucene.util.PriorityQueue;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

public class TruthIds {
    static String fileBeIdx = "E:\\yahoo_pmf_10d\\users.o";
    static String fileBeQuery = "E:\\yahoo_pmf_10d\\items.o";
    static String fileQueryAndTruth = "E:\\yahoo_pmf_10d\\item_as_query\\trueTop%s_itemsQuery.o";
    static int k = 20;
    @Test
    public void generateQueryLabel(){
        FloatDotHandler handler = new FloatDotHandler();

        float[][] queries = handler.load(new File(fileBeQuery))[0];

        float[][] data = handler.load(new File(fileBeIdx))[0];
        ItemFeatures<float[]>[] itemArr = new ItemFeatures[data.length];
        for (int i = 0; i < data.length; i++) {
            itemArr[i] = new ItemFeatures<>(i, data[i]);
        }

        HashMap<float[], int[]> queryLabel = new HashMap<>(queries.length);

        for (float[] query: queries) {
            PriorityQueue<ItemFeatures<float[]>> ranker = new PriorityQueue<ItemFeatures<float[]>>(k, ItemFeatures::new) {
                @Override
                protected boolean lessThan(ItemFeatures<float[]> a, ItemFeatures<float[]> b) {
                    return a.similarity < b.similarity;
                }
            };
            for (ItemFeatures<float[]> item: itemArr) {
                item.similarity = handler.similarity(item.features, query);
                if(ranker.top().similarity < item.similarity)
                    ranker.updateTop(item);
            }
            int i = 0;
            int[] ids = new int[k];
            Iterator<ItemFeatures<float[]>> iterator = ranker.iterator();
            while(iterator.hasNext()){
                ids[i++] = iterator.next().docID;
            }
            queryLabel.put(query, ids);
        }
        IndexUtils.saveHashMap(String.format(fileQueryAndTruth, k), queryLabel, float[].class, int[].class);

    }
}
