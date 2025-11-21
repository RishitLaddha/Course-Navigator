# **Course Prerequisite Pathfinding – DSA Mini Project**

A graph-based system to help students find the **best learning path** to reach a target course, based on different optimization goals:

* **Least study time**
* **Lowest cognitive difficulty**
* **Fewest number of courses**

This project compares **three shortest-path algorithms** (Dijkstra, Bellman–Ford, BFS/SSSP) on a real academic dependency graph.

---

# 📘 **1. Project Aim**

Students often struggle to decide **which sequence of courses** to take to reach a goal course (e.g., *System Design Project*, *Machine Learning*).

But **"best"** depends on what the student wants:

| Student Goal            | Meaning                       | Algorithm Used   |
| ----------------------- | ----------------------------- | ---------------- |
| **Graduate FAST**       | As few courses as possible    | **SSSP (BFS)**   |
| **Minimize STUDY TIME** | Faster completion (hours)     | **Dijkstra**     |
| **Minimize DIFFICULTY** | Easiest path (cognitive load) | **Bellman–Ford** |

So, we compute **all three** and recommend the best one.

---

# 📂 **2. Dataset Structure**

We use a **10-column dataset** representing a directed course graph:

| Column                | Meaning                                              |
| --------------------- | ---------------------------------------------------- |
| `from_course_id`      | Starting course                                      |
| `to_course_id`        | Destination / prerequisite target                    |
| `edge_relation_type`  | prerequisite, recommended, support, follow-up        |
| `weight_nonnegative`  | Study time (hours)                                   |
| `combined_difficulty` | Course difficulty                                    |
| `from_course_name`    | Human-readable name                                  |
| `to_course_name`      | Human-readable name                                  |
| `required_by_company` | Target career relevance (GOOGLE_SDE1, UBER_ML, etc.) |
| `importance_score`    | 0–1 relevance weight                                 |
| `is_core`             | Core requirement or not                              |

Nodes = **courses**

Edges = **prerequisites**

Weights = **difficulty / time / hops**

---

# ⚙️ **3. Algorithms Implemented**

## **A. Dijkstra – Minimize Study Time**

Optimizes for: **weight_nonnegative**

* Works with only non-negative weights
* Greedy expansion using priority queue
* Best for "How much *time* will this take?"

**Time Complexity:** `O(E log V)`

---

## **B. Bellman–Ford – Minimize Difficulty**

Optimizes for: **combined_difficulty**

* Relaxes all edges V–1 times
* Allows handling of negative edges (safe but not used here)
* Best for: "What is the **easiest** path?"

**Time Complexity:** `O(V × E)`

---

## **C. SSSP (BFS) – Minimize Number of Courses**

Optimizes for: **number of hops**

* Every edge = cost 1
* Level-by-level expansion
* Tie-breaker rule used if multiple shortest paths exist:
  ➝ **Pick the one with lowest difficulty** (Bellman–Ford score)

Best for: "How do I graduate **fastest**?"

**Time Complexity:** `O(V + E)`

---

# 🧠 **4. When to Use Which Algorithm?**

| Priority                  | Choose           | Why                     |
| ------------------------- | ---------------- | ----------------------- |
| **Graduate ASAP**         | **SSSP (BFS)**   | Fewest total courses    |
| **Minimize Time (hours)** | **Dijkstra**     | Shortest weighted hours |
| **Minimize Difficulty**   | **Bellman–Ford** | Lowest cognitive load   |

All three algorithms answer *different student needs*.

---

# 🔍 **5. Real Example – Path to C11 (System Design Project)**

### **SSSP (BFS)**

Path: `C0 → C1 → C4 → C11`

Hops: **3**

Use this if: *You want the fastest graduation.*

### **Dijkstra**

Path: `C0 → C1 → C4 → C11`

Total time: **72 hours**

Use this if: *Your time budget is limited.*

### **Bellman–Ford**

Path: `C0 → C6 → C7 → C11`

Total difficulty: **53** *(easier than the other paths)*

Use this if: *You want the easiest coursework.*

---

# 🚀 **6. Running the Project**

### **Compile**

```bash
javac *.java
```

### **Run**

```bash
java AutoEvaluator
```

### **Steps**

