import java.util.Arrays;

public class TransportationProblemSolver {
    public static void main(String[] args) {
        //Define supply and demand (Example 1)
        int[] supply = {20, 30, 25};
        int[] demand = {10, 25, 25, 15};
        
        //Define cost matrix (Example 1)
        int[][] cost = {
            {8, 6, 10, 9},
            {9, 7, 4, 2},
            {3, 4, 2, 5}
        };

        // // Define supply and demand (Example 2)
        // int[] supply = {160, 140, 170};
        // int[] demand = {120, 50, 190, 110};
        
        // // Define cost matrix (Example 2)
        // int[][] cost = {
        //     {7, 8, 1, 2},
        //     {4, 5, 9, 8},
        //     {9, 2, 3, 6}
        // };

        // Define supply and demand (Example 3)
        // int[] supply = {40, 180, 160};
        // int[] demand = {60, 70, 130, 130};
        
        // Define cost matrix (Example 3)
        // int[][] cost = {
        //     {6, 5, 3, 2},
        //     {8, 4, 2, 1},
        //     {3, 9, 3, 7}
        // };
    
        // Check if the problem is balanced
        int totalSupply = Arrays.stream(supply).sum();
        int totalDemand = Arrays.stream(demand).sum();
    
        if (totalSupply != totalDemand) {
            System.out.println("The problem is not balanced!");
            return;
        }

        // Print the unified matrix with cost, supply, and demand
        printUnifiedMatrix(cost, supply, demand);
    
        // North-West Corner Method
        int[][] nwCornerSolution = northWestCornerMethod(supply.clone(), demand.clone());
        System.out.println("Initial Basic Feasible Solution (North-West Corner Method):");
        printSolution(nwCornerSolution);
        int totalCorner = totalCost(nwCornerSolution, cost);
        System.out.println("Total Cost: " + totalCorner);
        System.out.println("-------------------------------------------------");
    
        // Vogel's Approximation Method
        int[][] vogelSolution = vogelsApproximationMethod(supply.clone(), demand.clone(), cost);
        System.out.println("Initial Basic Feasible Solution (Vogel's Approximation Method):");
        printSolution(vogelSolution);
        int totalVogel = totalCost(vogelSolution, cost);
        System.out.println("Total Cost: " + totalVogel);
        System.out.println("-------------------------------------------------");
    
        // Russell's Approximation Method
        int[][] russellSolution = russellsApproximationMethod(supply.clone(), demand.clone(), cost);
        System.out.println("Initial Basic Feasible Solution (Russell's Approximation Method):");
        printSolution(russellSolution);
        int totalRussell = totalCost(russellSolution, cost);
        System.out.println("Total Cost: " + totalRussell);
        System.out.println("-------------------------------------------------");
    }

