import java.util.*;

class TrieNode {
    Map<Character, TrieNode> children = new HashMap<>();
    boolean isEndOfWord = false;
    String query = null;
}

class QueryFrequency {
    String query;
    int frequency;

    QueryFrequency(String query, int frequency) {
        this.query = query;
        this.frequency = frequency;
    }
}

public class AutocompleteSystem {

    private TrieNode root = new TrieNode();
    private HashMap<String, Integer> frequencyMap = new HashMap<>();

    // Insert query into Trie
    public void insertQuery(String query) {
        TrieNode node = root;

        for (char c : query.toCharArray()) {
            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);
        }

        node.isEndOfWord = true;
        node.query = query;
        frequencyMap.put(query, frequencyMap.getOrDefault(query, 0) + 1);
    }

    // Update frequency manually
    public void updateFrequency(String query) {
        frequencyMap.put(query, frequencyMap.getOrDefault(query, 0) + 1);
        insertQuery(query); // ensure query exists in Trie
        System.out.println("Updated frequency of \"" + query + "\" → " +
                frequencyMap.get(query));
    }

    // Search for top K suggestions for a prefix
    public List<QueryFrequency> search(String prefix, int topK) {
        TrieNode node = root;

        for (char c : prefix.toCharArray()) {
            if (!node.children.containsKey(c)) {
                return Collections.emptyList();
            }
            node = node.children.get(c);
        }

        PriorityQueue<QueryFrequency> minHeap =
                new PriorityQueue<>(Comparator.comparingInt(q -> q.frequency));

        dfs(node, minHeap, topK);

        List<QueryFrequency> result = new ArrayList<>();
        while (!minHeap.isEmpty()) {
            result.add(minHeap.poll());
        }
        Collections.reverse(result); // highest frequency first
        return result;
    }

    private void dfs(TrieNode node, PriorityQueue<QueryFrequency> heap, int topK) {
        if (node.isEndOfWord && node.query != null) {
            int freq = frequencyMap.getOrDefault(node.query, 0);
            heap.offer(new QueryFrequency(node.query, freq));
            if (heap.size() > topK) heap.poll();
        }

        for (TrieNode child : node.children.values()) {
            dfs(child, heap, topK);
        }
    }

    // Suggest corrections for minor typos (1 character off)
    public List<String> suggestCorrections(String query) {
        List<String> suggestions = new ArrayList<>();
        for (String key : frequencyMap.keySet()) {
            if (isOneEditDistance(query, key)) {
                suggestions.add(key);
            }
        }
        return suggestions;
    }

    private boolean isOneEditDistance(String s1, String s2) {
        if (Math.abs(s1.length() - s2.length()) > 1) return false;

        int edits = 0, i = 0, j = 0;
        while (i < s1.length() && j < s2.length()) {
            if (s1.charAt(i) != s2.charAt(j)) {
                edits++;
                if (edits > 1) return false;
                if (s1.length() > s2.length()) i++;
                else if (s1.length() < s2.length()) j++;
                else { i++; j++; }
            } else { i++; j++; }
        }
        if (i < s1.length() || j < s2.length()) edits++;
        return edits == 1;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        AutocompleteSystem autocomplete = new AutocompleteSystem();

        while (true) {
            System.out.println("\n===== Autocomplete System =====");
            System.out.println("1. Insert Query");
            System.out.println("2. Search Prefix");
            System.out.println("3. Update Frequency");
            System.out.println("4. Suggest Corrections");
            System.out.println("5. Exit");
            System.out.print("Enter choice: ");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Enter query to insert: ");
                    String q = sc.nextLine();
                    autocomplete.insertQuery(q);
                    System.out.println("Query inserted.");
                    break;

                case 2:
                    System.out.print("Enter prefix to search: ");
                    String prefix = sc.nextLine();
                    List<QueryFrequency> results = autocomplete.search(prefix, 10);
                    if (results.isEmpty()) {
                        System.out.println("No suggestions found.");
                    } else {
                        System.out.println("Top suggestions:");
                        for (QueryFrequency fq : results) {
                            System.out.println(fq.query + " (" + fq.frequency + ")");
                        }
                    }
                    break;

                case 3:
                    System.out.print("Enter query to update frequency: ");
                    String uq = sc.nextLine();
                    autocomplete.updateFrequency(uq);
                    break;

                case 4:
                    System.out.print("Enter query for corrections: ");
                    String typo = sc.nextLine();
                    List<String> corrections = autocomplete.suggestCorrections(typo);
                    if (corrections.isEmpty()) {
                        System.out.println("No corrections found.");
                    } else {
                        System.out.println("Did you mean:");
                        for (String s : corrections) {
                            System.out.println(s);
                        }
                    }
                    break;

                case 5:
                    System.out.println("Exiting...");
                    sc.close();
                    return;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
}
