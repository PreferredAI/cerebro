package ai.preferred.cerebro.index.search;

import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.similarities.Similarity;

import java.io.IOException;


/**
 * Scorer using {@link ai.preferred.cerebro.index.scoring.CosineSimilarity}
 * to score similarity between latent vectors.
 *
 * @author hpminh@apcs.vn
 */
public class VectorScorer extends Scorer {
    private final PostingsEnum postingsEnum;
    private final Similarity.SimScorer docScorer;

    public VectorScorer(Weight weight, PostingsEnum td, Similarity.SimScorer docScorer) {
        super(weight);
        this.docScorer = docScorer;
        this.postingsEnum = td;
    }
    @Override
    public int docID() {
        return postingsEnum.docID();
    }

    @Override
    public float score() throws IOException {
        assert docID() != DocIdSetIterator.NO_MORE_DOCS;
        return docScorer.score(postingsEnum.docID(), 0);
    }

    @Override
    public DocIdSetIterator iterator() {
        return postingsEnum;
    }
}