    private static void printUnifiedMatrix(int[][] cost, int[] supply, int[] demand) {
        System.out.println("Unified Matrix (cost with supply and demand):");
        int rows = cost.length;
        int cols = cost[0].length;
        
        // Print the cost matrix with demand as the last row
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.printf("%-5d", cost[i][j]);
            }
            System.out.printf("| %-5d\n", supply[i]);  // Print supply at the end of each row
        }
        
        // Print the demand row
        for (int j = 0; j < cols; j++) {
            System.out.printf("-----");
        }
        System.out.println();
    
        for (int j = 0; j < cols; j++) {
            System.out.printf("%-5d", demand[j]);
        }
        System.out.println();
    }
    
    private static int[][] northWestCornerMethod(int[] supply, int[] demand) {
        int rows = supply.length;
        int cols = demand.length;
        int[][] solution = new int[rows][cols];
    
        int i = 0, j = 0;
        while (i < rows && j < cols) {
            int allocation = Math.min(supply[i], demand[j]);
            solution[i][j] = allocation;
            supply[i] -= allocation;
            demand[j] -= allocation;
    
            if (supply[i] == 0) {
                i++;
            }
    
            if (demand[j] == 0) {
                j++;
            }
        }
        return solution;
    }
    
    private static int[][] vogelsApproximationMethod(int[] supply, int[] demand, int[][] cost) {
        int rows = supply.length;
        int cols = demand.length;
        int[][] solution = new int[rows][cols];
        boolean[] rowDone = new boolean[rows];
        boolean[] colDone = new boolean[cols];
    
        while (Arrays.stream(supply).sum() > 0 && Arrays.stream(demand).sum() > 0) {
            int[] rowPenalties = new int[rows];
            int[] colPenalties = new int[cols];
    
            // Calculate row penalties
            for (int i = 0; i < rows; i++) {
                if (!rowDone[i]) {
                    int[] sortedCosts = Arrays.stream(cost[i]).filter(c -> c != Integer.MAX_VALUE).sorted().toArray();
                    if (sortedCosts.length > 1) {
                        rowPenalties[i] = sortedCosts[1] - sortedCosts[0];
                    }
                }
            }
    
            // Calculate column penalties
            for (int j = 0; j < cols; j++) {
                if (!colDone[j]) {
                    int[] colCosts = new int[rows];
                    for (int i = 0; i < rows; i++) {
                        colCosts[i] = cost[i][j];
                    }
                    int[] sortedCosts = Arrays.stream(colCosts).filter(c -> c != Integer.MAX_VALUE).sorted().toArray();
                    if (sortedCosts.length > 1) {
                        colPenalties[j] = sortedCosts[1] - sortedCosts[0];
                    }
                }
            }
    
            // Find the maximum penalty
            int maxRowPenalty = Arrays.stream(rowPenalties).max().orElse(-1);
            int maxColPenalty = Arrays.stream(colPenalties).max().orElse(-1);
    
            if (maxRowPenalty >= maxColPenalty) {
                // Allocate in the row with the maximum penalty
                int row = findIndex(rowPenalties, maxRowPenalty);
                int minCostIndex = findMinCostIndex(cost[row], colDone);
                int allocation = Math.min(supply[row], demand[minCostIndex]);
                solution[row][minCostIndex] = allocation;
                supply[row] -= allocation;
                demand[minCostIndex] -= allocation;
    
                if (supply[row] == 0) {
                    rowDone[row] = true;
                }
                if (demand[minCostIndex] == 0) {
                    colDone[minCostIndex] = true;
                }
            } else {
                // Allocate in the column with the maximum penalty
                int col = findIndex(colPenalties, maxColPenalty);
                int minCostIndex = findMinCostIndex(getColumn(cost, col), rowDone);
                int allocation = Math.min(supply[minCostIndex], demand[col]);
                solution[minCostIndex][col] = allocation;
                supply[minCostIndex] -= allocation;
                demand[col] -= allocation;
    
                if (supply[minCostIndex] == 0) {
                    rowDone[minCostIndex] = true;
                }
                if (demand[col] == 0) {
                    colDone[col] = true;
                }
            }
        }
        
        return solution;
    }
    







    
    private static int[][] russellsApproximationMethod(int[] supply, int[] demand, int[][] cost) {
        int m = supply.length;
        int n = demand.length;
        int[][] solution = new int[m][n];
        int[] supplyCopy = supply.clone();
        int[] demandCopy = demand.clone();

        while (sum(supplyCopy) > 0) {
            // Calculate u_i and v_j
            int[] u = new int[m];
            int[] v = new int[n];

            for (int i = 0; i < m; i++) {
                u[i] = Integer.MIN_VALUE;
                for (int j = 0; j < n; j++) {
                    u[i] = Math.max(u[i], cost[i][j]);
                }
            }

            for (int j = 0; j < n; j++) {
                v[j] = Integer.MIN_VALUE;
                for (int i = 0; i < m; i++) {
                    v[j] = Math.max(v[j], cost[i][j]);
                }
            }

            // Find cell with minimum delta
            int minDelta = Integer.MAX_VALUE;
            int minI = 0, minJ = 0;

            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    if (supplyCopy[i] > 0 && demandCopy[j] > 0) {
                        int delta = cost[i][j] - u[i] - v[j];
                        if (delta < minDelta) {
                            minDelta = delta;
                            minI = i;
                            minJ = j;
                        }
                    }
                }
            }

            // Allocate
            int quantity = Math.min(supplyCopy[minI], demandCopy[minJ]);
            solution[minI][minJ] = quantity;
            supplyCopy[minI] -= quantity;
            demandCopy[minJ] -= quantity;
        }
        return solution;
    }

    private static int sum(int[] array) {
        int sum = 0;
        for (int i : array) {
            sum += i;
        }
        return sum;
    }
    
    private static int findIndex(int[] array, int value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return -1;
    }
    
    private static int findMinCostIndex(int[] costs, boolean[] done) {
        int minCost = Integer.MAX_VALUE;
        int index = -1;
        for (int i = 0; i < costs.length; i++) {
            if (!done[i] && costs[i] < minCost) {
                minCost = costs[i];
                index = i;
            }
        }
        return index;
    }
    
    private static int[] getColumn(int[][] matrix, int col) {
        int[] column = new int[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            column[i] = matrix[i][col];
        }
        return column;
    }
    
    private static void printSolution(int[][] solution) {
        for (int[] row : solution) {
            for (int value : row) {
                System.out.printf("%-10d", value); // Print each allocation with formatting
            }
            System.out.println(); // Move to the next line after each row
        }
        System.out.println("-------------------------------------------------");
    }

    private static int totalCost(int[][] solution, int[][] cost) {
        int total = 0;
        for (int i = 0; i < solution.length; i++) {
            for (int j = 0; j < solution[i].length; j++) {
                total += solution[i][j] * cost[i][j];
            }
        }
        return total;
    }
  
}
