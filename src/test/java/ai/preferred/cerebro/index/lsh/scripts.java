package ai.preferred.cerebro.index.lsh;

import ai.preferred.cerebro.index.common.FloatCosineHandler;
import ai.preferred.cerebro.index.extra.ItemFeatures;
import ai.preferred.cerebro.index.utils.IndexUtils;
import org.apache.lucene.util.PriorityQueue;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

public class scripts {

    @Test
    public void generateData(){
        FloatCosineHandler handler = new FloatCosineHandler();
        float[][] data = IndexUtils.randomizeFloatFeatureVectors(1_000_000, 50, false);
        handler.save("E:\\lsh\\1m.o", data);
    }

    @Test
    public void generateQueryLabel(){
        int k = 20;
        FloatCosineHandler handler = new FloatCosineHandler();

        float[][] queries = IndexUtils.randomizeFloatFeatureVectors(1_000, 50, false);

        float[][] data = handler.load(new File("E:\\lsh\\1m.o"))[0];
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
        IndexUtils.saveHashMap("E:\\lsh\\queryLabel_1m.o", queryLabel, float[].class, int[].class);

    }
}
