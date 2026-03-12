import java.util.*;

public class SocialMediaUsernameAvailabilityChecker {

    // username -> userId
    private HashMap<String, Integer> userMap = new HashMap<>();

    // username -> attempt frequency
    private HashMap<String, Integer> attemptFrequency = new HashMap<>();

    // Register user
    public void registerUser(String username, int userId) {
        userMap.put(username, userId);
    }

    // Check username availability
    public boolean checkAvailability(String username) {

        attemptFrequency.put(username,
                attemptFrequency.getOrDefault(username, 0) + 1);

        return !userMap.containsKey(username);
    }

    // Suggest alternatives
    public List<String> suggestAlternatives(String username) {

        List<String> suggestions = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            String suggestion = username + i;

            if (!userMap.containsKey(suggestion)) {
                suggestions.add(suggestion);
            }
        }

        String modified = username.replace("_", ".");
        if (!userMap.containsKey(modified)) {
            suggestions.add(modified);
        }

        return suggestions;
    }

    // Most attempted username
    public String getMostAttempted() {

        String most = null;
        int max = 0;

        for (Map.Entry<String, Integer> entry : attemptFrequency.entrySet()) {

            if (entry.getValue() > max) {
                max = entry.getValue();
                most = entry.getKey();
            }
        }

        if (most == null)
            return "No attempts yet";

        return most + " (" + max + " attempts)";
    }

    // MAIN METHOD
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        SocialMediaUsernameAvailabilityChecker checker = new SocialMediaUsernameAvailabilityChecker();

        int userIdCounter = 1;

        while (true) {

            System.out.println("\n==== Username Availability System ====");
            System.out.println("1. Register User");
            System.out.println("2. Check Username Availability");
            System.out.println("3. Suggest Alternatives");
            System.out.println("4. Show Most Attempted Username");
            System.out.println("5. Exit");

            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {

                case 1:
                    System.out.print("Enter username to register: ");
                    String username = scanner.nextLine();

                    if (checker.checkAvailability(username)) {
                        checker.registerUser(username, userIdCounter++);
                        System.out.println("User registered successfully!");
                    } else {
                        System.out.println("Username already taken.");
                    }
                    break;

                case 2:
                    System.out.print("Enter username to check: ");
                    String checkName = scanner.nextLine();

                    boolean available = checker.checkAvailability(checkName);

                    if (available)
                        System.out.println("Username is available!");
                    else
                        System.out.println("Username is already taken.");
                    break;

                case 3:
                    System.out.print("Enter username for suggestions: ");
                    String suggestName = scanner.nextLine();

                    List<String> suggestions =
                            checker.suggestAlternatives(suggestName);

                    System.out.println("Suggestions: " + suggestions);
                    break;

                case 4:
                    System.out.println("Most attempted username: "
                            + checker.getMostAttempted());
                    break;

                case 5:
                    System.out.println("Exiting program...");
                    return;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
}