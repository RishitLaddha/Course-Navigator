================================================================================
    COURSE PREREQUISITE PATHFINDING - DSA MINI PROJECT
================================================================================

--------------------------------------------------------------------------------
1. PROJECT AIM
--------------------------------------------------------------------------------

GOAL: Find the optimal learning path from any starting course to a target course
      in a university curriculum graph.

PROBLEM: Students need to choose the best sequence of courses to reach their 
         goal course, but "best" can mean different things:
         
         - Shortest TIME to completion (minimize total study hours)
         - Minimum DIFFICULTY (minimize cognitive load)
         - Fewest NUMBER OF COURSES (fastest graduation)

SOLUTION: We implement 3 different shortest-path algorithms, each optimizing
          for a different metric, and compare their results.

DATA MODEL:
- Nodes = Courses (C0, C1, C2, ..., C15)
- Edges = Prerequisites/Dependencies between courses
- Edge Weights = Different metrics (time, difficulty, hops)


--------------------------------------------------------------------------------
2. DIJKSTRA'S ALGORITHM - MINIMIZING STUDY TIME
--------------------------------------------------------------------------------

WHAT IT DOES:
Finds the path with minimum total "weight_nonnegative" (study hours required).
Uses a greedy approach with a priority queue to always expand the cheapest path.

CONSTRAINTS:
- Only works with NON-NEGATIVE weights
- Optimized for sparse graphs
- Time Complexity: O(E log V)

--------------------------------------------------------------------------------
DRY RUN: Finding shortest time path from C0 → C11 (System Design Project)
--------------------------------------------------------------------------------

Initial State:
  dist[C0] = 0
  dist[all others] = ∞
  Priority Queue: [C0]

ITERATION 1: Process C0 (current dist = 0)
  Explore edges from C0:
    → C1: weight = 18  →  dist[C1] = 0 + 18 = 18  ✓ UPDATE
    → C3: weight = 12  →  dist[C3] = 0 + 12 = 12  ✓ UPDATE
    → C6: weight = 12  →  dist[C6] = 0 + 12 = 12  ✓ UPDATE
    → C12: weight = 15 →  dist[C12] = 0 + 15 = 15 ✓ UPDATE
  
  Updated distances: C0=0, C1=18, C3=12, C6=12, C12=15
  Priority Queue: [C3(12), C6(12), C12(15), C1(18)]

ITERATION 2: Process C3 (current dist = 12)
  Explore edges from C3:
    → C2: weight = 22  →  dist[C2] = 12 + 22 = 34  ✓ UPDATE
    → C13: weight = 16 →  dist[C13] = 12 + 16 = 28 ✓ UPDATE
  
  Updated distances: C2=34, C13=28
  Priority Queue: [C6(12), C12(15), C1(18), C13(28), C2(34)]

ITERATION 3: Process C6 (current dist = 12)
  Explore edges from C6:
    → C7: weight = 24  →  dist[C7] = 12 + 24 = 36  ✓ UPDATE
  
  Updated distances: C7=36
  Priority Queue: [C12(15), C1(18), C13(28), C2(34), C7(36)]

ITERATION 4: Process C1 (current dist = 18)
  Explore edges from C1:
    → C2: weight = 25  →  new_dist = 18 + 25 = 43 > 34 ✗ SKIP
    → C4: weight = 28  →  dist[C4] = 18 + 28 = 46  ✓ UPDATE
    → C8: weight = 24  →  dist[C8] = 18 + 24 = 42  ✓ UPDATE
  
  Updated distances: C4=46, C8=42
  Priority Queue: [C13(28), C2(34), C7(36), C8(42), C4(46)]

[... continues for remaining nodes ...]

FINAL PATH to C11:
  Best path: C0 → C1 → C4 → C11
  Total weight_nonnegative: 18 + 28 + 26 = 72 hours
  
  Path breakdown:
    C0 (Programming Basics) 
      → [18 hrs] → 
    C1 (Data Structures Intro) 
      → [28 hrs] → 
    C4 (Advanced Data Structures) 
      → [26 hrs] → 
    C11 (System Design Project)


--------------------------------------------------------------------------------
3. BELLMAN-FORD ALGORITHM - MINIMIZING DIFFICULTY
--------------------------------------------------------------------------------

WHAT IT DOES:
Finds the path with minimum total "combined_difficulty" (cognitive load).
Relaxes all edges repeatedly to handle cases where difficulty might vary.

CONSTRAINTS:
- Can handle negative weights (though our dataset has none)
- Slower than Dijkstra: O(V × E)
- More thorough: checks all edges V-1 times

--------------------------------------------------------------------------------
DRY RUN: Finding lowest difficulty path from C0 → C11 (System Design Project)
--------------------------------------------------------------------------------

