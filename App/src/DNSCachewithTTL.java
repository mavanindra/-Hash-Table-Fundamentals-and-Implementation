import java.util.*;
import java.util.concurrent.*;

public class DNSCachewithTTL {

    // Inner class for DNS entries
    private class DNSEntry {
        String domain;
        String ipAddress;
        long expiryTime; // in milliseconds

        DNSEntry(String domain, String ipAddress, long ttlSeconds) {
            this.domain = domain;
            this.ipAddress = ipAddress;
            this.expiryTime = System.currentTimeMillis() + ttlSeconds * 1000;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    private final int capacity; // max cache size
    private final HashMap<String, DNSEntry> cache;
    private final LinkedHashMap<String, DNSEntry> lruOrder; // maintain LRU
    private int hits = 0, misses = 0;
    private long totalLookupTimeNs = 0;

    // Scheduled executor to clean expired entries
    private final ScheduledExecutorService cleaner = Executors.newSingleThreadScheduledExecutor();

    public DNSCachewithTTL(int capacity) {
        this.capacity = capacity;
        this.cache = new HashMap<>();
        this.lruOrder = new LinkedHashMap<>(capacity, 0.75f, true);

        // Clean expired entries every second
        cleaner.scheduleAtFixedRate(this::removeExpiredEntries, 1, 1, TimeUnit.SECONDS);
    }

    // Resolve domain
    public synchronized String resolve(String domain) {

        long start = System.nanoTime();

        DNSEntry entry = cache.get(domain);

        if (entry != null && !entry.isExpired()) {
            // Cache HIT
            hits++;
            lruOrder.get(domain); // touch for LRU
            long duration = System.nanoTime() - start;
            totalLookupTimeNs += duration;

            System.out.printf("resolve(\"%s\") → Cache HIT → %s (retrieved in %.2fms)%n",
                    domain, entry.ipAddress, duration / 1_000_000.0);

            return entry.ipAddress;
        }

        // Cache MISS
        misses++;

        String ip = queryUpstreamDNS(domain);
        long ttl = 300; // simulate TTL 300s

        DNSEntry newEntry = new DNSEntry(domain, ip, ttl);
        addToCache(domain, newEntry);

        long duration = System.nanoTime() - start;
        totalLookupTimeNs += duration;

        if (entry != null && entry.isExpired()) {
            System.out.printf("resolve(\"%s\") → Cache EXPIRED → Query upstream → %s (TTL: %ds)%n",
                    domain, ip, ttl);
        } else {
            System.out.printf("resolve(\"%s\") → Cache MISS → Query upstream → %s (TTL: %ds)%n",
                    domain, ip, ttl);
        }

        return ip;
    }

    // Add entry to cache with LRU eviction
    private void addToCache(String domain, DNSEntry entry) {

        if (cache.size() >= capacity) {
            // Evict LRU
            Iterator<String> it = lruOrder.keySet().iterator();
            if (it.hasNext()) {
                String lruKey = it.next();
                it.remove();
                cache.remove(lruKey);
            }
        }

        cache.put(domain, entry);
        lruOrder.put(domain, entry);
    }

    // Remove expired entries
    private synchronized void removeExpiredEntries() {

        Iterator<Map.Entry<String, DNSEntry>> it = cache.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<String, DNSEntry> e = it.next();
            if (e.getValue().isExpired()) {
                it.remove();
                lruOrder.remove(e.getKey());
            }
        }
    }

    // Simulate upstream DNS query
    private String queryUpstreamDNS(String domain) {
        // For simplicity, generate a fake IP
        Random rand = new Random(domain.hashCode());
        return String.format("%d.%d.%d.%d", rand.nextInt(256), rand.nextInt(256),
                rand.nextInt(256), rand.nextInt(256));
    }

    // Print cache stats
    public void getCacheStats() {
        int total = hits + misses;
        double hitRate = total == 0 ? 0 : (hits * 100.0 / total);
        double avgLookupMs = total == 0 ? 0 : (totalLookupTimeNs / 1_000_000.0 / total);

        System.out.printf("getCacheStats() → Hit Rate: %.1f%%, Avg Lookup Time: %.2fms%n",
                hitRate, avgLookupMs);
    }

    // Main method
    public static void main(String[] args) throws InterruptedException {

        Scanner sc = new Scanner(System.in);

        DNSCachewithTTL dnsCache = new DNSCachewithTTL(5); // small cache for testing

        while (true) {

            System.out.println("\n1. Resolve domain");
            System.out.println("2. Show cache stats");
            System.out.println("3. Exit");

            System.out.print("Enter choice: ");
            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {

                case 1:
                    System.out.print("Enter domain: ");
                    String domain = sc.nextLine();

                    dnsCache.resolve(domain);
                    break;

                case 2:
                    dnsCache.getCacheStats();
                    break;

                case 3:
                    System.out.println("Exiting...");
                    dnsCache.cleaner.shutdown();
                    return;

                default:
                    System.out.println("Invalid choice");
            }
        }
    }
}