package ai.preferred.cerebro.index.lsh;

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

public class RamLSH<TVector> implements Closeable {
    final static String CONFIG = "config.o";
    final static String HASHVEC = "hashingVector.o";
    final static String HANDLER = "vectorHandler.o";
    String idxDir;
    LocalitySensitiveHash<TVector> lsh;
    LinkedList<LeafRAMLSH> leaves;
    LeafRAMLSH currentBuffer;
    ExecutorService executor;
    int maxEachLeaf;
    int numItems;
    //creation constructor
    public RamLSH(String idxDir, int maxEachLeaf, TVector[] hashvecs, VecHandler<TVector> handler) {
        this.idxDir = idxDir;
        this.maxEachLeaf = maxEachLeaf;
        lsh = new LocalitySensitiveHash<>(handler, hashvecs);
        handler.save(idxDir + HASHVEC, hashvecs);
        IndexUtils.saveVectorHandler(idxDir + HANDLER, handler);
        leaves = new LinkedList<>();
        leaves.addLast(new LeafRAMLSH<>(leaves.size(), maxEachLeaf, this.lsh));
        numItems = 0;
        currentBuffer = leaves.getLast();

    }
    //load constructor
    public RamLSH(String idxDir, ExecutorService executor){
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

    public void insert(TVector vector){
        if(currentBuffer.size() == maxEachLeaf){
            currentBuffer.save(idxDir);
            currentBuffer = new LeafRAMLSH<>(leaves.size(), maxEachLeaf, this.lsh);
            leaves.addLast(currentBuffer);
        }
        currentBuffer.insert(numItems++, vector);
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



    @Override
    public void close() throws IOException {
        currentBuffer.save(idxDir);
        int nleaves = leaves.size();
        Kryo kryo = new Kryo();
        try (Output output = new Output(new FileOutputStream(idxDir + CONFIG))){
            kryo.writeObject(output, nleaves);
            kryo.writeObject(output, maxEachLeaf);
        } catch (
                FileNotFoundException e) {
            e.printStackTrace();
        }
        leaves.clear();
    }

    static class LeafRAMLSH<TVector>{
        final static String lshtable = Sp + "lshtable.o";
        final static String vecfile  = Sp + "vecs.o";

        int numName;
        int maxCapacity;
        int size;
        ConcurrentHashMap<BytesRef, ArrayList<Integer>> invertidx;
        LocalitySensitiveHash<TVector> lsh;
        TVector[] vecsArray;
        //creation constructor
        public LeafRAMLSH(int numName, int maxCapacity, LocalitySensitiveHash<TVector> lsh) {
            this.numName = numName;
            this.maxCapacity = maxCapacity;
            this.lsh = lsh;
            size = 0;
            invertidx = new ConcurrentHashMap<>(maxCapacity);
            vecsArray = (TVector[]) new Object[maxCapacity];
        }
        //load constructor
        public LeafRAMLSH(String idxDir, int numName, LocalitySensitiveHash<TVector> lsh){
            this.numName = numName;
            Kryo kryo = new Kryo();
            kryo.register(byte[].class);
            kryo.register(BytesRef.class);
            kryo.register(ArrayList.class);
            kryo.register(ConcurrentHashMap.class);
            try (Input input = new Input(new FileInputStream(idxDir + numName + lshtable))) {
                invertidx =  kryo.readObject(input, ConcurrentHashMap.class);
                size = kryo.readObject(input, int.class);
            } catch (
                    FileNotFoundException e) {
                e.printStackTrace();
            }
            vecsArray = lsh.getHandler().load(new File(idxDir + numName + vecfile))[0];
            maxCapacity = vecsArray.length;
        }

        public void save(String dir){
            Kryo kryo = new Kryo();
            kryo.register(byte[].class);
            kryo.register(BytesRef.class);
            kryo.register(ArrayList.class);
            kryo.register(ConcurrentHashMap.class);

            try (Output output = new Output(new FileOutputStream(dir + numName + lshtable))){
                kryo.writeObject(output, invertidx);
                kryo.writeObject(output, size);
            } catch (
                    FileNotFoundException e) {
                e.printStackTrace();
            }
            lsh.getHandler().save(dir + numName + vecfile, vecsArray);
        }


        public int size (){
            return size;
        }

        public int count(Term t){
            BytesRef hashcode = t.bytes();
            ArrayList<Integer> ids = invertidx.get(hashcode);
            if (ids == null)
                return 0;
            return ids.size();
        }

        public void insert(int globalID, TVector vector){
            assert size() < maxCapacity;
            assert size == globalID - (numName * maxCapacity);
            vecsArray[size] = vector;
            BytesRef hashcode = lsh.getHashBit(vector);
            invertidx.putIfAbsent(hashcode, new ArrayList());
            ArrayList<Integer> idlist = invertidx.get(hashcode);
            idlist.add(size);
            ++size;
        }
        public TopDocs search(int k, VectorQuery<TVector> query){
            BytesRef hashcode = query.getTerm().bytes();
            TVector queryVector = query.getVec();
            ArrayList<Integer> ids = invertidx.get(hashcode);


            if (ids == null)
                return null;
            k = Math.min(k, ids.size());
            int baseID = numName * maxCapacity;
            VecHandler handler = lsh.getHandler();

            //min heap
            PriorityQueue<ScoreDoc> ranker = new PriorityQueue<ScoreDoc>(k, () -> new ScoreDoc(-1, Float.MIN_VALUE)) {
                @Override
                protected boolean lessThan(ScoreDoc a, ScoreDoc b) {
                    return a.score < b.score;
                }
            };

            //actual ranking
            for (Integer i : ids) {
                ScoreDoc e = new ScoreDoc(baseID + i, (float) handler.similarity(vecsArray[i], queryVector));
                if(ranker.top().score < e.score){
                    ranker.updateTop(e);
                }
            }

            //pop out in ascending order
            ScoreDoc[] docs = new ScoreDoc[k];
            int i = k - 1;
            while(ranker.size() != 0){
                docs[i--] = ranker.pop();
            }
            return new TopDocs(k, docs, docs[0].score);
        }
    }
}