Initial State:
  dist[C0] = 0
  dist[all others] = ∞
  
ROUND 1: Relax all edges once
  Edge C0→C1: dist[C1] = min(∞, 0 + 13) = 13  ✓
  Edge C1→C2: dist[C2] = min(∞, 13 + 17) = 30  ✓
  Edge C0→C3: dist[C3] = min(∞, 0 + 10) = 10  ✓
  Edge C3→C2: dist[C2] = min(30, 10 + 18) = 28  ✓ IMPROVED
  Edge C1→C4: dist[C4] = min(∞, 13 + 21) = 34  ✓
  Edge C2→C4: dist[C4] = min(34, 28 + 22) = 34  (no change)
  Edge C4→C5: dist[C5] = min(∞, 34 + 19) = 53  ✓
  Edge C0→C6: dist[C6] = min(∞, 0 + 10) = 10  ✓
  Edge C6→C7: dist[C7] = min(∞, 10 + 19) = 29  ✓
  Edge C2→C7: dist[C7] = min(29, 28 + 22) = 29  (no change)
  Edge C1→C8: dist[C8] = min(∞, 13 + 21) = 34  ✓
  Edge C2→C9: dist[C9] = min(∞, 28 + 19) = 47  ✓
  Edge C9→C10: dist[C10] = min(∞, 47 + 16) = 63  ✓
  Edge C8→C10: dist[C10] = min(63, 34 + 20) = 54  ✓ IMPROVED
  Edge C7→C11: dist[C11] = min(∞, 29 + 24) = 53  ✓
  Edge C4→C11: dist[C11] = min(53, 34 + 20) = 53  (no change)
  Edge C10→C11: dist[C11] = min(53, 54 + 18) = 53  (no change)
  
  After Round 1: dist[C11] = 53

ROUND 2: Relax all edges again (checking for improvements)
  Most edges show no improvement...
  Key observation: C0→C1→C4→C11 gives:
    combined_difficulty = 13 + 21 + 20 = 54
  But C0→C6→C7→C11 gives:
    combined_difficulty = 10 + 19 + 24 = 53  ✓ BETTER

ROUND 3, 4, ..., V-1: No further improvements found

FINAL PATH to C11:
  Best path: C0 → C6 → C7 → C11
  Total combined_difficulty: 10 + 19 + 24 = 53
  
  Path breakdown:
    C0 (Programming Basics) 
      → [difficulty: 10] → 
    C6 (OOP in Java) 
      → [difficulty: 19] → 
    C7 (Software Engineering) 
      → [difficulty: 24] → 
    C11 (System Design Project)


--------------------------------------------------------------------------------
4. SSSP (BFS) ALGORITHM - MINIMIZING NUMBER OF COURSES
--------------------------------------------------------------------------------

WHAT IT DOES:
Finds the path with the FEWEST number of courses (minimum hops).
Uses Breadth-First Search (BFS) to explore layer by layer.

CONSTRAINTS:
- Treats all edges as having equal weight (1 hop each)
- Guarantees shortest path in terms of number of edges
- Time Complexity: O(V + E)

--------------------------------------------------------------------------------
DRY RUN: Finding path with fewest courses from C0 → C11 (System Design Project)
--------------------------------------------------------------------------------

Initial State:
  dist[C0] = 0 hops
  dist[all others] = ∞
  Queue: [C0]

LEVEL 0: Process C0 (distance = 0)
  Neighbors of C0:
    → C1: dist[C1] = 0 + 1 = 1  ✓ UPDATE, add to queue
    → C3: dist[C3] = 0 + 1 = 1  ✓ UPDATE, add to queue
    → C6: dist[C6] = 0 + 1 = 1  ✓ UPDATE, add to queue
    → C12: dist[C12] = 0 + 1 = 1  ✓ UPDATE, add to queue
  
  Queue: [C1, C3, C6, C12]
  Distances: C1=1, C3=1, C6=1, C12=1

LEVEL 1: Process C1 (distance = 1)
  Neighbors of C1:
    → C2: dist[C2] = 1 + 1 = 2  ✓ UPDATE, add to queue
    → C4: dist[C4] = 1 + 1 = 2  ✓ UPDATE, add to queue
    → C8: dist[C8] = 1 + 1 = 2  ✓ UPDATE, add to queue
  
  Queue: [C3, C6, C12, C2, C4, C8]

LEVEL 1: Process C3 (distance = 1)
  Neighbors of C3:
    → C2: dist[C2] = 2 (already found) ✗ SKIP
    → C13: dist[C13] = 1 + 1 = 2  ✓ UPDATE, add to queue
  
  Queue: [C6, C12, C2, C4, C8, C13]

