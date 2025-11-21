import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implements the Takahashi-Matsuyama (TM) greedy algorithm for approximating the Steiner Tree.
 * This file is self-contained. The inner classes are duplicated from KMBAlgorithm.java
 * for simplicity as requested, but in a real project they would be in shared files.
 */
public class TakahashiMatsuyama {

    // =================================================================================
    // 1. DATA STRUCTURES (INNER CLASSES) - Duplicated for self-containment
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
            return Objects.hash(from, to) + Objects.hash(to, from) + Objects.hash(weight);
        }
    }

    /**
     * Stores node-specific metadata.
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

    // =================================================================================
    // 2. CORE ALGORITHMS (DIJKSTRA, TM STEPS)
    // =================================================================================

    /**
     * Public entry point for the Takahashi-Matsuyama Algorithm.
     * @param csvPath Path to the dataset file.
     * @return A SteinerResult object containing the tree and performance metrics.
     */
    public static SteinerResult runTM(String csvPath) {
        long startTime = System.currentTimeMillis();

        Graph originalGraph = new Graph();
        Set<String> terminals = new HashSet<>();
        try {
            loadGraphFromCSV(csvPath, originalGraph, terminals);
        } catch (IOException e) {
            System.err.println("Failed to load graph from CSV: " + e.getMessage());
            return null;
        }

        if (terminals.isEmpty()) {
            return new SteinerResult(0, new HashSet<>(), new HashSet<>(), 0);
        }

        Graph steinerGraph = new Graph();
        Set<String> unconnectedTerminals = new HashSet<>(terminals);

        // STEP A: Pick an initial terminal
        String firstTerminal = terminals.iterator().next();
        steinerGraph.addNode(firstTerminal, originalGraph.metadata.get(firstTerminal));
        unconnectedTerminals.remove(firstTerminal);
        
        // STEP B: Iteratively connect the closest unconnected terminal to the current tree
        while (!unconnectedTerminals.isEmpty()) {
            // Run a multi-source Dijkstra from all nodes currently in the tree
            DijkstraResult pathsFromTree = multiSourceDijkstra(originalGraph, steinerGraph.nodes);
            
            String closestTerminal = null;
            double minDistance = Double.POSITIVE_INFINITY;
            
            // Find which unconnected terminal is closest to the current tree
            for (String terminal : unconnectedTerminals) {
                if (pathsFromTree.distances.get(terminal) < minDistance) {
                    minDistance = pathsFromTree.distances.get(terminal);
                    closestTerminal = terminal;
                }
            }
            
            if (closestTerminal == null) {
                // This happens if some terminals are in a disconnected component
                break;
            }

            // STEP C: Merge the path to the closest terminal into the tree
            reconstructAndAddPath(closestTerminal, pathsFromTree.predecessors, originalGraph, steinerGraph);
            unconnectedTerminals.remove(closestTerminal);
        }

        // STEP D: Prune the resulting tree
        prune(steinerGraph, terminals);
        
        // STEP E: Calculate final cost and package results
        double totalCost = steinerGraph.getAllEdges().stream().mapToDouble(e -> e.weight).sum();
        Set<String> steinerNodes = new HashSet<>(steinerGraph.nodes);
        Set<Edge> steinerEdges = new HashSet<>(steinerGraph.getAllEdges());

        long endTime = System.currentTimeMillis();
        return new SteinerResult(totalCost, steinerNodes, steinerEdges, endTime - startTime);
    }
    
    /**
     * A multi-source version of Dijkstra's algorithm. Finds shortest paths from any node
     * in a set of start nodes to all other nodes in the graph.
     * @param graph The graph to search.
     * @param startNodes The set of source nodes for the search.
     * @return A DijkstraResult containing distances and predecessors.
     */
    private static DijkstraResult multiSourceDijkstra(Graph graph, Set<String> startNodes) {
        Map<String, Double> distances = new HashMap<>();
        Map<String, String> predecessors = new HashMap<>();
        PriorityQueue<Map.Entry<String, Double>> pq = new PriorityQueue<>(Map.Entry.comparingByValue());

        for (String node : graph.nodes) {
            distances.put(node, Double.POSITIVE_INFINITY);
        }

        // Initialize all start nodes with distance 0
        for (String startNode : startNodes) {
            distances.put(startNode, 0.0);
            pq.add(new AbstractMap.SimpleEntry<>(startNode, 0.0));
        }

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
     * Reconstructs a shortest path and adds its nodes and edges to the Steiner graph.
     * @param targetNode The end of the path to reconstruct.
     * @param predecessors The predecessor map from the Dijkstra run.
     * @param originalGraph The source of all graph data.
     * @param steinerGraph The destination graph to be augmented.
     */
    private static void reconstructAndAddPath(String targetNode, Map<String, String> predecessors, Graph originalGraph, Graph steinerGraph) {
        String current = targetNode;
        while (predecessors.containsKey(current)) {
            String prev = predecessors.get(current);

            final String currentFinal = current;
            Edge originalEdge = originalGraph.getNeighbors(prev).stream()
                .filter(e -> e.to.equals(currentFinal))
                .findFirst()
                .orElse(null);

            if (originalEdge != null) {
                steinerGraph.addNode(originalEdge.from, originalGraph.metadata.get(originalEdge.from));
                steinerGraph.addNode(originalEdge.to, originalGraph.metadata.get(originalEdge.to));
                
                // Add forward and reverse edges
                steinerGraph.addEdge(originalEdge);
                Edge reverseEdge = new Edge(originalEdge.to, originalEdge.from, originalEdge.weight, originalEdge.toName, originalEdge.fromName, originalEdge.isTerminalTo, originalEdge.isTerminalFrom, originalEdge.toDifficulty, originalEdge.fromDifficulty, originalEdge.toCategory, originalEdge.fromCategory, originalEdge.edgeType, originalEdge.overlapScore, originalEdge.isPrerequisiteHard, originalEdge.estimatedHours);
                steinerGraph.addEdge(reverseEdge);
            }
            current = prev;
        }
    }
    
    /**
     * Iteratively prunes non-terminal leaf nodes from the graph.
     * (Identical logic to KMB's prune function).
     */
    private static void prune(Graph graph, Set<String> terminals) {
        boolean removed;
        do {
            removed = false;
            List<String> nodesToRemove = graph.nodes.stream()
                .filter(node -> !terminals.contains(node) && graph.getNeighbors(node).size() == 1)
                .collect(Collectors.toList());

            if (!nodesToRemove.isEmpty()) {
                removed = true;
                for (String node : nodesToRemove) {
                    Edge incidentEdge = graph.getNeighbors(node).get(0);
                    String neighbor = incidentEdge.to;

                    graph.nodes.remove(node);
                    graph.adj.remove(node);
                    graph.allEdges.removeIf(e -> (e.from.equals(node) && e.to.equals(neighbor)) || (e.from.equals(neighbor) && e.to.equals(node)));

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
     * (Identical logic to KMB's loader function).
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

                graph.addNode(fromNode, new NodeMetadata(fromName, fromDifficulty, fromCategory));
                graph.addNode(toNode, new NodeMetadata(toName, toDifficulty, toCategory));

                Edge forwardEdge = new Edge(fromNode, toNode, edgeWeight, fromName, toName, isTerminalFrom, isTerminalTo, fromDifficulty, toDifficulty, fromCategory, toCategory, edgeType, overlapScore, isPrerequisiteHard, estimatedHours);
                Edge reverseEdge = new Edge(toNode, fromNode, edgeWeight, toName, fromName, isTerminalTo, isTerminalFrom, toDifficulty, fromDifficulty, toCategory, fromCategory, edgeType, overlapScore, isPrerequisiteHard, estimatedHours);
                graph.addEdge(forwardEdge);
                graph.addEdge(reverseEdge);
                
                if (isTerminalFrom) terminals.add(fromNode);
                if (isTerminalTo) terminals.add(toNode);
            }
        }
    }
}