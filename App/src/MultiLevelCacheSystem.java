import java.util.*;

class VideoData {
    String videoId;
    String content;

    public VideoData(String videoId, String content) {
        this.videoId = videoId;
        this.content = content;
    }
}

// Generic LRU Cache using LinkedHashMap
class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;

    public LRUCache(int capacity) {
        super(capacity, 0.75f, true); // access-order
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }
}

class MultiLevelCache {
    private LRUCache<String, VideoData> L1; // Memory
    private LRUCache<String, VideoData> L2; // SSD
    private Map<String, VideoData> L3;      // Database

    private int L1Hits = 0, L2Hits = 0, L3Hits = 0;
    private int L1Miss = 0, L2Miss = 0, L3Miss = 0;

    private Map<String, Integer> accessCount = new HashMap<>();

    public MultiLevelCache() {
        L1 = new LRUCache<>(10_000);
        L2 = new LRUCache<>(100_000);
        L3 = new HashMap<>(); // simulate DB
    }

    public void addToDatabase(String videoId, String content) {
        L3.put(videoId, new VideoData(videoId, content));
    }

    public VideoData getVideo(String videoId) {
        long start = System.nanoTime();

        // L1 Cache
        if (L1.containsKey(videoId)) {
            L1Hits++;
            System.out.println("→ L1 Cache HIT (0.5ms)");
            return L1.get(videoId);
        } else {
            L1Miss++;
            System.out.println("→ L1 Cache MISS");
        }

        // L2 Cache
        if (L2.containsKey(videoId)) {
            L2Hits++;
            System.out.println("→ L2 Cache HIT (5ms)");
            VideoData data = L2.get(videoId);
            promoteToL1(videoId, data);
            return data;
        } else {
            L2Miss++;
            System.out.println("→ L2 Cache MISS");
        }

        // L3 Database
        if (L3.containsKey(videoId)) {
            L3Hits++;
            System.out.println("→ L3 Database HIT (150ms)");
            VideoData data = L3.get(videoId);
            L2.put(videoId, data);
            incrementAccess(videoId);
            return data;
        } else {
            L3Miss++;
            System.out.println("→ L3 Database MISS");
            return null;
        }
    }

    private void promoteToL1(String videoId, VideoData data) {
        int count = accessCount.getOrDefault(videoId, 0) + 1;
        accessCount.put(videoId, count);
        if (count > 5) { // threshold
            L1.put(videoId, data);
            System.out.println("→ Promoted to L1");
        }
    }

    private void incrementAccess(String videoId) {
        accessCount.put(videoId, accessCount.getOrDefault(videoId, 0) + 1);
    }

    public void invalidate(String videoId) {
        L1.remove(videoId);
        L2.remove(videoId);
        L3.remove(videoId);
        System.out.println("→ Content invalidated for " + videoId);
    }

    public void getStatistics() {
        int totalHits = L1Hits + L2Hits + L3Hits;
        int totalMiss = L1Miss + L2Miss + L3Miss;
        int totalRequests = totalHits + totalMiss;

        System.out.println("L1: Hit Rate " + percent(L1Hits, totalRequests) + "%, Avg Time: 0.5ms");
        System.out.println("L2: Hit Rate " + percent(L2Hits, totalRequests) + "%, Avg Time: 5ms");
        System.out.println("L3: Hit Rate " + percent(L3Hits, totalRequests) + "%, Avg Time: 150ms");
        System.out.println("Overall: Hit Rate " + percent(totalHits, totalRequests) + "%");
    }

    private String percent(int hits, int total) {
        if (total == 0) return "0";
        return String.format("%.2f", (hits * 100.0) / total);
    }
}

public class MultiLevelCacheSystem {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        MultiLevelCache cache = new MultiLevelCache();

        // preload database
        cache.addToDatabase("video_123", "Popular Movie");
        cache.addToDatabase("video_999", "Rare Documentary");
        cache.addToDatabase("video_456", "Comedy Show");
        cache.addToDatabase("video_789", "Sports Highlight");

        while (true) {
            System.out.println("\n--- Multi-Level Cache Menu ---");
            System.out.println("1. Get Video");
            System.out.println("2. Invalidate Video");
            System.out.println("3. Get Statistics");
            System.out.println("4. Exit");
            System.out.print("Enter choice: ");
            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Enter videoId: ");
                    String vid = sc.nextLine();
                    VideoData v = cache.getVideo(vid);
                    if (v != null) {
                        System.out.println("Video Content: " + v.content);
                    } else {
                        System.out.println("Video not found!");
                    }
                    break;
                case 2:
                    System.out.print("Enter videoId to invalidate: ");
                    vid = sc.nextLine();
                    cache.invalidate(vid);
                    break;
                case 3:
                    cache.getStatistics();
                    break;
                case 4:
                    System.out.println("Exiting...");
                    sc.close();
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
}
