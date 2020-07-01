package ai.preferred.cerebro.index.lsh.searcher;





import ai.preferred.cerebro.index.Searcher;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.Similarity;

import ai.preferred.cerebro.index.lsh.builder.LocalitySensitiveHash;
import ai.preferred.cerebro.index.utils.IndexConst;
import ai.preferred.cerebro.index.utils.IndexUtils;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;


/**
 *
 * A Query that matches Documents by the hashcode
 * produced by their latent vector.
 *
 * @author hpminh@apcs.vn
 */
public class VectorQuery<TVector> extends Query {
    private final Term term;
    private final TVector vec;
    private final TermContext perReaderTermState;


    public VectorQuery(TVector vec, Term t) {
        this.vec = vec;
        term = Objects.requireNonNull(t);
        perReaderTermState = null;
    }

    public VectorQuery(TVector vec, LocalitySensitiveHash lsh, TermContext states) {
        this.vec = vec;
        term = new Term(IndexConst.HashFieldName, lsh.getHashBit(vec));
        perReaderTermState = states;
    }

    public VectorQuery(TVector vec, Term t, TermContext states) {
        this.vec = vec;
        term = Objects.requireNonNull(t);
        perReaderTermState = states;
    }

    public Term getTerm() {
        return term;
    }

    public TVector getVec() {
        return vec;
    }

    @Override
    public String toString(String field) {
        IndexUtils.notifyLazyImplementation("VectorQuery / toString");
        return null;
    }

    @Override
    public boolean equals(Object other) {
        return sameClassAs(other) &&
                term.equals(((VectorQuery) other).term);
    }

    @Override
    public int hashCode() {
        return classHash() ^ term.hashCode();
    }

    @Override
    public Weight createWeight(IndexSearcher searcher, boolean needsScores, float boost) throws IOException{
        return new VectorWeight<TVector>(searcher, needsScores, perReaderTermState);
    }




    final class VectorWeight<TVector> extends Weight {
        private final VectorSimilarity similarity;
        private final Similarity.SimWeight stats;
        private final TermContext termStates;
        private final boolean needsScores;

        public VectorWeight(IndexSearcher searcher, boolean needsScores, TermContext termStates) throws IOException {
            super(VectorQuery.this);
            this.needsScores = needsScores;
            this.termStates = termStates;
            this.similarity = ((Searcher<TVector>) searcher).getVectorSimilarity();

            final CollectionStatistics collectionStats;
            //final TermStatistics termStats;
            if (needsScores) {
                collectionStats = searcher.collectionStatistics(term.field());
                //termStats = searcher.termStatistics(term, termStates);
            } else {
                // we do not need the actual stats, use fake stats with docFreq=maxDoc and ttf=-1
                final int maxDoc = searcher.getIndexReader().maxDoc();
                collectionStats = new CollectionStatistics(term.field(), maxDoc, -1, -1, -1);
                //termStats = new TermStatistics(term.bytes(), maxDoc, -1);
            }
            this.stats = similarity.computeWeight(VectorQuery.this.vec, searcher.getIndexReader(), collectionStats);

        }

        @Override
        public void extractTerms(Set<Term> terms) {
            terms.add(getTerm());
        }

        @Override
        public Matches matches(LeafReaderContext context, int doc) throws IOException {
            IndexUtils.notifyLazyImplementation("VectorWeight / matches");
            TermsEnum te = getTermsEnum(context);
            if (te == null) {
                return null;
            }
            if (!context.reader().terms(term.field()).hasPositions()) {
                return super.matches(context, doc);
            }
            return MatchesUtils.forField(term.field(), () -> {
                PostingsEnum pe = te.postings(null, PostingsEnum.OFFSETS);
                if (pe.advance(doc) != doc) {
                    return null;
                }
                return new TermMatchesIterator(getQuery(), pe);
            });
        }

        @Override
        public String toString() {
            return "weight(" + VectorQuery.this + ")";
        }

        @Override
        public Scorer scorer(LeafReaderContext context) throws IOException {
            assert termStates == null || termStates.wasBuiltFor(ReaderUtil.getTopLevelContext(context)) : "The top-reader used to createPersonalizedDoc Weight is not the same as the current reader's top-reader (" + ReaderUtil.getTopLevelContext(context);
            ;
            final TermsEnum termsEnum = getTermsEnum(context);
            if (termsEnum == null) {
                return null;
            }
            PostingsEnum docs = termsEnum.postings(null, needsScores ? PostingsEnum.FREQS : PostingsEnum.NONE);
            assert docs != null;
            return new VectorScorer(this, docs, similarity.simScorer(stats, context));
        }

        @Override
        public boolean isCacheable(LeafReaderContext ctx) {
            return true;
        }

        /**
         * Returns a {@link TermsEnum} positioned at this weights Term or null if
         * the term does not exist in the given context
         */
        private TermsEnum getTermsEnum(LeafReaderContext context) throws IOException {
            if (termStates != null) {
                // TermQuery either used as a Query or the term states have been provided at construction time
                assert termStates.wasBuiltFor(ReaderUtil.getTopLevelContext(context)) : "The top-reader used to createPersonalizedDoc Weight is not the same as the current reader's top-reader (" + ReaderUtil.getTopLevelContext(context);
                final TermState state = termStates.get(context.ord);
                if (state == null) { // term is not present in that reader
                    assert termNotInReader(context.reader(), term) : "no termstate found but term exists in reader term=" + term;
                    return null;
                }
                final TermsEnum termsEnum = context.reader().terms(term.field()).iterator();
                termsEnum.seekExact(term.bytes(), state);
                return termsEnum;
            } else {
                // TermQuery used as a filter, so the term states have not been built up front
                Terms terms = context.reader().terms(term.field());
                if (terms == null) {
                    return null;
                }
                final TermsEnum termsEnum = terms.iterator();
                if (termsEnum.seekExact(term.bytes())) {
                    return termsEnum;
                } else {
                    return null;
                }
            }
        }

        private boolean termNotInReader(LeafReader reader, Term term) throws IOException {
            // only called from assert
            // System.out.println("TQ.termNotInReader reader=" + reader + " term=" +
            // field + ":" + bytes.utf8ToString());
            return reader.docFreq(term) == 0;
        }

        @Override
        public BulkScorer bulkScorer(LeafReaderContext context) throws IOException {

            Scorer scorer = scorer(context);
            if (scorer == null) {
                // No docs match
                return null;
            }

            // This impl always scores docs in order, so we can
            // ignore scoreDocsInOrder:
            return new DefaultBulkScorer(scorer);
        }

        @Override
        public Explanation explain(LeafReaderContext context, int doc) throws IOException {
            IndexUtils.notifyLazyImplementation("VectorQuery/explain");
            return null;
//            TermScorer scorer = (TermScorer) scorer(context);
//            if (scorer != null) {
//                int newDoc = scorer.iterator().advance(doc);
//                if (newDoc == doc) {
//                    float freq = scorer.freq();
//                    Similarity.SimScorer docScorer = similarity.simScorer(stats, context);
//                    Explanation freqExplanation = Explanation.match(freq, "termFreq=" + freq);
//                    Explanation scoreExplanation = docScorer.explain(doc, freqExplanation);
//                    return Explanation.match(
//                            scoreExplanation.getValue(),
//                            "weight(" + getQuery() + " in " + doc + ") ["
//                                    + similarity.getClass().getSimpleName() + "], result of:",
//                            scoreExplanation);
//                }
//            }
//            return Explanation.noMatch("no matching term");
        }
    }

}

