import java.util.*;

public class SSSP {

    public static class Result {
        Map<String, Integer> dist;
        long timeMs;

        public Result(Map<String, Integer> dist, long timeMs) {
            this.dist = dist;
            this.timeMs = timeMs;
        }
    }

    public static Result run(Graph g, String src) {
        long start = System.currentTimeMillis();

        Map<String, Integer> dist = new HashMap<>();
        for (String n : g.nodes) dist.put(n, Integer.MAX_VALUE);
        dist.put(src, 0);

        Queue<String> q = new LinkedList<>();
        q.add(src);

        while (!q.isEmpty()) {
            String u = q.poll();
            if (!g.adj.containsKey(u)) continue;

            for (Graph.Edge e : g.adj.get(u)) {
                if (dist.get(e.to) > dist.get(u) + 1) {
                    dist.put(e.to, dist.get(u) + 1);
                    q.add(e.to);
                }
            }
        }

        long end = System.currentTimeMillis();
        return new Result(dist, end - start);
    }
}
