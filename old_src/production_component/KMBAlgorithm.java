import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implements the Kou–Markowsky–Berman (KMB) algorithm for approximating the Steiner Tree.
 * This file is self-contained and includes all necessary classes for graph representation,
 * data loading, and the algorithm itself.
 */
public class KMBAlgorithm {

    // =================================================================================
    // 1. DATA STRUCTURES (INNER CLASSES)
    // =================================================================================

    /**
     * Represents a weighted edge in the graph, storing all 15 columns from the dataset.
     */
    static class Edge {
        String from, to, fromName, toName, fromCategory, toCategory, edgeType;
        double weight;
        boolean isTerminalFrom, isTerminalTo, isPrerequisiteHard;
        int fromDifficulty, toDifficulty;
        double overlapScore;
        int estimatedHours;

        public Edge(String from, String to, double weight, String fromName, String toName, boolean isTerminalFrom, boolean isTerminalTo, int fromDifficulty, int toDifficulty, String fromCategory, String toCategory, String edgeType, double overlapScore, boolean isPrerequisiteHard, int estimatedHours) {
            this.from = from;
            this.to = to;
            this.weight = weight;
            this.fromName = fromName;
            this.toName = toName;
            this.isTerminalFrom = isTerminalFrom;
            this.isTerminalTo = isTerminalTo;
            this.fromDifficulty = fromDifficulty;
            this.toDifficulty = toDifficulty;
            this.fromCategory = fromCategory;
            this.toCategory = toCategory;
            this.edgeType = edgeType;
            this.overlapScore = overlapScore;
            this.isPrerequisiteHard = isPrerequisiteHard;
            this.estimatedHours = estimatedHours;
        }

        @Override
        public String toString() {
            return String.format("%s (%s) -> %s (%s) [w:%.2f]", from, fromName, to, toName, weight);
        }

        // Equals and hashCode are important for using Edges in Sets
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Edge edge = (Edge) o;
            return Double.compare(edge.weight, weight) == 0 &&
                    (Objects.equals(from, edge.from) && Objects.equals(to, edge.to) ||
                     Objects.equals(from, edge.to) && Objects.equals(to, edge.from));
        }

