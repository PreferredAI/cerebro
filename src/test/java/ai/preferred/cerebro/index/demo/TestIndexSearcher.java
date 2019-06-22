package ai.preferred.cerebro.index.demo;

import ai.preferred.cerebro.index.builder.LocalitySensitiveHash;
import ai.preferred.cerebro.index.search.LatentVectorQuery;
import ai.preferred.cerebro.index.search.LuIndexSearcher;
import ai.preferred.cerebro.index.utils.IndexConst;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;

public class TestIndexSearcher extends LuIndexSearcher {
    public TestIndexSearcher(IndexReader r, String splitVecPath) throws IOException {
        super(r, splitVecPath);
    }
    public void setLSH(double[][] hashingVecs){
        this.lsh = new LocalitySensitiveHash(hashingVecs);
    }

    @Override
    protected TopDocs personalizedSearch(double [] vQuery, int topK) throws Exception {
        if(lsh == null)
            throw new Exception("LocalitySensitiveHash not initialized");
        Term t = new Term(IndexConst.HashFieldName, lsh.getHashBit(vQuery));
        // count the number of document that matches with this hashcode
        int count = 0;
        for (LeafReaderContext leaf : reader.leaves())
            count += leaf.reader().docFreq(t);
        if(count == 0){
            return null;
        }
        LatentVectorQuery query = new LatentVectorQuery(vQuery, t);
        //return search(query, topK < count ? topK : count);
        return pSearch(query, count, topK);
    }
}
