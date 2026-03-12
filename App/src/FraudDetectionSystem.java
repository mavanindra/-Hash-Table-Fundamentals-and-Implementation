import java.util.*;

class Transaction {
    int id;
    double amount;
    String merchant;
    String account;
    long timestamp; // epoch ms

    public Transaction(int id, double amount, String merchant, String account, long timestamp) {
        this.id = id;
        this.amount = amount;
        this.merchant = merchant;
        this.account = account;
        this.timestamp = timestamp;
    }
}

class FraudDetector {
    private List<Transaction> transactions;

    public FraudDetector(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    // Classic Two-Sum
    public List<String> findTwoSum(double target) {
        Map<Double, Transaction> map = new HashMap<>();
        List<String> result = new ArrayList<>();
        for (Transaction t : transactions) {
            double complement = target - t.amount;
            if (map.containsKey(complement)) {
                Transaction other = map.get(complement);
                result.add("(" + other.id + ", " + t.id + ")");
            }
            map.put(t.amount, t);
        }
        return result;
    }

    // Two-Sum with time window (1 hour)
    public List<String> findTwoSumWithinHour(double target) {
        List<String> result = new ArrayList<>();
        Map<Double, List<Transaction>> map = new HashMap<>();
        for (Transaction t : transactions) {
            double complement = target - t.amount;
            if (map.containsKey(complement)) {
                for (Transaction other : map.get(complement)) {
                    if (Math.abs(t.timestamp - other.timestamp) <= 3600_000) {
                        result.add("(" + other.id + ", " + t.id + ")");
                    }
                }
            }
            map.computeIfAbsent(t.amount, k -> new ArrayList<>()).add(t);
        }
        return result;
    }

    // K-Sum (recursive)
    public List<List<Integer>> findKSum(int k, double target) {
        List<List<Integer>> result = new ArrayList<>();
        backtrack(transactions, k, target, 0, new ArrayList<>(), result);
        return result;
    }

    private void backtrack(List<Transaction> txs, int k, double target, int start,
                           List<Integer> current, List<List<Integer>> result) {
        if (k == 0 && target == 0) {
            result.add(new ArrayList<>(current));
            return;
        }
        if (k == 0 || start >= txs.size()) return;

        for (int i = start; i < txs.size(); i++) {
            current.add(txs.get(i).id);
            backtrack(txs, k - 1, target - txs.get(i).amount, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }

    // Duplicate detection
    public List<String> detectDuplicates() {
        Map<String, Map<Double, Set<String>>> map = new HashMap<>();
        List<String> result = new ArrayList<>();

        for (Transaction t : transactions) {
            map.putIfAbsent(t.merchant, new HashMap<>());
            Map<Double, Set<String>> amtMap = map.get(t.merchant);
            amtMap.putIfAbsent(t.amount, new HashSet<>());

            Set<String> accounts = amtMap.get(t.amount);
            if (accounts.contains(t.account)) continue; // same account, ignore
            if (!accounts.isEmpty()) {
                result.add("{amount:" + t.amount + ", merchant:" + t.merchant +
                        ", accounts:" + accounts + " & " + t.account + "}");
            }
            accounts.add(t.account);
        }
        return result;
    }
}

public class FraudDetectionSystem {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        List<Transaction> txs = new ArrayList<>();
        FraudDetector detector = new FraudDetector(txs);

        while (true) {
            System.out.println("\n--- Fraud Detection Menu ---");
            System.out.println("1. Add Transaction");
            System.out.println("2. Find Two-Sum");
            System.out.println("3. Find Two-Sum within 1 hour");
            System.out.println("4. Find K-Sum");
            System.out.println("5. Detect Duplicates");
            System.out.println("6. Exit");
            System.out.print("Enter choice: ");
            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Enter transaction id: ");
                    int id = sc.nextInt();
                    System.out.print("Enter amount: ");
                    double amount = sc.nextDouble();
                    sc.nextLine();
                    System.out.print("Enter merchant: ");
                    String merchant = sc.nextLine();
                    System.out.print("Enter account: ");
                    String account = sc.nextLine();
                    System.out.print("Enter time (HH:MM): ");
                    String timeStr = sc.nextLine();
                    String[] parts = timeStr.split(":");
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
                    cal.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
                    long timestamp = cal.getTimeInMillis();

                    txs.add(new Transaction(id, amount, merchant, account, timestamp));
                    System.out.println("Transaction added.");
                    break;

                case 2:
                    System.out.print("Enter target amount: ");
                    double target = sc.nextDouble();
                    System.out.println("Pairs: " + detector.findTwoSum(target));
                    break;

                case 3:
                    System.out.print("Enter target amount: ");
                    target = sc.nextDouble();
                    System.out.println("Pairs within 1 hour: " + detector.findTwoSumWithinHour(target));
                    break;

                case 4:
                    System.out.print("Enter K: ");
                    int k = sc.nextInt();
                    System.out.print("Enter target amount: ");
                    target = sc.nextDouble();
                    System.out.println("K-Sum results: " + detector.findKSum(k, target));
                    break;

                case 5:
                    System.out.println("Duplicates: " + detector.detectDuplicates());
                    break;

                case 6:
                    System.out.println("Exiting...");
                    sc.close();
                    return;

                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
}