        @Override
        public int hashCode() {
            // Symmetrical hash code for undirected edges
            return Objects.hash(from, to) + Objects.hash(to, from) + Objects.hash(weight);
        }
    }

    /**
     * Stores node-specific metadata that doesn't belong to an edge.
     */
    static class NodeMetadata {
        String name;
        int difficulty;
        String category;

        public NodeMetadata(String name, int difficulty, String category) {
            this.name = name;
            this.difficulty = difficulty;
            this.category = category;
        }
    }

    /**
     * Represents the graph using an adjacency list.
     */
    static class Graph {
        Map<String, List<Edge>> adj = new HashMap<>();
        Set<String> nodes = new HashSet<>();
        Map<String, NodeMetadata> metadata = new HashMap<>();
        List<Edge> allEdges = new ArrayList<>();

        public void addNode(String nodeId, NodeMetadata meta) {
            adj.putIfAbsent(nodeId, new ArrayList<>());
            metadata.putIfAbsent(nodeId, meta);
            nodes.add(nodeId);
        }

        public void addEdge(Edge edge) {
            // Ensure nodes exist in the graph before adding an edge
            adj.putIfAbsent(edge.from, new ArrayList<>());
            adj.putIfAbsent(edge.to, new ArrayList<>());
            nodes.add(edge.from);
            nodes.add(edge.to);
            adj.get(edge.from).add(edge);
            allEdges.add(edge);
        }

        public List<Edge> getNeighbors(String node) {
            return adj.getOrDefault(node, Collections.emptyList());
        }

        public List<Edge> getAllEdges() {
            return allEdges;
        }

        public boolean hasNode(String node) {
            return nodes.contains(node);
        }
    }

    /**
     * Container for the results of a Steiner Tree algorithm execution.
     */
    static class SteinerResult {
        double totalCost;
        Set<String> steinerNodes;
        Set<Edge> steinerEdges;
        long runtimeMs;

        public SteinerResult(double totalCost, Set<String> steinerNodes, Set<Edge> steinerEdges, long runtimeMs) {
            this.totalCost = totalCost;
            this.steinerNodes = steinerNodes;
            this.steinerEdges = steinerEdges;
            this.runtimeMs = runtimeMs;
        }
    }
    
    /**
     * Container for the results of Dijkstra's algorithm.
     */
    static class DijkstraResult {
        Map<String, Double> distances;
        Map<String, String> predecessors;

        public DijkstraResult(Map<String, Double> distances, Map<String, String> predecessors) {
            this.distances = distances;
            this.predecessors = predecessors;
        }
    }
    
    /**
     * Implements the Union-Find data structure for Kruskal's algorithm.
     */
    static class UnionFind {
        private Map<String, String> parent = new HashMap<>();

        public void makeSet(Collection<String> elements) {
            for (String i : elements) {
                parent.put(i, i);
            }
        }

        public String find(String i) {
            if (!parent.containsKey(i)) {
                return null;
            }
            if (parent.get(i).equals(i)) {
                return i;
            }
            // Path compression
            String root = find(parent.get(i));
            parent.put(i, root);
            return root;
        }

        public void union(String i, String j) {
            String rootI = find(i);
            String rootJ = find(j);
            if (!rootI.equals(rootJ)) {
                parent.put(rootI, rootJ);
            }
        }
    }

    // =================================================================================
    // 2. CORE ALGORITHMS (DIJKSTRA, KMB STEPS)
    // =================================================================================

    /**
     * Public entry point for the KMB Algorithm.
     * @param csvPath Path to the dataset file.
     * @return A SteinerResult object containing the tree and performance metrics.
     */
    public static SteinerResult runKMB(String csvPath) {
        long startTime = System.currentTimeMillis();

        // Load graph data from CSV
        Graph graph = new Graph();
        Set<String> terminals = new HashSet<>();
        try {
            loadGraphFromCSV(csvPath, graph, terminals);
        } catch (IOException e) {
            System.err.println("Failed to load graph from CSV: " + e.getMessage());
            return null;
        }

        // STEP A: Compute all-pairs shortest paths between terminals
        Map<String, DijkstraResult> allShortestPaths = new HashMap<>();
        for (String terminal : terminals) {
            allShortestPaths.put(terminal, dijkstra(graph, terminal));
        }

        // STEP B: Build the metric closure graph (a complete graph of terminals)
        Graph metricClosure = new Graph();
        List<String> terminalList = new ArrayList<>(terminals);
        for (int i = 0; i < terminalList.size(); i++) {
            for (int j = i + 1; j < terminalList.size(); j++) {
                String u = terminalList.get(i);
                String v = terminalList.get(j);
                double weight = allShortestPaths.get(u).distances.get(v);
                if (weight < Double.POSITIVE_INFINITY) {
                    metricClosure.addEdge(new Edge(u, v, weight, null, null, true, true, 0,0,null,null,null,0,false,0));
                }
            }
        }

        // STEP C: Find the Minimum Spanning Tree (MST) on the metric closure
        Set<Edge> mstEdges = kruskalMST(metricClosure);

        // STEP D: Expand MST edges back to original graph paths
        Graph steinerGraph = new Graph();
        for (Edge mstEdge : mstEdges) {
            String u = mstEdge.from;
            String v = mstEdge.to;
            // Use the pre-computed predecessor map to reconstruct the path
            reconstructPath(u, v, allShortestPaths.get(u).predecessors, graph, steinerGraph);
        }

        // STEP E: Prune the Steiner Tree
        prune(steinerGraph, terminals);

        // STEP F: Calculate final cost and package results
        double totalCost = steinerGraph.getAllEdges().stream().mapToDouble(e -> e.weight).sum();
        Set<String> steinerNodes = new HashSet<>(steinerGraph.nodes);
        Set<Edge> steinerEdges = new HashSet<>(steinerGraph.getAllEdges());

        long endTime = System.currentTimeMillis();
        return new SteinerResult(totalCost, steinerNodes, steinerEdges, endTime - startTime);
    }
    
    /**
     * Implements Dijkstra's algorithm to find the shortest paths from a single source.
     * @param graph The graph to search.
     * @param startNode The starting node.
     * @return A DijkstraResult containing distances and predecessors.
     */
    private static DijkstraResult dijkstra(Graph graph, String startNode) {
        Map<String, Double> distances = new HashMap<>();
        Map<String, String> predecessors = new HashMap<>();
        // Priority queue stores entries of <Node, Distance>
        PriorityQueue<Map.Entry<String, Double>> pq = new PriorityQueue<>(Map.Entry.comparingByValue());

        // Initialize all distances to infinity
        for (String node : graph.nodes) {
            distances.put(node, Double.POSITIVE_INFINITY);
        }
        distances.put(startNode, 0.0);

        pq.add(new AbstractMap.SimpleEntry<>(startNode, 0.0));

        while (!pq.isEmpty()) {
            String u = pq.poll().getKey();

            for (Edge edge : graph.getNeighbors(u)) {
                String v = edge.to;
                double weight = edge.weight;
                if (distances.get(u) + weight < distances.get(v)) {
                    distances.put(v, distances.get(u) + weight);
                    predecessors.put(v, u);
                    pq.add(new AbstractMap.SimpleEntry<>(v, distances.get(v)));
                }
            }
        }
        return new DijkstraResult(distances, predecessors);
    }

    /**
     * Implements Kruskal's algorithm to find the Minimum Spanning Tree (MST).
     * @param graph The graph on which to find the MST (in our case, the metric closure).
     * @return A set of edges forming the MST.
     */
    private static Set<Edge> kruskalMST(Graph graph) {
        Set<Edge> mst = new HashSet<>();
        List<Edge> sortedEdges = graph.getAllEdges().stream()
            .sorted(Comparator.comparingDouble(e -> e.weight))
            .collect(Collectors.toList());

        UnionFind uf = new UnionFind();
        uf.makeSet(graph.nodes);

        for (Edge edge : sortedEdges) {
            if (uf.find(edge.from) != null && uf.find(edge.to) != null && !uf.find(edge.from).equals(uf.find(edge.to))) {
                mst.add(edge);
                uf.union(edge.from, edge.to);
            }
        }
        return mst;
    }
    
    /**
     * Reconstructs a shortest path between two nodes using a predecessor map and adds it to the steinerGraph.
     * @param u The start node of the path.
     * @param v The end node of the path.
     * @param predecessors The map from a node to its predecessor on the shortest path from u.
     * @param originalGraph The complete original graph, to look up edge details.
     * @param steinerGraph The graph being built, to which path edges are added.
     */
    private static void reconstructPath(String u, String v, Map<String, String> predecessors, Graph originalGraph, Graph steinerGraph) {
        String current = v;
        while (predecessors.containsKey(current)) {
            String prev = predecessors.get(current);
            // Find the original edge to get all metadata
            final String currentFinal = current;
            Edge originalEdge = originalGraph.getNeighbors(prev).stream()
                .filter(e -> e.to.equals(currentFinal))
                .findFirst()
                .orElse(null);

            if (originalEdge != null) {
                // Add nodes and edge to the Steiner graph
                steinerGraph.addNode(originalEdge.from, originalGraph.metadata.get(originalEdge.from));
                steinerGraph.addNode(originalEdge.to, originalGraph.metadata.get(originalEdge.to));
                steinerGraph.addEdge(originalEdge);

                // Add reverse edge to maintain undirected property for pruning
                Edge reverseEdge = new Edge(originalEdge.to, originalEdge.from, originalEdge.weight, originalEdge.toName, originalEdge.fromName, originalEdge.isTerminalTo, originalEdge.isTerminalFrom, originalEdge.toDifficulty, originalEdge.fromDifficulty, originalEdge.toCategory, originalEdge.fromCategory, originalEdge.edgeType, originalEdge.overlapScore, originalEdge.isPrerequisiteHard, originalEdge.estimatedHours);
                steinerGraph.addEdge(reverseEdge);
            }
            current = prev;
            if (current.equals(u)) break;
        }
    }

    /**
     * Iteratively prunes non-terminal leaf nodes from the graph.
     * A leaf node is a node with a degree of 1.
     * @param graph The graph to prune.
     * @param terminals The set of terminal nodes that should not be pruned.
     */
    private static void prune(Graph graph, Set<String> terminals) {
        boolean removed;
        do {
            removed = false;
            // Find all leaf nodes (degree 1) that are not terminals
            List<String> nodesToRemove = graph.nodes.stream()
                .filter(node -> !terminals.contains(node) && graph.getNeighbors(node).size() == 1)
                .collect(Collectors.toList());

            if (!nodesToRemove.isEmpty()) {
                removed = true;
                for (String node : nodesToRemove) {
                    // Remove the node and its incident edge from the graph
                    Edge incidentEdge = graph.getNeighbors(node).get(0);
                    String neighbor = incidentEdge.to;

                    graph.nodes.remove(node);
                    graph.adj.remove(node);
                    graph.allEdges.removeIf(e -> (e.from.equals(node) && e.to.equals(neighbor)) || (e.from.equals(neighbor) && e.to.equals(node)));

                    // Also remove the edge from the neighbor's adjacency list
                    if (graph.adj.containsKey(neighbor)) {
                        graph.adj.get(neighbor).removeIf(e -> e.to.equals(node));
                    }
                }
            }
        } while (removed);
    }
    
    // =================================================================================
    // 3. UTILITY / DATA LOADING
    // =================================================================================

    /**
     * Loads a graph from a CSV file.
     * The graph is treated as undirected, so a reverse edge is added for each row.
     * @param csvPath Path to the CSV file.
     * @param graph The Graph object to populate.
     * @param terminals The Set of terminal nodes to populate.
     * @throws IOException If the file cannot be read.
     */
    private static void loadGraphFromCSV(String csvPath, Graph graph, Set<String> terminals) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            String line;
            br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                // Parse all 15 columns
                String fromNode = values[0];
                String fromName = values[1];
                String toNode = values[2];
                String toName = values[3];
                double edgeWeight = Double.parseDouble(values[4]);
                boolean isTerminalFrom = Boolean.parseBoolean(values[5]);
                boolean isTerminalTo = Boolean.parseBoolean(values[6]);
                int fromDifficulty = Integer.parseInt(values[7]);
                int toDifficulty = Integer.parseInt(values[8]);
                String fromCategory = values[9];
                String toCategory = values[10];
                String edgeType = values[11];
                double overlapScore = Double.parseDouble(values[12]);
                boolean isPrerequisiteHard = Boolean.parseBoolean(values[13]);
                int estimatedHours = Integer.parseInt(values[14]);

                // Add nodes and metadata to the graph
                graph.addNode(fromNode, new NodeMetadata(fromName, fromDifficulty, fromCategory));
                graph.addNode(toNode, new NodeMetadata(toName, toDifficulty, toCategory));

                // Add forward and reverse edges to make the graph undirected
                Edge forwardEdge = new Edge(fromNode, toNode, edgeWeight, fromName, toName, isTerminalFrom, isTerminalTo, fromDifficulty, toDifficulty, fromCategory, toCategory, edgeType, overlapScore, isPrerequisiteHard, estimatedHours);
                Edge reverseEdge = new Edge(toNode, fromNode, edgeWeight, toName, fromName, isTerminalTo, isTerminalFrom, toDifficulty, fromDifficulty, toCategory, fromCategory, edgeType, overlapScore, isPrerequisiteHard, estimatedHours);
                graph.addEdge(forwardEdge);
                graph.addEdge(reverseEdge);
                
                // Identify terminals
                if (isTerminalFrom) terminals.add(fromNode);
                if (isTerminalTo) terminals.add(toNode);
            }
        }
    }
}