# ⭐ Steiner Tree Approximation Algorithms in Java

## 🚀 Overview

This project implements and compares two well-known approximation algorithms for solving the **Steiner Tree Problem**: the **Kou–Markowsky–Berman (KMB)** algorithm and the **Takahashi–Matsuyama (TM)** algorithm.

The Steiner Tree Problem is a classic optimization problem in graph theory that seeks to find the minimum-cost tree connecting a set of designated "terminal" nodes in a weighted graph. This tree may include additional non-terminal nodes (called Steiner nodes) if they help reduce the total cost.

This implementation processes a graph from a CSV file containing course prerequisite relationships, runs both approximation algorithms, and presents a detailed comparison of their performance metrics, including total cost, runtime, node count, and edge count.

## ✨ Features

- **Two Steiner Algorithms**: Complete implementations of the KMB and Takahashi-Matsuyama algorithms for Steiner Tree approximation
- **Detailed CSV Parsing**: Loads a graph from a CSV file, parsing all 15 columns for complete metadata preservation
- **Custom Graph Implementation**: Self-contained Graph, Edge, and NodeMetadata classes with no external dependencies
- **Manual Shortest Path**: Implements Dijkstra's algorithm from scratch for pathfinding between nodes
- **Zero Dependencies**: Written in pure Java 8+ with no external libraries required
- **Comparative Analysis**: The Main.java executor runs both algorithms and prints a detailed comparison table with cost differences and performance metrics
- **Built-in Dummy Data**: Automatically creates a sample dataset for immediate, out-of-the-box execution

## 📂 Project Structure

| File | Description |
|------|-------------|
| `KMBAlgorithm.java` | Self-contained implementation of the Kou–Markowsky–Berman algorithm. Includes inner classes for graph representation (Graph, Edge, NodeMetadata), Dijkstra's algorithm, Kruskal's MST, Union-Find data structure, and the complete KMB algorithm workflow. |
| `TakahashiMatsuyama.java` | Self-contained implementation of the Takahashi–Matsuyama greedy algorithm. Includes similar inner classes for graph representation and implements a multi-source Dijkstra approach for iteratively connecting terminals. |
| `Main.java` | Main executor class that orchestrates the execution of both algorithms. Loads the dataset, runs both algorithms sequentially, prints formatted results, and generates a comprehensive comparison table. Also includes a utility function to create a dummy dataset if one doesn't exist. |
| `Prod_Data/` | Directory containing the input dataset. The CSV file `synthetic_dataset.csv` should be placed in this folder. The Main class will automatically create a dummy dataset here if the file doesn't exist. |

## 📊 Dataset Explained

The input is a CSV file located at `Prod_Data/synthetic_dataset.csv`. Each row represents a directed edge in the graph with extensive metadata. The graph is treated as **undirected** by the algorithms (reverse edges are automatically added during loading).

The dataset contains **15 columns** with the following structure:

| Column Name | Type | Description |
|-------------|------|-------------|
| `from_node` | String | Unique identifier for the source node (e.g., course code) |
| `from_name` | String | Human-readable name of the source node (e.g., course title) |
| `to_node` | String | Unique identifier for the destination node |
| `to_name` | String | Human-readable name of the destination node |
| `edge_weight` | Double | Cost/weight of the edge (used for optimization) |
| `is_terminal_from` | Boolean | Indicates if the source node is a terminal (must be included in Steiner Tree) |
| `is_terminal_to` | Boolean | Indicates if the destination node is a terminal |
| `from_difficulty` | Integer | Difficulty level of the source node (1-5 scale) |
| `to_difficulty` | Integer | Difficulty level of the destination node (1-5 scale) |
| `from_category` | String | Category/domain of the source node (e.g., "Programming", "Math") |
| `to_category` | String | Category/domain of the destination node |
| `edge_type` | String | Type of relationship (e.g., "prerequisite", "recommended", "alternative") |
| `overlap_score` | Double | Similarity/overlap score between nodes (0.0-1.0) |
| `is_prerequisite_hard` | Boolean | Indicates if this is a hard prerequisite requirement |
| `estimated_hours` | Integer | Estimated time investment for this edge/relationship |

## 🧠 Algorithms Explained

### 1. Kou–Markowsky–Berman (KMB) Algorithm

The KMB algorithm is a well-known 2-approximation algorithm for the Steiner Tree problem. It works by building a Minimum Spanning Tree (MST) on the metric closure of the terminal nodes, then expanding those edges back to paths in the original graph.

**High-level Strategy**: Compute shortest paths between all terminal pairs, build a complete graph (metric closure) on terminals with shortest path distances as edge weights, find the MST of this closure, expand MST edges to original graph paths, and prune unnecessary nodes.

**Implementation Steps**:

- **Step A: Compute Shortest Paths Between All Terminals**
  - For each terminal node, run Dijkstra's algorithm to find shortest paths to all other nodes
  - Store distances and predecessor maps for path reconstruction

- **Step B: Build Metric Closure Graph**
  - Create a complete graph where nodes are terminals
  - Edge weights are the shortest path distances between terminal pairs

- **Step C: Minimum Spanning Tree on Metric Closure**
  - Use Kruskal's algorithm with Union-Find to find the MST of the metric closure
  - This gives the optimal way to connect terminals using shortest paths

- **Step D: Expand MST Edges**
  - For each edge in the MST, reconstruct the actual shortest path in the original graph
  - Add all nodes and edges from these paths to the Steiner tree

