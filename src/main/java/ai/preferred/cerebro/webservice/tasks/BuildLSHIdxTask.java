package ai.preferred.cerebro.webservice.tasks;

import ai.preferred.cerebro.index.common.DoubleDotHandler;
import ai.preferred.cerebro.index.ids.StringID;
import ai.preferred.cerebro.index.lsh.builder.LSHIndexWriter;
import ai.preferred.cerebro.index.lsh.searcher.LSHIndexSearcher;
import ai.preferred.cerebro.index.utils.IndexUtils;
import ai.preferred.cerebro.webservice.RecomController;
import ai.preferred.cerebro.webservice.models.Items;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;

/**
 * @author hpminh@apcs.vn
 */
public class BuildLSHIdxTask extends ValidFolder implements Runnable {
    int embeddingSize;
    RecomController controller;
    MongoRepository<Items, String> respository;

    public BuildLSHIdxTask(String idxDir,  int embeddingSize, RecomController controller) {
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
        int optimal_leaves = 4;
        double[][] splitvec = IndexUtils.randomizeDoubleFeatureVectors(8, 50, true);
        DoubleDotHandler handler = new DoubleDotHandler();
        LSHIndexSearcher<double[]> searcher = null;
        try (LSHIndexWriter<double[]> writer = new LSHIndexWriter<>(idxDir, handler, splitvec)) {
            writer.setMaxBufferRAMSize(512);
            writer.setMaxBufferDocNum(26744 / optimal_leaves + 1);
            for(Items item : respository.findAll()){
                double[] vec = ArrayUtils.toPrimitive(item.getVec().toArray(new Double[50]));
                writer.idxPersonalizedDoc(new StringID(item.get_id()), vec);
            }
            searcher = new LSHIndexSearcher<>(idxDir, false, Executors.newFixedThreadPool(4));
            controller.switchSearcher(searcher);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}