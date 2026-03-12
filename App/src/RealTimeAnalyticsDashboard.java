import java.util.*;
import java.util.concurrent.*;

public class RealTimeAnalyticsDashboard {

    private Map<String, Integer> pageViews = new ConcurrentHashMap<>();
    private Map<String, Set<String>> uniqueVisitors = new ConcurrentHashMap<>();
    private Map<String, Integer> trafficSources = new ConcurrentHashMap<>();

    private final ScheduledExecutorService dashboardScheduler = Executors.newSingleThreadScheduledExecutor();

    public RealTimeAnalyticsDashboard() {
        // Schedule dashboard updates every 5 seconds
        dashboardScheduler.scheduleAtFixedRate(this::printDashboard, 5, 5, TimeUnit.SECONDS);
    }

    // Event structure
    public static class PageViewEvent {
        String url;
        String userId;
        String source;

        PageViewEvent(String url, String userId, String source) {
            this.url = url;
            this.userId = userId;
            this.source = source;
        }
    }

    // Process incoming page view event
    public void processEvent(PageViewEvent event) {
        pageViews.put(event.url, pageViews.getOrDefault(event.url, 0) + 1);

        uniqueVisitors.putIfAbsent(event.url, ConcurrentHashMap.newKeySet());
        uniqueVisitors.get(event.url).add(event.userId);

        trafficSources.put(event.source, trafficSources.getOrDefault(event.source, 0) + 1);
    }

    // Print top pages and traffic sources dashboard
    private void printDashboard() {

        System.out.println("\n=== Dashboard Update ===");

        // Top 10 pages by views
        PriorityQueue<Map.Entry<String, Integer>> pq = new PriorityQueue<>(
                Map.Entry.comparingByValue()
        );

        for (Map.Entry<String, Integer> entry : pageViews.entrySet()) {
            pq.offer(entry);
            if (pq.size() > 10) pq.poll();
        }

        List<Map.Entry<String, Integer>> topPages = new ArrayList<>();
        while (!pq.isEmpty()) topPages.add(pq.poll());
        Collections.reverse(topPages);

        System.out.println("Top Pages:");
        int rank = 1;
        for (Map.Entry<String, Integer> entry : topPages) {
            String url = entry.getKey();
            int views = entry.getValue();
            int unique = uniqueVisitors.getOrDefault(url, Collections.emptySet()).size();
            System.out.printf("%d. %s - %d views (%d unique)%n", rank++, url, views, unique);
        }

        // Traffic sources
        int totalTraffic = trafficSources.values().stream().mapToInt(Integer::intValue).sum();
        System.out.println("\nTraffic Sources:");
        for (Map.Entry<String, Integer> entry : trafficSources.entrySet()) {
            double percent = totalTraffic == 0 ? 0 : (entry.getValue() * 100.0 / totalTraffic);
            System.out.printf("%s: %.0f%%%n", entry.getKey(), percent);
        }

        System.out.println("========================\n");
    }

    public void shutdown() {
        dashboardScheduler.shutdown();
    }

    // Demo with user input
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        RealTimeAnalyticsDashboard dashboard = new RealTimeAnalyticsDashboard();

        System.out.println("Enter page view events in format: url userId source");
        System.out.println("Type 'exit' to quit.");

        while (true) {
            String line = sc.nextLine().trim();
            if (line.equalsIgnoreCase("exit")) break;

            String[] parts = line.split("\\s+");
            if (parts.length != 3) {
                System.out.println("Invalid input. Format: url userId source");
                continue;
            }

            PageViewEvent event = new PageViewEvent(parts[0], parts[1], parts[2]);
            dashboard.processEvent(event);
        }

        dashboard.shutdown();
        System.out.println("Exiting...");
    }
}