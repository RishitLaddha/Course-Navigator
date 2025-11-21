import java.util.*;

public class BellmanFord {

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

        int V = g.nodes.size();

        for (int i = 0; i < V - 1; i++) {
            for (String u : g.adj.keySet()) {
                for (Graph.Edge e : g.adj.get(u)) {
                    double nd = dist.get(u) + e.combinedDifficulty;
                    if (nd < dist.get(e.to)) {
                        dist.put(e.to, nd);
                    }
                }
            }
        }

        long end = System.currentTimeMillis();
        return new Result(dist, end - start);
    }
}
