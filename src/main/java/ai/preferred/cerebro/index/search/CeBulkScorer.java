package ai.preferred.cerebro.index.search;

import org.apache.lucene.search.*;
import org.apache.lucene.util.Bits;

import ai.preferred.cerebro.index.utils.IndexUtils;

import java.io.IOException;

/**
 * Cerebro internal scorer.
 *
 * @author hpminh@apcs.vn
 */

public class CeBulkScorer extends BulkScorer {
    private final Scorer scorer;
    private final DocIdSetIterator iterator;
    private final TwoPhaseIterator twoPhase;

    public CeBulkScorer(Scorer scorer) {
        if (scorer == null) {
            throw new NullPointerException();
        }
        this.scorer = scorer;
        this.iterator = scorer.iterator();
        this.twoPhase = scorer.twoPhaseIterator();
    }

    @Override
    public int score(LeafCollector collector, Bits acceptDocs, int min, int max) throws IOException {
        collector.setScorer(scorer);
        if (scorer.docID() == -1 && min == 0 && max == DocIdSetIterator.NO_MORE_DOCS) {
            scoreAll(collector, iterator, twoPhase, acceptDocs);
            return DocIdSetIterator.NO_MORE_DOCS;
        } else {
            IndexUtils.notifyLazyImplementation("CeBulkScorer / score");
            int doc = scorer.docID();
            if (doc < min) {
                if (twoPhase == null) {
                    doc = iterator.advance(min);
                } else {
                    doc = twoPhase.approximation().advance(min);
                }
            }
            return scoreRange(collector, iterator, twoPhase, acceptDocs, doc, max);
        }
    }

    static int scoreRange(LeafCollector collector, DocIdSetIterator iterator, TwoPhaseIterator twoPhase,
                          Bits acceptDocs, int currentDoc, int end) throws IOException {
        if (twoPhase == null) {
            while (currentDoc < end) {
                if (acceptDocs == null || acceptDocs.get(currentDoc)) {
                    collector.collect(currentDoc);
                }
                currentDoc = iterator.nextDoc();
            }
            return currentDoc;
        } else {
            final DocIdSetIterator approximation = twoPhase.approximation();
            while (currentDoc < end) {
                if ((acceptDocs == null || acceptDocs.get(currentDoc)) && twoPhase.matches()) {
                    collector.collect(currentDoc);
                }
                currentDoc = approximation.nextDoc();
            }
            return currentDoc;
        }
    }


    static void scoreAll(LeafCollector collector, DocIdSetIterator iterator, TwoPhaseIterator twoPhase, Bits acceptDocs) throws IOException {
        if (twoPhase == null) {
            //long startTime = System.currentTimeMillis();
            for (int doc = iterator.nextDoc(); doc != DocIdSetIterator.NO_MORE_DOCS; doc = iterator.nextDoc()) {
                if (acceptDocs == null || acceptDocs.get(doc)) {
                    collector.collect(doc);
                }
            }
            //long endtime = System.currentTimeMillis();
            //System.out.println("Doc collect time" + (endtime - startTime));
        } else {
            // The scorer has an approximation, so run the approximation first, then check acceptDocs, then confirm
            final DocIdSetIterator approximation = twoPhase.approximation();
            for (int doc = approximation.nextDoc(); doc != DocIdSetIterator.NO_MORE_DOCS; doc = approximation.nextDoc()) {
                if ((acceptDocs == null || acceptDocs.get(doc)) && twoPhase.matches()) {
                    collector.collect(doc);
                }
            }
        }
    }

    @Override
    public long cost() {
        return iterator.cost();
    }
}
