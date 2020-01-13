package draft_index;

import ai.preferred.cerebro.index.common.VecHandler;
import ai.preferred.cerebro.index.lsh.builder.LocalitySensitiveHash;
import ai.preferred.cerebro.index.lsh.searcher.VectorQuery;
import ai.preferred.cerebro.index.utils.IndexUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.PriorityQueue;
import org.apache.lucene.util.ThreadInterruptedException;

import javax.persistence.Index;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import static ai.preferred.cerebro.index.utils.IndexConst.Sp;

public class MemoryLSHWriter<TVector> implements Closeable {
    final static String CONFIG = "config.o";
    final static String HASHVEC = "hashingVector.o";
    final static String HANDLER = "vectorHandler.o";
    String idxDir;
    LocalitySensitiveHash<TVector> lsh;
    LeafMemoryLSH currentBuffer;
    ExecutorService executor;
    int maxEachLeaf; //maximum capacity of each leaf
    int nleaves;
    int nItems;
    //creation constructor
    public MemoryLSHWriter(String idxDir, int maxEachLeaf, TVector[] hashvecs, VecHandler<TVector> handler) {
        this.idxDir = idxDir;
        this.maxEachLeaf = maxEachLeaf;
        lsh = new LocalitySensitiveHash<>(handler, hashvecs);
        handler.save(idxDir + HASHVEC, hashvecs);
        IndexUtils.saveVectorHandler(idxDir + HANDLER, handler);
        nleaves = 0;
        currentBuffer = new LeafMemoryLSH<>(nleaves, maxEachLeaf, this.lsh);
        nItems = 0;
    }

    public void insert(TVector vector){
        if(currentBuffer.size() == maxEachLeaf){
            currentBuffer.save(idxDir);
            currentBuffer = new LeafMemoryLSH<>(++nleaves, maxEachLeaf, this.lsh);
        }
        currentBuffer.insert(nItems++, vector);
    }


    @Override
    public void close() throws IOException {
        currentBuffer.save(idxDir);
        Kryo kryo = new Kryo();
        try (Output output = new Output(new FileOutputStream(idxDir + CONFIG))){
            kryo.writeObject(output, nleaves);
            kryo.writeObject(output, maxEachLeaf);
        } catch (
                FileNotFoundException e) {
            e.printStackTrace();
        }
        currentBuffer = null;
    }

}
