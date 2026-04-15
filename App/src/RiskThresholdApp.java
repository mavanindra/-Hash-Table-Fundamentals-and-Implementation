import java.util.Arrays;

public class RiskThresholdApp {

    // ---------------- LINEAR SEARCH (unsorted)
    public static boolean linearSearch(int[] arr, int target) {
        int comparisons = 0;

        for (int val : arr) {
            comparisons++;
            if (val == target) {
                System.out.println("Linear Comparisons: " + comparisons);
                return true;
            }
        }

        System.out.println("Linear Comparisons: " + comparisons);
        return false;
    }

    // ---------------- BINARY SEARCH (insertion point)
    public static int findInsertionPoint(int[] arr, int target) {
        int low = 0, high = arr.length - 1;
        int comparisons = 0;

        while (low <= high) {
            int mid = (low + high) / 2;
            comparisons++;

            if (arr[mid] == target) {
                System.out.println("Binary Comparisons: " + comparisons);
                return mid;
            } else if (arr[mid] < target) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        System.out.println("Binary Comparisons: " + comparisons);
        return low; // insertion index
    }

    // ---------------- FLOOR (largest ≤ target)
    public static Integer floor(int[] arr, int target) {
        int low = 0, high = arr.length - 1;
        Integer result = null;

        while (low <= high) {
            int mid = (low + high) / 2;

            if (arr[mid] <= target) {
                result = arr[mid];
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return result;
    }

    // ---------------- CEILING (smallest ≥ target)
    public static Integer ceiling(int[] arr, int target) {
        int low = 0, high = arr.length - 1;
        Integer result = null;

        while (low <= high) {
            int mid = (low + high) / 2;

            if (arr[mid] >= target) {
                result = arr[mid];
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return result;
    }

    public static void main(String[] args) {

        int[] risks = {10, 25, 50, 100};
        int target = 30;

        // -------- LINEAR SEARCH
        boolean found = linearSearch(risks, target);

        System.out.println("\nLinear Search Result: " + found);

        // -------- BINARY SEARCH (sorted assumed)
        Arrays.sort(risks);

        int insertionIndex = findInsertionPoint(risks, target);

        Integer floorVal = floor(risks, target);
        Integer ceilVal = ceiling(risks, target);

        System.out.println("\nBinary Search:");
        System.out.println("Insertion Index: " + insertionIndex);
        System.out.println("Floor: " + floorVal);
        System.out.println("Ceiling: " + ceilVal);
    }
}