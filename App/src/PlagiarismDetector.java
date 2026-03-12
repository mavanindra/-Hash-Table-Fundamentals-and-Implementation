import java.util.*;
import java.io.*;

public class PlagiarismDetector {

    private final int N_GRAM = 5; // using 5-grams
    private Map<String, Set<String>> nGramIndex = new HashMap<>();
    private List<String> documentIds = new ArrayList<>();
    private Map<String, List<String>> documents = new HashMap<>(); // docId -> content

    // Add a document to the database
    public void addDocument(String docId, String content) {
        documentIds.add(docId);
        documents.put(docId, Arrays.asList(content.split("\\s+")));

        List<String> words = Arrays.asList(content.split("\\s+"));
        for (int i = 0; i <= words.size() - N_GRAM; i++) {
            String ngram = String.join(" ", words.subList(i, i + N_GRAM)).toLowerCase();
            nGramIndex.putIfAbsent(ngram, new HashSet<>());
            nGramIndex.get(ngram).add(docId);
        }
    }

    // Analyze a new document
    public void analyzeDocument(String docId, String content) {

        List<String> words = Arrays.asList(content.split("\\s+"));
        List<String> ngrams = new ArrayList<>();
        Map<String, Integer> matchCount = new HashMap<>();

        for (int i = 0; i <= words.size() - N_GRAM; i++) {
            String ngram = String.join(" ", words.subList(i, i + N_GRAM)).toLowerCase();
            ngrams.add(ngram);

            Set<String> matchedDocs = nGramIndex.getOrDefault(ngram, new HashSet<>());
            for (String matchedDoc : matchedDocs) {
                matchCount.put(matchedDoc, matchCount.getOrDefault(matchedDoc, 0) + 1);
            }
        }

        System.out.println("analyzeDocument(\"" + docId + "\")");
        System.out.println("→ Extracted " + ngrams.size() + " n-grams");

        for (String matchedDoc : matchCount.keySet()) {
            int matches = matchCount.get(matchedDoc);
            double similarity = matches * 100.0 / ngrams.size();
            String flag = similarity > 50 ? "(PLAGIARISM DETECTED)" : "(suspicious)";
            System.out.printf("→ Found %d matching n-grams with \"%s\"\n", matches, matchedDoc);
            System.out.printf("→ Similarity: %.1f%% %s%n", similarity, flag);
        }

        if (matchCount.isEmpty()) {
            System.out.println("→ No significant matches found.");
        }
    }

    // Demo main method with user input
    public static void main(String[] args) throws IOException {

        Scanner sc = new Scanner(System.in);
        PlagiarismDetector detector = new PlagiarismDetector();

        // Simulate previous documents (database)
        detector.addDocument("essay_089.txt", "This is a sample essay content used for testing plagiarism detection in our system.");
        detector.addDocument("essay_092.txt", "Another sample essay which shares significant content with the student's essay for plagiarism detection purposes.");

        System.out.print("Enter new document ID: ");
        String newDocId = sc.nextLine();

        System.out.println("Enter document content (single line or multiple lines, end with an empty line):");
        StringBuilder sb = new StringBuilder();
        while (true) {
            String line = sc.nextLine();
            if (line.isEmpty()) break;
            sb.append(line).append(" ");
        }

        String newDocContent = sb.toString().trim();

        detector.analyzeDocument(newDocId, newDocContent);
    }
}