LEVEL 1: Process C6 (distance = 1)
  Neighbors of C6:
    → C7: dist[C7] = 1 + 1 = 2  ✓ UPDATE, add to queue
  
  Queue: [C12, C2, C4, C8, C13, C7]

LEVEL 2: Process C4 (distance = 2)
  Neighbors of C4:
    → C5: dist[C5] = 2 + 1 = 3  ✓ UPDATE
    → C11: dist[C11] = 2 + 1 = 3  ✓ UPDATE ← TARGET FOUND!
    → C15: dist[C15] = 2 + 1 = 3  ✓ UPDATE
  
  Queue: [C2, C8, C13, C7, C5, C11, C15]

BFS continues until queue is empty, but C11 is already found at distance 3.

FINAL PATH to C11:
  Best path: C0 → C1 → C4 → C11
  Total hops: 3 courses
  
  Path breakdown:
    C0 (Programming Basics) 
      → [1 hop] → 
    C1 (Data Structures Intro) 
      → [1 hop] → 
    C4 (Advanced Data Structures) 
      → [1 hop] → 
    C11 (System Design Project)


--------------------------------------------------------------------------------
5. WHEN TO USE WHICH ALGORITHM?
--------------------------------------------------------------------------------

┌─────────────────────┬──────────────────┬──────────────────┬──────────────────┐
│   YOUR PRIORITY     │   USE ALGORITHM  │  OPTIMIZES FOR   │   BEST WHEN...   │
├─────────────────────┼──────────────────┼──────────────────┼──────────────────┤
│ Graduate FAST       │  SSSP (BFS)      │ Fewest courses   │ - Time-sensitive │
│ (minimum courses)   │                  │ (min hops)       │ - Need degree    │
│                     │                  │                  │   quickly        │
│                     │                  │                  │ - Don't care     │
│                     │                  │                  │   about load     │
├─────────────────────┼──────────────────┼──────────────────┼──────────────────┤
│ Minimize STUDY TIME │  DIJKSTRA        │ Total hours      │ - Busy schedule  │
│ (total hours)       │                  │ (weight_        │ - Working part-  │
│                     │                  │  nonnegative)    │   time           │
│                     │                  │                  │ - Want efficient │
│                     │                  │                  │   time usage     │
├─────────────────────┼──────────────────┼──────────────────┼──────────────────┤
│ Minimize DIFFICULTY │  BELLMAN-FORD    │ Cognitive load   │ - Struggling     │
│ (cognitive load)    │                  │ (combined_       │   student        │
│                     │                  │  difficulty)     │ - Want easier    │
│                     │                  │                  │   path           │
│                     │                  │                  │ - Better grades  │
│                     │                  │                  │   more important │
└─────────────────────┴──────────────────┴──────────────────┴──────────────────┘


REAL EXAMPLE - Path from C0 to C11:

╔════════════════════════════════════════════════════════════════════════════╗
║                         ALGORITHM COMPARISON                               ║
╚════════════════════════════════════════════════════════════════════════════╝

SSSP (BFS):
  Path: C0 → C1 → C4 → C11
  Courses: 3 hops (FASTEST GRADUATION) ✓
  Study Time: 72 hours
  Difficulty: 54
  → Choose if: You need the degree ASAP

DIJKSTRA:
  Path: C0 → C1 → C4 → C11
  Courses: 3 hops
  Study Time: 72 hours (LEAST TIME) ✓
  Difficulty: 54
  → Choose if: You're working part-time and have limited study hours

BELLMAN-FORD:
  Path: C0 → C6 → C7 → C11
  Courses: 3 hops
  Study Time: 77 hours
  Difficulty: 53 (EASIEST PATH) ✓
  → Choose if: You want to maintain high GPA / avoid burnout


KEY INSIGHTS:

1. SSSP and Dijkstra found the SAME PATH in this case
   (C0→C1→C4→C11) because the fastest path also happens to have good time.

2. Bellman-Ford found a DIFFERENT PATH (C0→C6→C7→C11) that trades 
   5 extra hours for 1 point less difficulty - better for learning.

3. The "best" algorithm depends on YOUR constraints:
   - Limited time? → Dijkstra
   - Want easy courses? → Bellman-Ford
   - Need to graduate fast? → SSSP

4. All three algorithms are valuable because they answer different questions!


--------------------------------------------------------------------------------
HOW TO RUN
--------------------------------------------------------------------------------

1. Compile all files:
   javac *.java

2. Run the main evaluator:
   java AutoEvaluator

3. Enter target course when prompted (e.g., C11, C14, C4)

4. View comparison of all three algorithms with recommended path


================================================================================
                            END OF README
================================================================================

