package draft_index;

import ai.preferred.cerebro.index.common.VecHandler;
import ai.preferred.cerebro.index.lsh.builder.LocalitySensitiveHash;
import ai.preferred.cerebro.index.lsh.searcher.VectorQuery;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.PriorityQueue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import static ai.preferred.cerebro.index.utils.IndexConst.Sp;

/**
 * @author hpminh@apcs.vn
 */
public class LeafMemoryLSH<TVector> {
    final static String lshtable = Sp + "lshtable.o";
    final static String vecfile  = Sp + "vecs.o";

    int numName;
    int maxCapacity;
    int size;
    ConcurrentHashMap<BytesRef, ArrayList<Integer>> invertidx;
    LocalitySensitiveHash<TVector> lsh;
    TVector[] vecsArray;
    //creation constructor
    public LeafMemoryLSH(int numName, int maxCapacity, LocalitySensitiveHash<TVector> lsh) {
        this.numName = numName;
        this.maxCapacity = maxCapacity;
        this.lsh = lsh;
        size = 0;
        invertidx = new ConcurrentHashMap<>(maxCapacity);
        vecsArray = (TVector[]) new Object[maxCapacity];
    }
    //load constructor
    public LeafMemoryLSH(String idxDir, int numName, LocalitySensitiveHash<TVector> lsh){
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