- **Step E: Prune the Steiner Tree**
  - Iteratively remove non-terminal leaf nodes (nodes with degree 1 that aren't terminals)
  - Continue until no more pruning is possible

- **Step F: Output Results**
  - Calculate total cost, collect all nodes and edges, measure runtime, and return results

### 2. Takahashi–Matsuyama (TM) Algorithm

The TM algorithm is a greedy approximation algorithm that builds the Steiner tree incrementally by connecting terminals one at a time to the growing tree.

**High-level Strategy**: Start with one terminal, then iteratively find the unconnected terminal closest to the current tree, add the shortest path to that terminal, and repeat until all terminals are connected.

**Implementation Steps**:

- **Step A: Pick an Initial Terminal**
  - Select an arbitrary terminal as the starting point
  - Initialize the Steiner tree with this single node

- **Step B: Connect Terminals One by One**
  - While there are unconnected terminals:
    - Run multi-source Dijkstra from all nodes currently in the tree
    - Find the unconnected terminal with minimum distance to the tree
    - Select this terminal for connection

- **Step C: Merge Paths**
  - Reconstruct the shortest path from the tree to the selected terminal
  - Add all nodes and edges from this path to the Steiner tree
  - Mark the terminal as connected

- **Step D: Prune the Tree**
  - After all terminals are connected, prune non-terminal leaf nodes
  - Same pruning logic as KMB algorithm

- **Step E: Output Results**
  - Calculate total cost, collect all nodes and edges, measure runtime, and return results

## ⚙️ How to Run

### 1. Prerequisites

- **Java JDK 8 or higher** is required
- Verify installation by running: `java -version` and `javac -version`

### 2. Directory Structure

Ensure your project has the following structure:

```
.
├── KMBAlgorithm.java
├── TakahashiMatsuyama.java
├── Main.java
└── Prod_Data/
    └── synthetic_dataset.csv
```

> **Note**: The `Main.java` file will automatically create a dummy `synthetic_dataset.csv` in the `Prod_Data/` folder on the first run if it doesn't exist. This allows the project to run immediately without manual dataset setup.

### 3. Compilation

Compile all Java files using the following command:

```bash
javac Main.java KMBAlgorithm.java TakahashiMatsuyama.java
```

This will generate `.class` files in the same directory.

### 4. Execution

Run the main class:

```bash
java Main
```

### 5. Expected Output

The program will output detailed results for both algorithms followed by a comparison table. Example output:

```
Running Steiner Tree Algorithms on: Prod_Data/synthetic_dataset.csv
====================================================

========== Kou-Markowsky-Berman (KMB) Algorithm ==========
Total Cost: 24.00
Runtime: 20 ms
Node Count: 5
Edge Count: 4
Nodes: [D, F, K, M, N]
Edges:
  F (Computer Networks) -> N (Distributed Systems) [w:4.00]
  K (System Security) -> M (Network Security) [w:2.00]
  D (Operating Systems) -> K (System Security) [w:3.00]
  F (Computer Networks) -> M (Network Security) [w:3.00]


========== Takahashi-Matsuyama (TM) Algorithm ==========
Total Cost: 24.00
Runtime: 5 ms
Node Count: 5
Edge Count: 4
Nodes: [D, F, K, M, N]
Edges:
  K (System Security) -> M (Network Security) [w:2.00]
  F (Computer Networks) -> N (Distributed Systems) [w:4.00]
  D (Operating Systems) -> K (System Security) [w:3.00]
  M (Network Security) -> F (Computer Networks) [w:3.00]


==================== ALGORITHM COMPARISON ====================
| Metric             | KMB Algorithm    | TM Algorithm     |
|--------------------|------------------|------------------|
| Total Cost         | 24.00            | 24.00            |
| Node Count         | 5                | 5                |
| Edge Count         | 4                | 4                |
| Runtime (ms)       | 20               | 5                |
|--------------------|------------------|------------------|

Cost Difference (TM - KMB): 0.00
Percentage Difference: 0.00%
============================================================
```

## 🛠️ Implementation Details

- **Graph Representation**: The graph is stored as an adjacency list using `Map<String, List<Edge>>`, allowing efficient neighbor lookups for pathfinding algorithms.

- **Undirected Graph**: Although the dataset describes directed relationships (from_node → to_node), the graph is treated as **undirected** by automatically adding a reverse edge for every edge read from the CSV. This is crucial for pathfinding algorithms like Dijkstra's, which need to traverse edges in both directions.

- **Metadata Handling**: All 15 columns from the CSV are parsed and stored in the `Edge` objects and a `NodeMetadata` map. This comprehensive data preservation makes the metadata available for potential future analysis, visualization, or extended algorithm features.

- **Union-Find**: Kruskal's MST implementation in the KMB algorithm uses a custom Union-Find (Disjoint Set Union) data structure with path compression for efficient cycle detection during MST construction.

- **Multi-source Dijkstra**: The TM algorithm implements a multi-source variant of Dijkstra's algorithm, initializing the priority queue with all nodes currently in the growing Steiner tree. This efficiently finds the closest unconnected terminal in a single pass.

- **Pruning Strategy**: Both algorithms use the same iterative pruning approach: repeatedly remove non-terminal nodes with degree 1 (leaf nodes) until no more can be removed. This ensures the final tree contains only necessary nodes while maintaining connectivity of all terminals.

