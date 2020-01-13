package draft_index;

import ai.preferred.cerebro.index.common.VecHandler;
import ai.preferred.cerebro.index.lsh.builder.LocalitySensitiveHash;
import ai.preferred.cerebro.index.lsh.searcher.VectorQuery;
import ai.preferred.cerebro.index.utils.IndexUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.ThreadInterruptedException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author hpminh@apcs.vn
 */
public class MemoryLSHSearcher<TVector> {
    public MemoryLSHWriter(String idxDir, ExecutorService executor){
        this.executor = executor;
        VecHandler<TVector> handler = IndexUtils.loadVectorHandler(idxDir + HANDLER);
        TVector[] hashingVec = handler.load(new File(idxDir + HASHVEC))[0];
        this.lsh = new LocalitySensitiveHash<>(handler, hashingVec);
        leaves = new LinkedList<>();
        int numleaves = 0;
        Kryo kryo = new Kryo();
        try (Input input = new Input(new FileInputStream(idxDir + CONFIG))) {
            numleaves =  kryo.readObject(input, int.class);
            maxEachLeaf = kryo.readObject(input, int.class);
        } catch (
                FileNotFoundException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < numleaves; i++) {
            leaves.addLast(new LeafRAMLSH<>(idxDir, i, this.lsh));
        }
    }

    public int count(Term t){
        int num = 0;
        for (LeafRAMLSH<TVector> leaf: leaves) {
            num += leaf.count(t);
        }
        return num;
    }

    public TopDocs search(TVector query, int k){
        VectorQuery<TVector> vecAndHashcode = new VectorQuery<>(query, lsh, null);
        final int limit = count(vecAndHashcode.getTerm());
        final int cappedNumHits = Math.min(k, limit);


        final List<Future<TopDocs>> topDocsFutures = new ArrayList<>(leaves.size());
        for (LeafRAMLSH<TVector> leaf: leaves)
            topDocsFutures.add(executor.submit(new Callable<TopDocs>() {
                @Override
                public TopDocs call() throws Exception {
                    return leaf.search(k, vecAndHashcode);
                }
            }));
        int i =0;
        final TopDocs[] collectedTopdocs = new TopDocs[leaves.size()];
        for (Future<TopDocs> future : topDocsFutures) {
            try {
                collectedTopdocs[i] = future.get();
                i++;
            } catch (InterruptedException e) {
                throw new ThreadInterruptedException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        return TopDocs.merge(0, cappedNumHits, collectedTopdocs, true);
    }
}
