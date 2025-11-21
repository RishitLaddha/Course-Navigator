import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class Graph {

    public static class Edge {
        String from;
        String to;
        String relationType;
        double weightNonNegative;
        double combinedDifficulty;
        String fromName;
        String toName;
        String requiredByCompany;
        double importanceScore;
        boolean isCore;

        public Edge(String from, String to, String relationType, double weightNonNegative,
                    double combinedDifficulty, String fromName, String toName,
                    String requiredByCompany, double importanceScore, boolean isCore) {
            this.from = from;
            this.to = to;
            this.relationType = relationType;
            this.weightNonNegative = weightNonNegative;
            this.combinedDifficulty = combinedDifficulty;
            this.fromName = fromName;
            this.toName = toName;
            this.requiredByCompany = requiredByCompany;
            this.importanceScore = importanceScore;
            this.isCore = isCore;
        }
    }

    Map<String, List<Edge>> adj = new HashMap<>();
    Set<String> nodes = new HashSet<>();

    public void loadCSV(String filePath) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line;
        br.readLine(); // header

        while ((line = br.readLine()) != null) {
            // Skip empty lines
            if (line.trim().isEmpty()) continue;
            
            String[] parts = line.split(",", -1);
            
            // Skip malformed lines
            if (parts.length < 10) continue;

            String from = parts[0].trim();
            String to = parts[1].trim();
            String relationType = parts[2].trim();
            double weightNonNegative = Double.parseDouble(parts[3].trim());
            double combinedDifficulty = Double.parseDouble(parts[4].trim());
            String fromName = parts[5].trim();
            String toName = parts[6].trim();
            String requiredByCompany = parts[7].trim();
            double importanceScore = Double.parseDouble(parts[8].trim());
            boolean isCore = parts[9].trim().equalsIgnoreCase("TRUE");

            Edge e = new Edge(from, to, relationType, weightNonNegative, combinedDifficulty,
                    fromName, toName, requiredByCompany, importanceScore, isCore);

            adj.computeIfAbsent(from, k -> new ArrayList<>()).add(e);
            nodes.add(from);
            nodes.add(to);
        }

        br.close();
    }
}
