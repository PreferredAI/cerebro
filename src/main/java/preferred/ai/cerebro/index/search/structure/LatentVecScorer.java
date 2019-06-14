package preferred.ai.cerebro.index.search.structure;

import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.similarities.Similarity;

import java.io.IOException;


/**
 * Scorer using {@link preferred.ai.cerebro.index.similarity.CosineSimilarity}
 * to score similarity between latent vectors.
 */
public class LatentVecScorer extends Scorer {
    private final PostingsEnum postingsEnum;
    private final Similarity.SimScorer docScorer;

    LatentVecScorer(Weight weight, PostingsEnum td, Similarity.SimScorer docScorer) {
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

