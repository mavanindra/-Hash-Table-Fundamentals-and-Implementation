import java.util.*;
import java.util.concurrent.TimeUnit;

class TokenBucket {
    private int tokens;
    private long lastRefillTime;
    private final int maxTokens;
    private final int refillRatePerSecond; // tokens added per second
    public TokenBucket(int maxTokens, int refillRatePerHour) {
        this.maxTokens = maxTokens;
        this.tokens = maxTokens;
        this.refillRatePerSecond = refillRatePerHour / 3600; // approximate per second
        this.lastRefillTime = System.currentTimeMillis();
    }
    public synchronized boolean allowRequest() {
        refill();
        if (tokens > 0) {
            tokens--;
            return true;
        } else {
            return false;
        }
    }
    private void refill() {
        long now = System.currentTimeMillis();
        long elapsedSeconds = (now - lastRefillTime) / 1000;
        if (elapsedSeconds > 0) {
            int refillTokens = (int) (elapsedSeconds * refillRatePerSecond);
            tokens = Math.min(tokens + refillTokens, maxTokens);
            lastRefillTime = now;
        }
    }
    public synchronized int getRemainingTokens() {
        refill();
        return tokens;
    }
    public synchronized long getNextRefillTimeSeconds() {
        if (tokens > 0) return 0;
        long now = System.currentTimeMillis();
        long refillIntervalMs = 1000 / Math.max(refillRatePerSecond,1);
        return TimeUnit.MILLISECONDS.toSeconds(refillIntervalMs);
    }
}

public class DistributedRateLimiter {
    private HashMap<String, TokenBucket> clients = new HashMap<>();
    private final int MAX_REQUESTS_PER_HOUR = 1000;
    public void addClient(String clientId) {
        clients.put(clientId, new TokenBucket(MAX_REQUESTS_PER_HOUR, MAX_REQUESTS_PER_HOUR));
        System.out.println("Client added: " + clientId);
    }

    public void checkRateLimit(String clientId) {
        if (!clients.containsKey(clientId)) {
            System.out.println("Client not found. Adding client.");
            addClient(clientId);
        }
        TokenBucket bucket = clients.get(clientId);
        boolean allowed = bucket.allowRequest();
        if (allowed) {
            System.out.println("Allowed (" + bucket.getRemainingTokens() + " requests remaining)");
        } else {
            System.out.println("Denied (0 requests remaining, retry after ~" + bucket.getNextRefillTimeSeconds() + "s)");
        }
    }
    public void getRateLimitStatus(String clientId) {
        if (!clients.containsKey(clientId)) {
            System.out.println("Client not found.");
            return;
        }
        TokenBucket bucket = clients.get(clientId);
        int used = MAX_REQUESTS_PER_HOUR - bucket.getRemainingTokens();
        long reset = System.currentTimeMillis() / 1000 + bucket.getNextRefillTimeSeconds();
        System.out.println("Client: " + clientId);
        System.out.println("{used: " + used + ", limit: " + MAX_REQUESTS_PER_HOUR +", reset: " + reset + "}");
    }
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        DistributedRateLimiter rateLimiter = new DistributedRateLimiter();
        while (true) {
            System.out.println("\n===== API Gateway Rate Limiter =====");
            System.out.println("1. Add Client");
            System.out.println("2. Check Rate Limit");
            System.out.println("3. Get Rate Limit Status");
            System.out.println("4. Exit");
            System.out.print("Enter choice: ");
            int choice = sc.nextInt();
            sc.nextLine();
            switch (choice) {
                case 1:
                    System.out.print("Enter Client ID: ");
                    String clientId = sc.nextLine();
                    rateLimiter.addClient(clientId);
                    break;
                case 2:
                    System.out.print("Enter Client ID: ");
                    clientId = sc.nextLine();
                    rateLimiter.checkRateLimit(clientId);
                    break;
                case 3:
                    System.out.print("Enter Client ID: ");
                    clientId = sc.nextLine();
                    rateLimiter.getRateLimitStatus(clientId);
                    break;
                case 4:
                    System.out.println("Exiting...");
                    sc.close();
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
}