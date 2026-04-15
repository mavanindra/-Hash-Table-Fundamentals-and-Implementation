import java.util.*;


class Txn {
    String id;
    double fee;
    String timestamp;

    public Txn(String id, double fee, String timestamp) {
        this.id = id;
        this.fee = fee;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return id + ":" + fee + "@" + timestamp;
    }
}

public class TransactionSorter {

    // -------- Bubble Sort (by fee only) --------
    public static void bubbleSort(List<Txn> list) {
        int n = list.size();
        int passes = 0;
        int swaps = 0;
        boolean swapped;

        for (int i = 0; i < n - 1; i++) {
            swapped = false;
            passes++;

            for (int j = 0; j < n - i - 1; j++) {
                if (list.get(j).fee > list.get(j + 1).fee) {
                    // swap
                    Txn temp = list.get(j);
                    list.set(j, list.get(j + 1));
                    list.set(j + 1, temp);
                    swaps++;
                    swapped = true;
                }
            }

            // early stop
            if (!swapped) break;
        }

        System.out.println("Bubble Sort Result: " + list);
        System.out.println("Passes: " + passes + ", Swaps: " + swaps);
    }

    // -------- Insertion Sort (fee + timestamp) --------
    public static void insertionSort(List<Txn> list) {
        for (int i = 1; i < list.size(); i++) {
            Txn key = list.get(i);
            int j = i - 1;

            while (j >= 0 && compare(list.get(j), key) > 0) {
                list.set(j + 1, list.get(j)); // shift
                j--;
            }

            list.set(j + 1, key);
        }

        System.out.println("Insertion Sort Result: " + list);
    }

    // -------- Comparator --------
    private static int compare(Txn t1, Txn t2) {
        if (t1.fee != t2.fee) {
            return Double.compare(t1.fee, t2.fee);
        }
        return t1.timestamp.compareTo(t2.timestamp);
    }

    // -------- Outlier Detection --------
    public static void findOutliers(List<Txn> list) {
        System.out.print("High-fee outliers (>50): ");
        boolean found = false;

        for (Txn t : list) {
            if (t.fee > 50) {
                System.out.print(t + " ");
                found = true;
            }
        }

        if (!found) {
            System.out.print("None");
        }
        System.out.println();
    }

    // -------- Main --------
    public static void main(String[] args) {

        List<Txn> transactions = new ArrayList<>();

        // Sample input
        transactions.add(new Txn("id1", 10.5, "10:00"));
        transactions.add(new Txn("id2", 25.0, "09:30"));
        transactions.add(new Txn("id3", 5.0, "10:15"));

        // Copies for sorting
        List<Txn> bubbleList = new ArrayList<>(transactions);
        List<Txn> insertionList = new ArrayList<>(transactions);

        // Apply sorting rules
        if (bubbleList.size() <= 100) {
            bubbleSort(bubbleList);
        }

        if (insertionList.size() > 100 && insertionList.size() <= 1000) {
            insertionSort(insertionList);
        } else {
            // For demo (small input)
            insertionSort(insertionList);
        }

        // Outliers
        findOutliers(transactions);
    }
}
