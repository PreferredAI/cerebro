package ai.preferred.cerebro.index.hnsw;

import java.util.Comparator;

public class Candidate implements Comparable<Candidate>{
    public final int nodeId;
    public final double distance;
    final Comparator<Double> distanceComparator;

    public Candidate(int nodeId, double distance, Comparator<Double> distanceComparator) {
        this.nodeId = nodeId;
        this.distance = distance;
        this.distanceComparator = distanceComparator;
    }

    @Override
    public int compareTo(Candidate o) {
        return distanceComparator.compare(distance, o.distance);
    }
}
