package ai.preferred.cerebro.index.search.structure;

import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.ScoreDoc;

import ai.preferred.cerebro.core.entity.AbstractVector;
import ai.preferred.cerebro.core.entity.TopKItem;
import ai.preferred.cerebro.core.jpa.entity.IndexMetadata;

import javax.swing.text.Document;
import java.util.List;

/**
 * This interface enforces the functionality of a searcher used by other
 * Cerebro components.
 *
 * @author hpminh@apcs.vn
 */
public interface VersatileSearcher {
    ScoreDoc[] queryKeyWord(QueryParser queryParser, String sQuery, int resultSize) throws Exception;
    ScoreDoc[] queryVector(double[] vQuery, int resultSize) throws Exception;
    //Document doc(int docID);

//    private final IndexMetadata metadata;
//    private final String pathToIndex;
//
//    public AbstractSearcher(IndexMetadata metadata,String pathToIndex){
//        this.metadata = metadata;
//        this.pathToIndex = pathToIndex;
//    }
//
//    public abstract TopKItem[] query(AbstractVector query, int resultSize);
//
//    @Deprecated
//    public IndexMetadata getMetadata() { return metadata; }
//    @Deprecated
//    public void setMetadata(IndexMetadata metadata) { //this.metadata = metadata;
//        }
}
