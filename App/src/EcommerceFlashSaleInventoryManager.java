import java.util.*;

public class EcommerceFlashSaleInventoryManager {

    // productId -> stock count
    private HashMap<String, Integer> inventory = new HashMap<>();

    // productId -> waiting list (FIFO)
    private HashMap<String, LinkedHashMap<Integer, Integer>> waitingList = new HashMap<>();

    // add product
    public void addProduct(String productId, int stock) {
        inventory.put(productId, stock);
        waitingList.put(productId, new LinkedHashMap<>());
    }

    // check stock
    public void checkStock(String productId) {

        int stock = inventory.getOrDefault(productId, 0);

        System.out.println(
                "checkStock(\"" + productId + "\") → " +
                        stock + " units available"
        );
    }

    // purchase item (thread-safe)
    public synchronized void purchaseItem(String productId, int userId) {

        int stock = inventory.getOrDefault(productId, 0);

        if (stock > 0) {

            stock--;
            inventory.put(productId, stock);

            System.out.println(
                    "purchaseItem(\"" + productId + "\", userId=" + userId + ") → Success, "
                            + stock + " units remaining"
            );

        } else {

            LinkedHashMap<Integer, Integer> queue = waitingList.get(productId);
            queue.put(userId, queue.size() + 1);

            System.out.println(
                    "purchaseItem(\"" + productId + "\", userId=" + userId +
                            ") → Added to waiting list, position #" + queue.size()
            );
        }
    }

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        EcommerceFlashSaleInventoryManager manager = new EcommerceFlashSaleInventoryManager();

        System.out.print("Enter product ID: ");
        String productId = sc.nextLine();

        System.out.print("Enter stock quantity: ");
        int stock = sc.nextInt();

        manager.addProduct(productId, stock);

        while (true) {

            System.out.println("\n1. Check Stock");
            System.out.println("2. Purchase Item");
            System.out.println("3. Exit");

            System.out.print("Enter choice: ");
            int choice = sc.nextInt();

            switch (choice) {

                case 1:

                    manager.checkStock(productId);
                    break;

                case 2:

                    System.out.print("Enter userId: ");
                    int userId = sc.nextInt();

                    manager.purchaseItem(productId, userId);
                    break;

                case 3:

                    System.out.println("Exiting...");
                    return;

                default:

                    System.out.println("Invalid choice");
            }
        }
    }
}