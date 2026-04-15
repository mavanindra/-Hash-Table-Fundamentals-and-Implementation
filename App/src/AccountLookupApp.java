import java.util.Arrays;

public class AccountLookupApp {

    // ---------------- LINEAR SEARCH (first + last)
    public static int[] linearSearch(String[] arr, String target) {
        int first = -1, last = -1;
        int comparisons = 0;

        for (int i = 0; i < arr.length; i++) {
            comparisons++;

            if (arr[i].equals(target)) {
                if (first == -1) first = i;
                last = i;
            }
        }

        System.out.println("Linear Search Comparisons: " + comparisons);
        return new int[]{first, last};
    }

    // ---------------- BINARY SEARCH (any one index)
    public static int binarySearch(String[] arr, String target) {
        int low = 0, high = arr.length - 1;
        int comparisons = 0;

        while (low <= high) {
            int mid = (low + high) / 2;
            comparisons++;

            int cmp = target.compareTo(arr[mid]);

            if (cmp == 0) {
                System.out.println("Binary Search Comparisons: " + comparisons);
                return mid;
            } else if (cmp < 0) {
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }

        System.out.println("Binary Search Comparisons: " + comparisons);
        return -1;
    }

    // ---------------- COUNT OCCURRENCES (binary based)
    public static int countOccurrences(String[] arr, String target) {
        int count = 0;

        for (String s : arr) {
            if (s.equals(target)) count++;
        }
        return count;
    }

    public static void main(String[] args) {

        String[] logs = {"accB", "accA", "accB", "accC"};

        // -------- LINEAR SEARCH
        int[] result = linearSearch(logs, "accB");

        System.out.println("\nLinear Search:");
        System.out.println("First occurrence index: " + result[0]);
        System.out.println("Last occurrence index: " + result[1]);

        // -------- BINARY SEARCH (requires sorting)
        Arrays.sort(logs);

        System.out.println("\nSorted Logs:");
        System.out.println(Arrays.toString(logs));

        int index = binarySearch(logs, "accB");
        int count = countOccurrences(logs, "accB");

        System.out.println("\nBinary Search:");
        System.out.println("Found at index: " + index);
        System.out.println("Count: " + count);
    }
}