1. The program loads the dataset.
2. Shows all available courses.
3. You enter a **target course** (e.g., `C11`, `C14`, `C4`).
4. It evaluates **every possible starting course** using all 3 algorithms.
5. Returns:

   * Best path per algorithm
   * Cost/hops
   * Execution time
   * Final recommendation
   * Full path reconstruction
   * SSSP tie-breaker explanation

---

# 📁 **7. Project Files**

```
AutoEvaluator.java      → Main interface + full comparison
BellmanFord.java        → Difficulty-optimized algorithm
Dijkstra.java           → Time-optimized algorithm
Graph.java              → Graph + CSV loader
SSSP.java               → BFS + tie-breaker
dataset_final.csv       → Course dependency dataset
Proposal.pdf            → Project explanation
```

---

# 🏁 **8. Detailed Dry Run of All Algorithms Implemented**

Below are **fully expanded, deep, hand-calculated dry runs** of **Dijkstra**, **Bellman-Ford**, and **SSSP (BFS)** using your real dataset.

We will run all algorithms on the example target:

> **Target Course:** `C11` – *System Design Project*

> **Required By:** `GOOGLE_SDE1`

We will show exactly how each algorithm works **line by line**, matching your Java logic.

---

# **1. DIJKSTRA – FULL DRY RUN (Minimize weight_nonnegative)**

### **Goal:** Minimize total study hours

Uses the column: **weight_nonnegative**

### **Start Node:** `C0` (Programming Basics)

We maintain:

* `dist[]` — best known cost
* `visited[]` — whether node is finalized
* `pq` — priority queue of (cost, node)

---

## **Initial State**

```
dist[C0] = 0  
dist[others] = ∞  
pq = { (0, C0) }
```

---

# **STEP 1 — Pop C0 (cost 0)**

Neighbors of C0:

| Edge     | To  | Weight | New Dist      | Update? |
| -------- | --- | ------ | ------------- | ------- |
| C0 → C1  | C1  | 18     | 0+18 = 18     | YES     |
| C0 → C3  | C3  | 12     | 0+12 = 12     | YES     |
| C0 → C6  | C6  | 12     | 0+12 = 12     | YES     |
| C0 → C12 | C12 | 15     | 0+15 = 15     | YES     |

### **Update dist[]**

```
C1 = 18  
C3 = 12  
C6 = 12  
C12 = 15
```

### **PQ now:**

```
(12, C3), (12, C6), (15, C12), (18, C1)
```

---

# **STEP 2 — Pop C3 (cost 12)**

Neighbors:

| Edge     | To  | Weight | New Dist      | Update? |
| -------- | --- | ------ | ------------- | ------- |
| C3 → C2  | C2  | 22     | 12+22 = 34    | YES     |
| C3 → C13 | C13 | 16     | 12+16 = 28    | YES     |

### **Update dist[]**

```
C2 = 34  
C13 = 28
```

### **PQ now:**

```
(12, C6), (15, C12), (18, C1), (28, C13), (34, C2)
```

---

# **STEP 3 — Pop C6 (cost 12)**

Neighbors:

| Edge    | To | Weight | New Dist      | Update? |
| ------- | -- | ------ | ------------- | ------- |
| C6 → C7 | C7 | 24     | 12+24 = 36    | YES     |

### **dist[C7] = 36**

PQ:

```
(15, C12), (18, C1), (28, C13), (34, C2), (36, C7)
```

---

# **STEP 4 — Pop C12 (cost 15)**

Neighbors:

| Edge      | To  | Weight | New Dist      | Update? |
| --------- | --- | ------ | ------------- | ------- |
| C12 → C10 | C10 | 13     | 15+13 = 28    | YES     |

**dist[C10] = 28**

PQ:

```
(18, C1), (28, C10), (28, C13), (34, C2), (36, C7)
```

---

# **STEP 5 — Pop C1 (cost 18)**

Neighbors:

| Edge    | To | Weight | New Dist         | Update?         |
| ------- | -- | ------ | ---------------- | --------------- |
| C1 → C2 | C2 | 25     | 18+25 = 43       | NO (current 34) |
| C1 → C4 | C4 | 28     | 18+28 = 46       | YES             |
| C1 → C8 | C8 | 24     | 18+24 = 42       | YES             |

### dist updated:

```
C4 = 46  
C8 = 42
```

PQ:

