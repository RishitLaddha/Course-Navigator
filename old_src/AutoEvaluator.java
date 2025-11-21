import java.util.*;

public class AutoEvaluator {

    public static void main(String[] args) throws Exception {
        
        Graph g = new Graph();
        g.loadCSV("dataset_final.csv");

        System.out.println("=".repeat(80));
        System.out.println("🎯 INTERACTIVE PATH FINDER - Choose Your Goal Course");
        System.out.println("=".repeat(80));
        
        Scanner sc = new Scanner(System.in);

        // Display available courses
        System.out.println("\nAvailable courses in the graph:");
        System.out.println("-".repeat(80));
        
        // Get unique course names from edges
        Map<String, String> courseNames = new HashMap<>();
        Map<String, String> courseCompanies = new HashMap<>();
        
        for (String node : g.nodes) {
            courseNames.put(node, node); // Default
            courseCompanies.put(node, "NONE");
        }
        
        // Extract course names and companies from edges
        for (String src : g.adj.keySet()) {
            for (Graph.Edge e : g.adj.get(src)) {
                courseNames.put(e.from, e.fromName);
                courseNames.put(e.to, e.toName);
                courseCompanies.put(e.to, e.requiredByCompany);
            }
        }
        
        // Display courses sorted
        List<String> sortedCourses = new ArrayList<>(g.nodes);
        Collections.sort(sortedCourses);
        
        for (String course : sortedCourses) {
            String company = courseCompanies.get(course);
            String companyDisplay = company.equals("NONE") ? "" : " [" + company + "]";
            System.out.printf("  %-5s : %-35s %s\n", course, courseNames.get(course), companyDisplay);
        }
        
        System.out.println("-".repeat(80));
        System.out.print("\n🎯 Enter your TARGET course ID (e.g., C11): ");
        String target = sc.nextLine().trim().toUpperCase();
        
        if (!g.nodes.contains(target)) {
            System.out.println("❌ Invalid course ID. Exiting.");
            return;
        }
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📊 FINDING BEST PATHS TO: " + target + " (" + courseNames.get(target) + ")");
        if (!courseCompanies.get(target).equals("NONE")) {
            System.out.println("🏢 Required by: " + courseCompanies.get(target));
        }
        System.out.println("=".repeat(80));
        
        // Run all algorithms from all sources
        System.out.println("\n⏳ Running all algorithms from all sources...\n");
        
        Map<String, Dijkstra.Result> dijkstraResults = new HashMap<>();
        Map<String, BellmanFord.Result> bellmanResults = new HashMap<>();
        Map<String, SSSP.Result> ssspResults = new HashMap<>();
        
        for (String src : g.nodes) {
            dijkstraResults.put(src, Dijkstra.run(g, src));
            bellmanResults.put(src, BellmanFord.run(g, src));
            ssspResults.put(src, SSSP.run(g, src));
        }
        
        // Find best source for each algorithm
        String bestSourceDijkstra = null;
        double bestDistDijkstra = Double.POSITIVE_INFINITY;
        
        String bestSourceBellman = null;
        double bestDistBellman = Double.POSITIVE_INFINITY;
        
        String bestSourceSSSP = null;
        int bestDistSSSP = Integer.MAX_VALUE;
        double bestSSSPDifficulty = Double.POSITIVE_INFINITY; // Tie-breaker
        int ssspTieCount = 0; // Count how many sources have same minimum hops
        
        for (String src : g.nodes) {
            if (!src.equals(target)) {
                // Dijkstra
                double dDist = dijkstraResults.get(src).dist.get(target);
                if (dDist < bestDistDijkstra) {
                    bestDistDijkstra = dDist;
                    bestSourceDijkstra = src;
                }
                
                // Bellman-Ford
                double bDist = bellmanResults.get(src).dist.get(target);
                if (bDist < bestDistBellman) {
                    bestDistBellman = bDist;
                    bestSourceBellman = src;
                }
                
                // SSSP with Bellman-Ford tie-breaker
                int sDist = ssspResults.get(src).dist.get(target);
                if (sDist < bestDistSSSP) {
                    // Found fewer hops
                    bestDistSSSP = sDist;
                    bestSourceSSSP = src;
                    bestSSSPDifficulty = bDist;
                    ssspTieCount = 1;
                } else if (sDist == bestDistSSSP && sDist != Integer.MAX_VALUE) {
                    // Same hops - count ties
                    ssspTieCount++;
                    // Use Bellman-Ford difficulty as tie-breaker
                    if (bDist < bestSSSPDifficulty) {
                        bestSourceSSSP = src;
                        bestSSSPDifficulty = bDist;
                    }
                }
            }
        }
        
        // Display results
        System.out.println("✅ Analysis Complete!\n");
        System.out.println("=".repeat(80));
        System.out.println("🏆 BEST STARTING COURSE FOR EACH ALGORITHM");
        System.out.println("=".repeat(80));
        
        // Dijkstra
        System.out.println("\n🥇 DIJKSTRA (Minimize: weight_nonnegative)");
        System.out.println("-".repeat(80));
        if (bestDistDijkstra != Double.POSITIVE_INFINITY) {
            System.out.println("Best Source: " + bestSourceDijkstra + " (" + courseNames.get(bestSourceDijkstra) + ")");
            System.out.println("Total Cost:  " + bestDistDijkstra);
            System.out.println("# of Hops:   " + ssspResults.get(bestSourceDijkstra).dist.get(target));
            System.out.println("Time Taken:  " + dijkstraResults.get(bestSourceDijkstra).timeMs + " ms");
            System.out.println("Use case:    Best for minimizing time/difficulty cost");
        } else {
            System.out.println("❌ Target unreachable from any source");
        }
        
        // Bellman-Ford
        System.out.println("\n🥈 BELLMAN-FORD (Minimize: combined_difficulty with overlap)");
        System.out.println("-".repeat(80));
        if (bestDistBellman != Double.POSITIVE_INFINITY) {
            System.out.println("Best Source: " + bestSourceBellman + " (" + courseNames.get(bestSourceBellman) + ")");
            System.out.println("Total Cost:  " + bestDistBellman);
            System.out.println("# of Hops:   " + ssspResults.get(bestSourceBellman).dist.get(target));
            System.out.println("Time Taken:  " + bellmanResults.get(bestSourceBellman).timeMs + " ms");
            System.out.println("Use case:    Best for maximizing learning efficiency (course synergy)");
        } else {
            System.out.println("❌ Target unreachable from any source");
        }
        
        // SSSP
        System.out.println("\n🥉 SSSP (BFS) (Minimize: number of courses)");
        System.out.println("-".repeat(80));
        if (bestDistSSSP != Integer.MAX_VALUE) {
            System.out.println("Best Source: " + bestSourceSSSP + " (" + courseNames.get(bestSourceSSSP) + ")");
            System.out.println("# of Hops:   " + bestDistSSSP + " courses");
            System.out.println("Time Taken:  " + ssspResults.get(bestSourceSSSP).timeMs + " ms ⚡");
            System.out.println("Use case:    Best for minimum prerequisite chain (fastest graduation)");
            System.out.println("\nDijkstra cost if using same path: " + dijkstraResults.get(bestSourceSSSP).dist.get(target));
            System.out.println("Bellman cost if using same path:  " + bellmanResults.get(bestSourceSSSP).dist.get(target));
        } else {
            System.out.println("❌ Target unreachable from any source");
        }
        
        // Comparison Table
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📊 COMPARISON TABLE");
        System.out.println("=".repeat(80));
        System.out.println("Algorithm     | Best Source | Cost/Hops | Time (ms) | Optimization Goal");
        System.out.println("--------------|-------------|-----------|-----------|------------------");
        System.out.printf("Dijkstra      | %-11s | %-9.1f | %-9d | Minimize difficulty\n", 
                          bestSourceDijkstra, bestDistDijkstra, dijkstraResults.get(bestSourceDijkstra).timeMs);
        System.out.printf("Bellman-Ford  | %-11s | %-9.1f | %-9d | Maximize efficiency\n", 
                          bestSourceBellman, bestDistBellman, bellmanResults.get(bestSourceBellman).timeMs);
        System.out.printf("SSSP (BFS)    | %-11s | %-9d | %-9d | Minimum courses ⭐\n", 
                          bestSourceSSSP, bestDistSSSP, ssspResults.get(bestSourceSSSP).timeMs);
        System.out.println("=".repeat(80));
        
        // Overall recommendation
        System.out.println("\n🎓 FINAL RECOMMENDATION - ALGORITHM RANKING:");
        System.out.println("=".repeat(80));
        
        if (bestDistSSSP != Integer.MAX_VALUE) {
        System.out.println("\n🥇 WINNER: SSSP (BFS) - Use This! ⭐");
        System.out.println("-".repeat(80));
        System.out.println("Best Source: " + bestSourceSSSP + " (" + courseNames.get(bestSourceSSSP) + ")");
        System.out.println("Why #1?");
        System.out.println("  ✓ MINIMUM COURSES: Only " + bestDistSSSP + " course(s) to reach " + target);
        if (ssspTieCount > 1) {
            System.out.println("  ✓ Tie-breaker: " + ssspTieCount + " sources had " + bestDistSSSP + 
                               " hop(s), chose lowest difficulty (" + String.format("%.1f", bestSSSPDifficulty) + ")");
        }
        System.out.println("  ✓ Fastest graduation path");
        System.out.println("  ✓ Execution time: " + ssspResults.get(bestSourceSSSP).timeMs + " ms (instant!)");
        System.out.println("  ✓ Best for: \"I want to graduate QUICKLY with minimum prerequisites\"");
            
            System.out.println("\n🥈 RUNNER-UP: Bellman-Ford - Use If You Want Easier Path");
            System.out.println("-".repeat(80));
            System.out.println("Best Source: " + bestSourceBellman + " (" + courseNames.get(bestSourceBellman) + ")");
            System.out.println("Why #2?");
            System.out.println("  • MINIMUM DIFFICULTY: Cost = " + bestDistBellman);
            System.out.println("  • More courses but EASIER overall (considers overlap/synergy)");
            System.out.println("  • Courses needed: " + ssspResults.get(bestSourceBellman).dist.get(target) + " course(s)");
            System.out.println("  • Execution time: " + bellmanResults.get(bestSourceBellman).timeMs + " ms");
            System.out.println("  • Best for: \"I want the EASIEST path, even if more courses\"");
            
            System.out.println("\n🥉 THIRD PLACE: Dijkstra - Standard Approach");
            System.out.println("-".repeat(80));
            System.out.println("Best Source: " + bestSourceDijkstra + " (" + courseNames.get(bestSourceDijkstra) + ")");
            System.out.println("Why #3?");
            System.out.println("  • Cost = " + bestDistDijkstra + " (higher than Bellman-Ford)");
            System.out.println("  • Doesn't account for overlap savings");
            System.out.println("  • Courses needed: " + ssspResults.get(bestSourceDijkstra).dist.get(target) + " course(s)");
            System.out.println("  • Execution time: " + dijkstraResults.get(bestSourceDijkstra).timeMs + " ms (faster than Bellman)");
            System.out.println("  • Best for: \"Standard weighted shortest path (no special considerations)\"");
            
            System.out.println("\n" + "=".repeat(80));
            System.out.println("💡 SUMMARY:");
            System.out.println("=".repeat(80));
            System.out.println("  🏆 Use SSSP if goal = Minimum courses (" + bestDistSSSP + " courses)");
            System.out.println("  📚 Use Bellman-Ford if goal = Easiest path (cost: " + bestDistBellman + ")");
            System.out.println("  ⚡ Use Dijkstra if goal = Standard approach (cost: " + bestDistDijkstra + ")");
            System.out.println();
            System.out.println("  SSSP is RECOMMENDED because it minimizes prerequisites!");
        }
        
        System.out.println("=".repeat(80));
        
        // Show all reachable sources ranked
        System.out.println("\n📋 ALL SOURCES RANKED BY SSSP (Minimum Courses):");
        System.out.println("=".repeat(80));
        
        List<Map.Entry<String, Integer>> ranked = new ArrayList<>();
        for (String src : g.nodes) {
            if (!src.equals(target)) {
                int dist = ssspResults.get(src).dist.get(target);
                if (dist != Integer.MAX_VALUE) {
                    ranked.add(new AbstractMap.SimpleEntry<>(src, dist));
                }
            }
        }
        
        ranked.sort(Map.Entry.comparingByValue());
        
        System.out.println("Rank | Source | Course Name                  | Hops | Dijkstra Cost | Bellman Cost");
        System.out.println("-----|--------|------------------------------|------|---------------|-------------");
        
        int rank = 1;
        for (Map.Entry<String, Integer> entry : ranked) {
            String src = entry.getKey();
            int hops = entry.getValue();
            double dCost = dijkstraResults.get(src).dist.get(target);
            double bCost = bellmanResults.get(src).dist.get(target);
            
            System.out.printf("%-4d | %-6s | %-28s | %-4d | %-13.1f | %-12.1f\n",
                              rank++, src, courseNames.get(src), hops, dCost, bCost);
        }
        
        System.out.println("=".repeat(80));
        
        // Target company info
        if (!courseCompanies.get(target).equals("NONE")) {
            System.out.println("\n💼 TARGET COMPANY: " + courseCompanies.get(target));
            System.out.println("This course is required for career goal: " + courseCompanies.get(target).replace("_", " "));
        }
        
        System.out.println("\n✅ Analysis Complete!");
    }
}

