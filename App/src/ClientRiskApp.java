import java.util.Arrays;

public class ClientRiskApp {

    // Client model
    static class Client {
        String name;
        int riskScore;
        double accountBalance;

        Client(String name, int riskScore, double accountBalance) {
            this.name = name;
            this.riskScore = riskScore;
            this.accountBalance = accountBalance;
        }

        @Override
        public String toString() {
            return name + ":" + riskScore;
        }
    }

    // ---------------- BUBBLE SORT (ASC by riskScore)
    public static void bubbleSort(Client[] arr) {
        int n = arr.length;
        int swaps = 0;

        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;

            for (int j = 0; j < n - i - 1; j++) {
                if (arr[j].riskScore > arr[j + 1].riskScore) {
                    // swap
                    Client temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;

                    swaps++;
                    swapped = true;
                }
            }

            if (!swapped) break;
        }

        System.out.println("Bubble Sort Swaps: " + swaps);
    }

    // ---------------- INSERTION SORT (DESC by riskScore + accountBalance)
    public static void insertionSort(Client[] arr) {

        for (int i = 1; i < arr.length; i++) {
            Client key = arr[i];
            int j = i - 1;

            while (j >= 0 &&
                    (arr[j].riskScore < key.riskScore ||   // DESC risk
                            (arr[j].riskScore == key.riskScore &&
                                    arr[j].accountBalance < key.accountBalance))) { // DESC balance

                arr[j + 1] = arr[j];
                j--;
            }

            arr[j + 1] = key;
        }
    }

    // ---------------- TOP N HIGH RISK
    public static void printTopRisk(Client[] arr, int n) {
        System.out.println("\nTop " + n + " High Risk Clients:");

        for (int i = 0; i < Math.min(n, arr.length); i++) {
            System.out.println(arr[i].name + "(" + arr[i].riskScore + ")");
        }
    }

    public static void main(String[] args) {

        Client[] clients = {
                new Client("clientC", 80, 5000),
                new Client("clientA", 20, 3000),
                new Client("clientB", 50, 7000)
        };

        // -------- BUBBLE SORT
        Client[] bubbleArr = Arrays.copyOf(clients, clients.length);
        bubbleSort(bubbleArr);

        System.out.println("\nBubble Sort (Ascending):");
        for (Client c : bubbleArr) {
            System.out.print(c + " ");
        }

        // -------- INSERTION SORT
        Client[] insertionArr = Arrays.copyOf(clients, clients.length);
        insertionSort(insertionArr);

        System.out.println("\n\nInsertion Sort (Descending):");
        for (Client c : insertionArr) {
            System.out.print(c + " ");
        }

        // -------- TOP RISKS
        printTopRisk(insertionArr, 10);
    }
}