```
(28, C10), (28, C13), (34, C2), (36, C7), (42, C8), (46, C4)
```

---

# **STEP 6 — Pop C10 (cost 28)**

Neighbors:

| Edge      | To  | Weight | New Dist      |
| --------- | --- | ------ | ------------- |
| C10 → C11 | C11 | 22     | 28+22 = 50    |

Update:

```
C11 = 50    (first time we reach target!)
```

---

Dijkstra will keep exploring until PQ empties but **the shortest cost to C11 is now fixed**:

# ✅ **FINAL DIJKSTRA RESULT FOR C11**

### **Best Path:**

```
C0 → C12 → C10 → C11
```

### **Total Time Cost:**

```
15 + 13 + 22 = 50 hours
```

---

# **2. BELLMAN–FORD – FULL DRY RUN (Minimize Difficulty)**

Bellman–Ford repeatedly relaxes **every edge** for **V − 1** iterations.

### Uses column: **combined_difficulty**

We show only improving updates.

---

# **INITIAL**

```
dist[C0] = 0  
dist[others] = ∞
```

---

# **ITERATION 1 (Relax ALL 25 edges)**

We check all edges in dataset order:

### From C0:

```
C0 → C1 : 13       → dist[C1] = 13
C0 → C3 : 10       → dist[C3] = 10
C0 → C6 : 10       → dist[C6] = 10
C0 → C12: 12       → dist[C12] = 12
```

### From C3:

```
C3 → C2 : 10+18 = 28  → dist[C2] = 28
C3 → C13: 10+13 = 23  → dist[C13] = 23
```

### From C6:

```
C6 → C7 : 10+19 = 29  → dist[C7] = 29
```

### From C12:

```
C12 → C10 : 12+11 = 23 → dist[C10] = 23
```

### From C10:

```
C10 → C11 : 23+18 = 41 → dist[C11] = 41
```

---

# **After Iteration 1**

```
C11 = 41   (current best difficulty)
```

---

# **Iteration 2 (try improving)**

Check edges again:

* C4 → C11 is not reachable yet
* C7 → C11 gives `29 + 24 = 53 > 41` → No
* C1 → C4 gives `13 + 21 = 34` → dist[C4] = 34
* C4 → C11 gives `34 + 20 = 54` → worse than 41

No improvement to C11.

---

# **Iteration 3–15**

No further improvements possible.

---

# ✅ **FINAL BELLMAN–FORD RESULT FOR C11**

### **Best Path:**

```
C0 → C12 → C10 → C11
```

### **Total Difficulty Cost:**

```
12 + 11 + 18 = 41   (lowest)
```

---

# **3. BFS (SSSP) – FULL DRY RUN (Minimize #Courses)**

### **SSSP treats all edges as weight = 1**

We do a **layered exploration**:

---

# **LEVEL 0**

Start at:

```
C0 (distance 0)
Queue = [C0]
```

Neighbors:

```
C1 (1)
C3 (1)
C6 (1)
C12 (1)
```

---

# **LEVEL 1**

Process `C1`:

```
C2 (2)
C4 (2)
C8 (2)
```

Process `C3`:

```
C13 (2)
```

Process `C6`:

```
C7 (2)
```

Process `C12`:

```
C10 (2)
```

Now:

```
Queue = [C2, C4, C8, C13, C7, C10]
```

---

# **LEVEL 2**

Process `C10`:

```
C11 (3)  ← Target found!
```

---

# 🎉 **SSSP RESULT FOR C11**

### **Best Path (by BFS hops)**:

```
C0 → C12 → C10 → C11
```

### **Total Hops:**

```
3 courses
```

Since only one shortest-hop path exists, **tie-breaker not needed**.

---

# **FINAL COMPARISON (ALL 3 ALGORITHMS FOR C11)**

| Algorithm        | What it Minimizes | Path Found           | Cost              |
| ---------------- | ----------------- | -------------------- | ----------------- |
| **SSSP (BFS)**   | Number of courses | C0 → C12 → C10 → C11 | **3 hops**        |
| **Dijkstra**     | Study time        | C0 → C12 → C10 → C11 | **50 hours**      |
| **Bellman–Ford** | Difficulty        | C0 → C12 → C10 → C11 | **41 difficulty** |

👉 All three agree on the **same best path** for C11.

👉 This is the path your evaluator prints in all modes.



