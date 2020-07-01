package ai.preferred.cerebro.webservice.tasks;

import ai.preferred.cerebro.index.common.DoubleDotHandler;
import ai.preferred.cerebro.index.hnsw.HnswConfiguration;
import ai.preferred.cerebro.index.hnsw.Item;
import ai.preferred.cerebro.index.hnsw.builder.HnswIndexWriter;
import ai.preferred.cerebro.index.hnsw.searcher.HnswIndexSearcher;
import ai.preferred.cerebro.index.ids.StringID;
import ai.preferred.cerebro.webservice.RecomController;
import ai.preferred.cerebro.webservice.models.Items;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.data.mongodb.repository.MongoRepository;

import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hpminh@apcs.vn
 */
public class BuildHNSWIdxTask extends ValidFolder implements Runnable {
    int embeddingSize;
    RecomController controller;
    MongoRepository<Items, String> respository;

    public BuildHNSWIdxTask(String idxDir,  int embeddingSize, RecomController controller) {
        super(idxDir);
        this.controller = controller;
        this.respository = controller.getItemsRepository();
        this.embeddingSize = embeddingSize;
    }

    @Override
    public void run() {
        try {
            archiveOrMakeFolder();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<Item<double[]>> idxData = new ArrayList<>((int)respository.count());
        for (Items item : respository.findAll()) {
            if(item.vec == null || item.vec.size() < embeddingSize) {
                //System.out.println(item._id);
                continue;
            }
            StringID id = new StringID(item._id);
            double[] vec = ArrayUtils.toPrimitive(item.vec.toArray(new Double[embeddingSize]));
            idxData.add(new Item<>(id , vec));
        }
        DoubleDotHandler handler = new DoubleDotHandler();
        HnswConfiguration configuration = new HnswConfiguration(handler, 500_000);
        configuration.setM(5);
        configuration.setEfConstruction(100);

        HnswIndexWriter<double[]> index = new HnswIndexWriter<>(configuration, idxDir);

        try {
            index.addAll(idxData);
            index.save();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        HnswIndexSearcher<double[]> searcher = new HnswIndexSearcher<>(idxDir);// to implement;
        System.out.println("New Index built successfully");
        controller.switchSearcher(searcher);
    }
}
