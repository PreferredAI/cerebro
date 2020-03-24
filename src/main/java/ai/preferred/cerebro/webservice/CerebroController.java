package ai.preferred.cerebro.webservice;

import ai.preferred.cerebro.index.hnsw.builder.HnswIndexWriter;
import ai.preferred.cerebro.index.hnsw.searcher.HnswIndexSearcher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author hpminh@apcs.vn
 */
@RestController
@RequestMapping("/cerebro")
public class CerebroController {
    public HnswIndexWriter<float[]> indexWriter;
    public HnswIndexSearcher<float[]> indexSearcher;
    CerebroController(){
        String idxDir = System.getenv("IDX");

    }

}
