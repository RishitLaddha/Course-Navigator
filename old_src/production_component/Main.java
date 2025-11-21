import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Main executor class to run the Steiner Tree approximation algorithms
 * and compare their results.
 */
public class Main {

    public static void main(String[] args) {
        // The project structure assumes a 'Prod_Data' folder at the root.
        String csvPath = "Prod_Data/synthetic_dataset.csv";

        // Before running, let's create a dummy dataset if it doesn't exist.
        // This makes the project runnable out-of-the-box.
        createDummyDataset(csvPath);

        System.out.println("Running Steiner Tree Algorithms on: " + csvPath);
        System.out.println("====================================================\n");

        // --- Run Kou-Markowsky-Berman (KMB) Algorithm ---
        KMBAlgorithm.SteinerResult kmbResult = KMBAlgorithm.runKMB(csvPath);
        if (kmbResult != null) {
            printResult("Kou-Markowsky-Berman (KMB) Algorithm", kmbResult);
        } else {
            System.out.println("KMB Algorithm failed to run.");
        }

        System.out.println("\n");

        // --- Run Takahashi-Matsuyama (TM) Algorithm ---
        TakahashiMatsuyama.SteinerResult tmResult = TakahashiMatsuyama.runTM(csvPath);
        if (tmResult != null) {
            printResult("Takahashi-Matsuyama (TM) Algorithm", tmResult);
        } else {
            System.out.println("Takahashi-Matsuyama Algorithm failed to run.");
        }

        System.out.println("\n");

        // --- Print Comparison ---
        if (kmbResult != null && tmResult != null) {
            printComparison(kmbResult, tmResult);
        }
    }

    /**
     * Prints the results of a single algorithm run in a formatted way.
     * @param algorithmName The name of the algorithm.
     * @param result The SteinerResult object to print.
     */
    private static void printResult(String algorithmName, KMBAlgorithm.SteinerResult result) {
        System.out.println("========== " + algorithmName + " ==========");
        System.out.printf("Total Cost: %.2f\n", result.totalCost);
        System.out.println("Runtime: " + result.runtimeMs + " ms");
        System.out.println("Node Count: " + result.steinerNodes.size());
        System.out.println("Edge Count: " + result.steinerEdges.size());
        System.out.println("Nodes: " + result.steinerNodes);
        System.out.println("Edges:");
        result.steinerEdges.forEach(edge -> System.out.println("  " + edge));
    }

    // Overloaded method for TM result type
    private static void printResult(String algorithmName, TakahashiMatsuyama.SteinerResult result) {
        System.out.println("========== " + algorithmName + " ==========");
        System.out.printf("Total Cost: %.2f\n", result.totalCost);
        System.out.println("Runtime: " + result.runtimeMs + " ms");
        System.out.println("Node Count: " + result.steinerNodes.size());
        System.out.println("Edge Count: " + result.steinerEdges.size());
        System.out.println("Nodes: " + result.steinerNodes);
        System.out.println("Edges:");
        result.steinerEdges.forEach(edge -> System.out.println("  " + edge));
    }
    
    /**
     * Prints a comparison table of the two algorithm results.
     * @param kmb The result from the KMB algorithm.
     * @param tm The result from the TM algorithm.
     */
    private static void printComparison(KMBAlgorithm.SteinerResult kmb, TakahashiMatsuyama.SteinerResult tm) {
        double costDifference = tm.totalCost - kmb.totalCost;
        double percentageDiff = (kmb.totalCost == 0) ? 0 : (costDifference / kmb.totalCost) * 100;

        System.out.println("==================== ALGORITHM COMPARISON ====================");
        System.out.println("| Metric             | KMB Algorithm    | TM Algorithm     |");
        System.out.println("|--------------------|------------------|------------------|");
        System.out.printf("| Total Cost         | %-16.2f | %-16.2f |\n", kmb.totalCost, tm.totalCost);
        System.out.printf("| Node Count         | %-16d | %-16d |\n", kmb.steinerNodes.size(), tm.steinerNodes.size());
        System.out.printf("| Edge Count         | %-16d | %-16d |\n", kmb.steinerEdges.size(), tm.steinerEdges.size());
        System.out.printf("| Runtime (ms)       | %-16d | %-16d |\n", kmb.runtimeMs, tm.runtimeMs);
        System.out.println("|--------------------|------------------|------------------|");
        System.out.printf("\nCost Difference (TM - KMB): %.2f\n", costDifference);
        System.out.printf("Percentage Difference: %.2f%%\n", percentageDiff);
        System.out.println("============================================================");
    }
    
    /**
     * Creates a dummy CSV file if it doesn't exist, so the project
     * can be compiled and run immediately without manual setup.
     * @param path The full path for the CSV file.
     */
    private static void createDummyDataset(String path) {
        File file = new File(path);
        if (file.exists()) {
            return; // Don't overwrite existing data
        }

        // Ensure the parent directory exists
        File parentDir = file.getParentFile();
        if (parentDir != null) {
            parentDir.mkdirs();
        }
        
        // CSV Header (15 columns)
        String header = "from_node,from_name,to_node,to_name,edge_weight,is_terminal_from,is_terminal_to,from_difficulty,to_difficulty,from_category,to_category,edge_type,overlap_score,is_prerequisite_hard,estimated_hours";

        // Create a simple graph with some terminals
        // T1---A---B---T2
        //     |   |
        //     C---D
        // T3 is isolated but reachable from D
        String[] data = {
            "C101,Calc I,C102,Lin Alg,10,true,false,2,3,Math,Math,prerequisite,0.7,true,150",
            "C102,Lin Alg,C201,Data Struct,12,false,false,3,4,Math,CS,prerequisite,0.4,true,180",
            "C201,Data Struct,C202,Algorithms,8,false,true,4,5,CS,CS,prerequisite,0.9,true,200",
            "C101,Calc I,C301,Physics I,15,true,false,2,3,Math,Science,recommended,0.2,false,160",
            "C301,Physics I,C302,Physics II,11,false,false,3,4,Science,Science,prerequisite,0.8,true,160",
            "C201,Data Struct,C302,Physics II,25,false,false,4,4,CS,Science,alternative,0.1,false,170",
            "C302,Physics II,C401,Adv Topics,50,false,true,4,5,Science,Research,recommended,0.3,false,250"
        };

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(header + "\n");
            for (String row : data) {
                writer.write(row + "\n");
            }
            System.out.println("Created dummy dataset at: " + path);
        } catch (IOException e) {
            System.err.println("Could not create dummy dataset: " + e.getMessage());
        }
    }
}