import java.util.*;

public class AutocompleteSystem {

    // Trie Node
    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isWord = false;
        String word = null;
    }

    private final TrieNode root = new TrieNode();
    private final Map<String, Integer> frequencyMap = new HashMap<>();

    // Insert a query into trie and frequency map
    public void insertQuery(String query, int frequency) {
        frequencyMap.put(query, frequencyMap.getOrDefault(query, 0) + frequency);

        TrieNode node = root;
        for (char c : query.toCharArray()) {
            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);
        }
        node.isWord = true;
        node.word = query;
    }

    // Update frequency of a query
    public void updateFrequency(String query) {
        insertQuery(query, 1);
        System.out.printf("updateFrequency(\"%s\") → Frequency: %d%n", query, frequencyMap.get(query));
    }

    // Autocomplete suggestions
    public List<String> search(String prefix) {
        TrieNode node = root;
        for (char c : prefix.toCharArray()) {
            if (!node.children.containsKey(c)) return Collections.emptyList();
            node = node.children.get(c);
        }

        PriorityQueue<String> minHeap = new PriorityQueue<>(
                (a, b) -> frequencyMap.get(a).equals(frequencyMap.get(b))
                        ? a.compareTo(b)
                        : frequencyMap.get(a) - frequencyMap.get(b)
        );

        dfs(node, minHeap);

        List<String> result = new ArrayList<>();
        while (!minHeap.isEmpty()) result.add(minHeap.poll());
        Collections.reverse(result); // top frequencies first
        return result;
    }

    // DFS traversal to find all words under node
    private void dfs(TrieNode node, PriorityQueue<String> heap) {
        if (node.isWord) {
            heap.offer(node.word);
            if (heap.size() > 10) heap.poll(); // maintain top 10
        }

        for (TrieNode child : node.children.values()) {
            dfs(child, heap);
        }
    }

    // Demo main method
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        AutocompleteSystem system = new AutocompleteSystem();

        // Sample queries for demo
        system.insertQuery("java tutorial", 1234567);
        system.insertQuery("javascript", 987654);
        system.insertQuery("java download", 456789);
        system.insertQuery("jav 21 features", 1000);
        system.insertQuery("java concurrency", 2000);
        system.insertQuery("java streams", 1500);
        system.insertQuery("jav basics", 800);

        System.out.println("Autocomplete System Ready.");
        System.out.println("Commands: search <prefix> | update <query> | exit");

        while (true) {
            String line = sc.nextLine().trim();
            if (line.equalsIgnoreCase("exit")) break;

            String[] parts = line.split("\\s+", 2);
            if (parts.length != 2) {
                System.out.println("Invalid command. Use: search <prefix> or update <query>");
                continue;
            }

            String cmd = parts[0];
            String arg = parts[1];

            switch (cmd.toLowerCase()) {
                case "search":
                    List<String> suggestions = system.search(arg);
                    System.out.println("search(\"" + arg + "\") →");
                    int rank = 1;
                    for (String s : suggestions) {
                        System.out.printf("%d. \"%s\" (%d searches)%n", rank++, s, system.frequencyMap.get(s));
                    }
                    break;

                case "update":
                    system.updateFrequency(arg);
                    break;

                default:
                    System.out.println("Unknown command.");
            }
        }

        System.out.println("Exiting...");
    }
}