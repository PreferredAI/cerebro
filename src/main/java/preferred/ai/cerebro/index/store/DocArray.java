package preferred.ai.cerebro.index.store;

import org.apache.lucene.search.ScoreDoc;


/**
 * A data construct to get out the top k document
 * according a specific score measurement.
 */
public class DocArray extends Container<ScoreDoc> {

    public DocArray(int size, boolean prePopulate) {
        super(size, () -> {
            if (prePopulate) {
                // Always set the doc Id to MAX_VALUE so that it won't be favored by
                // lessThan. This generally should not happen since if score is not NEG_INF,
                // TopScoreDocCollector will always add the object to the queue.
                return new ScoreDoc(Integer.MAX_VALUE, Float.NEGATIVE_INFINITY);
            } else {
                return null;
            }
        });
    }


    @Override
    protected boolean lessThan(ScoreDoc a, ScoreDoc b) {
        if (a.score == b.score)
            return a.doc > b.doc;
        else
            return a.score < b.score;
    }

    @Override
    public void calculateScore(ScoreDoc target) {
        //future implementation
        //do not use this function right now
        //socre is calculated in Lucene framework
    }
}
