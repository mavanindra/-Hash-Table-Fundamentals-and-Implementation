import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class DistributedRateLimiter {

    // TokenBucket inner class
    private static class TokenBucket {
        private final int maxTokens;
        private final long refillIntervalMs; // e.g., 1 hour = 3600000ms
        private AtomicInteger tokens;
        private long lastRefillTime;

        public TokenBucket(int maxTokens, long refillIntervalMs) {
            this.maxTokens = maxTokens;
            this.refillIntervalMs = refillIntervalMs;
            this.tokens = new AtomicInteger(maxTokens);
            this.lastRefillTime = System.currentTimeMillis();
        }

        public synchronized boolean allowRequest() {
            refillTokens();
            if (tokens.get() > 0) {
                tokens.decrementAndGet();
                return true;
            }
            return false;
        }

        public synchronized int getRemainingTokens() {
            refillTokens();
            return tokens.get();
        }

        public synchronized long getResetTimeSeconds() {
            refillTokens();
            return (lastRefillTime + refillIntervalMs) / 1000;
        }

        private void refillTokens() {
            long now = System.currentTimeMillis();
            if (now - lastRefillTime >= refillIntervalMs) {
                tokens.set(maxTokens);
                lastRefillTime = now;
            }
        }
    }

    private final ConcurrentHashMap<String, TokenBucket> clients = new ConcurrentHashMap<>();
    private final int maxRequests;
    private final long refillIntervalMs;

    public DistributedRateLimiter(int maxRequestsPerHour) {
        this.maxRequests = maxRequestsPerHour;
        this.refillIntervalMs = 60 * 60 * 1000; // 1 hour
    }

    // Check rate limit for a client
    public void checkRateLimit(String clientId) {
        clients.putIfAbsent(clientId, new TokenBucket(maxRequests, refillIntervalMs));
        TokenBucket bucket = clients.get(clientId);

        boolean allowed = bucket.allowRequest();
        int remaining = bucket.getRemainingTokens();

        if (allowed) {
            System.out.printf("checkRateLimit(clientId=\"%s\") → Allowed (%d requests remaining)%n",
                    clientId, remaining);
        } else {
            long resetSeconds = bucket.getResetTimeSeconds() - System.currentTimeMillis() / 1000;
            System.out.printf("checkRateLimit(clientId=\"%s\") → Denied (0 requests remaining, retry after %ds)%n",
                    clientId, resetSeconds);
        }
    }

    // Get current status for a client
    public void getRateLimitStatus(String clientId) {
        clients.putIfAbsent(clientId, new TokenBucket(maxRequests, refillIntervalMs));
        TokenBucket bucket = clients.get(clientId);
        int used = maxRequests - bucket.getRemainingTokens();
        long reset = bucket.getResetTimeSeconds();

        System.out.printf("getRateLimitStatus(\"%s\") → {used: %d, limit: %d, reset: %d}%n",
                clientId, used, maxRequests, reset);
    }

    // Demo with user input
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        DistributedRateLimiter limiter = new DistributedRateLimiter(5); // 5 requests/hour for demo

        System.out.println("Commands: check <clientId> | status <clientId> | exit");

        while (true) {
            String line = sc.nextLine().trim();
            if (line.equalsIgnoreCase("exit")) break;

            String[] parts = line.split("\\s+");
            if (parts.length != 2) {
                System.out.println("Invalid command. Format: check <clientId> or status <clientId>");
                continue;
            }

            String cmd = parts[0];
            String clientId = parts[1];

            switch (cmd.toLowerCase()) {
                case "check":
                    limiter.checkRateLimit(clientId);
                    break;
                case "status":
                    limiter.getRateLimitStatus(clientId);
                    break;
                default:
                    System.out.println("Unknown command.");
            }
        }

        System.out.println("Exiting...");
    }
}