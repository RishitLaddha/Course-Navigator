import java.util.*;

public class Dijkstra {

    public static class Result {
        Map<String, Double> dist;
        long timeMs;

        public Result(Map<String, Double> dist, long timeMs) {
            this.dist = dist;
            this.timeMs = timeMs;
        }
    }

    public static Result run(Graph g, String src) {
        long start = System.currentTimeMillis();

        Map<String, Double> dist = new HashMap<>();
        for (String n : g.nodes) dist.put(n, Double.POSITIVE_INFINITY);
        dist.put(src, 0.0);

        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingDouble(dist::get));
        pq.add(src);

        while (!pq.isEmpty()) {
            String u = pq.poll();
            if (!g.adj.containsKey(u)) continue;

            for (Graph.Edge e : g.adj.get(u)) {
                double nd = dist.get(u) + e.weightNonNegative;
                if (nd < dist.get(e.to)) {
                    dist.put(e.to, nd);
                    pq.add(e.to);
                }
            }
        }

        long end = System.currentTimeMillis();
        return new Result(dist, end - start);
    }
}
