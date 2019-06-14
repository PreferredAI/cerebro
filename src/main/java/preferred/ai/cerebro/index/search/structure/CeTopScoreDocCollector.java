package preferred.ai.cerebro.index.search.structure;


import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.*;
import preferred.ai.cerebro.index.store.DocArray;
import preferred.ai.cerebro.index.utils.IndexUtils;


import java.io.IOException;

/**
 * Class to handle Cerebro internal retrieving and ranking
 * of Document Objects.
 */
public class CeTopScoreDocCollector extends CeCollector<ScoreDoc> {

    abstract static class ScorerLeafCollector implements LeafCollector {

        Scorer scorer;

        @Override
        public void setScorer(Scorer scorer) throws IOException {
            this.scorer = scorer;
        }

    }

    @Override
    public LeafCollector getLeafCollector(LeafReaderContext context) throws IOException {
        final int docBase = context.docBase;
        return new ScorerLeafCollector() {

            @Override
            public void collect(int doc) throws IOException {
                float score = scorer.score();
                // This collector cannot handle these scores:
                assert score != Float.NEGATIVE_INFINITY;
                assert !Float.isNaN(score);
                totalHits++;
                arr.add(new ScoreDoc(doc + docBase, score));
            }

        };
    }


    CeTopScoreDocCollector(int numHits, int k) {
        super(new DocArray(numHits, false), k);
    }

    public void pullTopK(){
        arr.pullTopK(topK, true,true);
    }

    @Override
    protected TopDocs newTopDocs(ScoreDoc[] results, int start) {
        if (results == null) {
            return EMPTY_TOPDOCS;
        }
        // We need to compute maxScore in order to set it in TopDocs. If start == 0,
        // it means the largest element is already in results, use its score as
        // maxScore. Otherwise pop everything else, until the largest element is
        // extracted and use its score as maxScore.
        float maxScore = Float.NaN;
        if (start == 0) {
            maxScore = results[0].score;
        } else {
            IndexUtils.notifyLazyImplementation("CeTopScoreDocCollector / newTopDocs");
            //for (int i = pq.size(); i > 1; i--) { pq.pop(); }
            //maxScore = pq.pop().score;
        }

        return new TopDocs(totalHits, results, maxScore);
    }

    @Override
    public boolean needsScores() {
        return true;
    }
}

