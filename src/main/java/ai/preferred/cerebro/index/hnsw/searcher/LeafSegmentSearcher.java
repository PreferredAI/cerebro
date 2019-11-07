package ai.preferred.cerebro.index.hnsw.searcher;

import ai.preferred.cerebro.index.hnsw.*;
import ai.preferred.cerebro.index.hnsw.structure.BoundedMaxHeap;
import ai.preferred.cerebro.index.hnsw.structure.Candidate;
import ai.preferred.cerebro.index.hnsw.Node;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.eclipse.collections.impl.list.mutable.primitive.IntArrayList;

public class LeafSegmentSearcher<TVector> extends LeafSegment<TVector> {

    LeafSegmentSearcher(HnswManager parent, int numName, String idxDir) {
        super(parent, numName, idxDir, Mode.SEARCH);
    }

    public TopDocs findNearest(TVector query, int k) {

        if (entryPoint == null) {
            return new TopDocs(0, null, Float.NaN);
        }

        Node<TVector> entryPointCopy = entryPoint;

        Node<TVector> currObj = entryPointCopy;

        double curDist = handler.distance(query, currObj.vector());

        for (int activeLevel = entryPointCopy.maxLevel(); activeLevel > 0; activeLevel--) {

            boolean changed = true;
            while (changed){
                changed = false;
                IntArrayList candidateConnections = currObj.outConns[activeLevel];

                for (int i = 0; i < candidateConnections.size(); i++) {

                    int candidateId = candidateConnections.get(i);

                    double candidateDistance = handler.distance(query, nodes[candidateId].vector());
                    if (candidateDistance < curDist) {
                        curDist = candidateDistance;
                        currObj = nodes[candidateId];
                        changed = true;
                    }
                }
            }
        }

        BoundedMaxHeap topCandidates = searchLayer(currObj, query, Math.max(ef, k), 0);

        while (topCandidates.size() > k) {
            topCandidates.pop();
        }
        ScoreDoc[] hits = new ScoreDoc[topCandidates.size()];
        for (int i = topCandidates.size() - 1; i >= 0; i--) {
            Candidate h = topCandidates.pop();
            hits[i] = new ScoreDoc(nodes[h.nodeId].item.externalId, (float) (1 - h.distance));
        }


        return new TopDocs(topCandidates.size(), hits, hits[0].score);
    }
}
