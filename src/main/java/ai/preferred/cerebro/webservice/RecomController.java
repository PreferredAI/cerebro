package ai.preferred.cerebro.webservice;

import ai.preferred.cerebro.index.hnsw.searcher.HnswIndexSearcher;
import ai.preferred.cerebro.index.ids.StringID;
import ai.preferred.cerebro.index.utils.IndexConst;
import ai.preferred.cerebro.index.utils.IndexUtils;
import ai.preferred.cerebro.webservice.models.Items;
import ai.preferred.cerebro.webservice.repositories.ItemsRepository;
import ai.preferred.cerebro.webservice.repositories.UsersRepository;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author hpminh@apcs.vn
 */

@RestController
@RequestMapping("/recom")
public class RecomController {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private ItemsRepository itemsRepository;

    HnswIndexSearcher<float[]> searcher;
    IndexSearcher textSearch;
    private final Object dummyLock;
    int embeddingSize;
    int topK;
    RecomController() {
        String idxDir = System.getenv("IDX");
        dummyLock = new Object();
        HnswIndexSearcher<float[]> index = new HnswIndexSearcher(idxDir);
        embeddingSize = 50;
        topK = 20;

        ControllerHook.getInstance().putRecomController(this);

    }

    public void switchSearcher(HnswIndexSearcher<float[]> searcher){
        synchronized (dummyLock){
            this.searcher = searcher;
        }

    }

    public ItemsRepository getItemsRepository() {
        return itemsRepository;
    }

    @CrossOrigin
    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String helloWorld(){
        return "Hello World";
    }



    @CrossOrigin
    @RequestMapping(value = "/get/{id}", method = RequestMethod.GET)
    public ResultAndTime getRecommendations(@PathVariable("id")ObjectId id) throws IOException {

        float[] vectorQuery = ArrayUtils.toPrimitive((Float[])((List<Float>)
                usersRepository.findBy_id(id).getVec()).toArray(new Float[embeddingSize]));

        long first = System.nanoTime();
        ArrayList<ObjectId> ids = new ArrayList<>(topK);
        synchronized(dummyLock){
            TopDocs res = searcher.search(vectorQuery, topK);

            if(res != null){

                for (int j = 0; j < res.scoreDocs.length; j++) {
                    StringID realId = (StringID) searcher.getExternalID(res.scoreDocs[j].doc);
                    ids.add(new ObjectId(realId.getVal()));
                }
            }
        }
        long second = System.nanoTime();

        ArrayList<Items> result = (ArrayList<Items>) itemsRepository.findAllById(ids);
        //long third = System.nanoTime();
        //System.out.println("Lucene time: "+ (second - first));
        //System.out.println("DB time: "+ (third - second));
        return new ResultAndTime(result, (second - first)/1000_000);
    }



    @CrossOrigin
    @RequestMapping(value = "/relatedItems/{id}", method = RequestMethod.GET)
    public ResultRelated relatedItems(@PathVariable("id") ObjectId id) throws IOException{
        Items qItem = itemsRepository.findBy_id(id);
        float[] vectorQuery = ArrayUtils.toPrimitive(qItem.vec.toArray(new Float[embeddingSize]));
        long first = System.nanoTime();
        ArrayList<ObjectId> ids = new ArrayList<>(topK + 1);

        TopDocs res = searcher.search(vectorQuery, topK + 1);
        if(res != null){
            for(int i = 0; i < res.scoreDocs.length; i++){
                StringID dbId = (StringID) searcher.getExternalID(res.scoreDocs[i].doc);
                if(dbId.getVal().equals(id.toString()))
                    continue;
                ids.add(new ObjectId(dbId.getVal()));
            }
        }
        long second = System.nanoTime();
        ArrayList<Items> results = (ArrayList<Items>) itemsRepository.findAllById(ids);
        return new ResultRelated(qItem, results, (second - first) / 1_000_000);
    }

}
