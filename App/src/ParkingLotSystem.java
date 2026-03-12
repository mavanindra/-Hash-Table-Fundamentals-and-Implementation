import java.util.*;

class ParkingSpot {
    String licensePlate;
    long entryTime;
    boolean occupied;

    public ParkingSpot() {
        this.licensePlate = null;
        this.entryTime = 0;
        this.occupied = false;
    }
}

class ParkingLot {
    private ParkingSpot[] spots;
    private int capacity;
    private int occupiedCount;
    private int totalProbes;
    private Map<Integer, Integer> hourlyOccupancy; // hour -> count

    public ParkingLot(int capacity) {
        this.capacity = capacity;
        this.spots = new ParkingSpot[capacity];
        for (int i = 0; i < capacity; i++) {
            spots[i] = new ParkingSpot();
        }
        this.occupiedCount = 0;
        this.totalProbes = 0;
        this.hourlyOccupancy = new HashMap<>();
    }

    private int hash(String licensePlate) {
        return Math.abs(licensePlate.hashCode()) % capacity;
    }

    public String parkVehicle(String licensePlate) {
        int preferred = hash(licensePlate);
        int probes = 0;
        for (int i = 0; i < capacity; i++) {
            int idx = (preferred + i) % capacity;
            probes++;
            if (!spots[idx].occupied) {
                spots[idx].licensePlate = licensePlate;
                spots[idx].entryTime = System.currentTimeMillis();
                spots[idx].occupied = true;
                occupiedCount++;
                totalProbes += probes;
                int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                hourlyOccupancy.put(hour, hourlyOccupancy.getOrDefault(hour, 0) + 1);
                return "Assigned spot #" + idx + " (" + (probes - 1) + " probes)";
            }
        }
        return "Parking Lot Full!";
    }

    public String exitVehicle(String licensePlate) {
        for (int i = 0; i < capacity; i++) {
            if (spots[i].occupied && spots[i].licensePlate.equals(licensePlate)) {
                long durationMs = System.currentTimeMillis() - spots[i].entryTime;
                double hours = durationMs / 3600_000.0;
                double fee = hours * 5.0; // $5 per hour
                spots[i].occupied = false;
                spots[i].licensePlate = null;
                spots[i].entryTime = 0;
                occupiedCount--;
                return "Spot #" + i + " freed, Duration: " +
                        String.format("%.2f", hours) + "h, Fee: $" +
                        String.format("%.2f", fee);
            }
        }
        return "Vehicle not found!";
    }

    public String getStatistics() {
        double occupancyRate = (occupiedCount * 100.0) / capacity;
        double avgProbes = occupiedCount == 0 ? 0 : (double) totalProbes / occupiedCount;

        // Find peak hour
        int peakHour = -1;
        int maxCount = 0;
        for (Map.Entry<Integer, Integer> entry : hourlyOccupancy.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                peakHour = entry.getKey();
            }
        }

        return "Occupancy: " + String.format("%.2f", occupancyRate) + "%, " +
                "Avg Probes: " + String.format("%.2f", avgProbes) + ", " +
                "Peak Hour: " + (peakHour == -1 ? "N/A" : peakHour + ":00");
    }
}

public class ParkingLotSystem {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ParkingLot lot = new ParkingLot(500);

        while (true) {
            System.out.println("\n--- Parking Lot Menu ---");
            System.out.println("1. Park Vehicle");
            System.out.println("2. Exit Vehicle");
            System.out.println("3. Get Statistics");
            System.out.println("4. Exit Program");
            System.out.print("Enter choice: ");
            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Enter license plate: ");
                    String plate = sc.nextLine();
                    System.out.println(lot.parkVehicle(plate));
                    break;
                case 2:
                    System.out.print("Enter license plate: ");
                    plate = sc.nextLine();
                    System.out.println(lot.exitVehicle(plate));
                    break;
                case 3:
                    System.out.println(lot.getStatistics());
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
