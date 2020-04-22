package ai.preferred.cerebro.webservice;

import ai.preferred.cerebro.index.common.DoubleDotHandler;
import ai.preferred.cerebro.index.common.FloatDotHandler;
import ai.preferred.cerebro.index.hnsw.HnswConfiguration;
import ai.preferred.cerebro.index.hnsw.Item;
import ai.preferred.cerebro.index.hnsw.builder.HnswIndexWriter;
import ai.preferred.cerebro.index.hnsw.searcher.HnswIndexSearcher;
import ai.preferred.cerebro.index.ids.StringID;
import ai.preferred.cerebro.index.lsh.searcher.Searcher;
import ai.preferred.cerebro.webservice.models.Items;
import com.mongodb.client.MongoCollection;
import org.apache.commons.lang3.ArrayUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author hpminh@apcs.vn
 */
public class BuildHNSWIdxTask implements Runnable {
    String idxDir;
    int embeddingSize;
    RecomController controller;
    MongoRepository<Items, String> respository;

    public BuildHNSWIdxTask(String idxDir,  int embeddingSize, RecomController controller) {
        this.idxDir = idxDir;
        this.controller = controller;
        this.respository = controller.getItemsRepository();
        this.embeddingSize = embeddingSize;
    }

    @Override
    public void run() {
        List<Item<double[]>> idxData = new ArrayList<>((int)respository.count());

        for (Items item : respository.findAll()) {
            if(item.vec == null || item.vec.size() < embeddingSize) {
                System.out.println(item._id);
                continue;
            }
            StringID id = new StringID(item._id);
            double[] vec = ArrayUtils.toPrimitive(item.vec.toArray(new Double[embeddingSize]));
            idxData.add(new Item<>(id , vec));
        }
        DoubleDotHandler handler = new DoubleDotHandler();
        HnswConfiguration configuration = new HnswConfiguration(handler, 500_000);

        HnswIndexWriter<double[]> index = new HnswIndexWriter<>(configuration, idxDir);

        try {
            index.addAll(idxData);
            index.save();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        HnswIndexSearcher<double[]> searcher = new HnswIndexSearcher<>(idxDir);// to implement;
        controller.switchSearcher(searcher);
    }
}
