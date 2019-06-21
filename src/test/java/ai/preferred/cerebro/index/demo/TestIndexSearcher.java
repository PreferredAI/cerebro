package ai.preferred.cerebro.index.demo;

import ai.preferred.cerebro.index.builder.LocalitySensitiveHash;
import ai.preferred.cerebro.index.search.LuIndexSearcher;
import org.apache.lucene.index.IndexReader;

import java.io.IOException;

public class TestIndexSearcher extends LuIndexSearcher {
    public TestIndexSearcher(IndexReader r, String splitVecPath) throws IOException {
        super(r, splitVecPath);
    }
    public void setLSH(double[][] hashingVecs){
        this.lsh = new LocalitySensitiveHash(hashingVecs);
    }
}
