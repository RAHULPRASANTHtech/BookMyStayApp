import java.util.*;

// ===== ROOM DOMAIN =====
abstract class Room {
    protected int beds;
    protected int size;
    protected double price;

    public Room(int beds, int size, double price) {
        this.beds = beds;
        this.size = size;
        this.price = price;
    }

    public void displayDetails() {
        System.out.println("Beds: " + beds);
        System.out.println("Size: " + size + " sqft");
        System.out.println("Price per night: " + price);
    }
}

class SingleRoom extends Room {
    public SingleRoom() { super(1, 250, 1500.0); }
}

class DoubleRoom extends Room {
    public DoubleRoom() { super(2, 400, 2500.0); }
}

class SuiteRoom extends Room {
    public SuiteRoom() { super(3, 750, 5000.0); }
}

// ===== UC3: INVENTORY =====
class RoomInventory {
    private HashMap<String, Integer> inventory;

    public RoomInventory() {
        inventory = new HashMap<>();
        inventory.put("Single", 2);
        inventory.put("Double", 2);
        inventory.put("Suite", 1);
    }

    public int getAvailability(String type) {
        return inventory.getOrDefault(type, 0);
    }

    public void decreaseAvailability(String type) {
        inventory.put(type, getAvailability(type) - 1);
    }
}

// ===== UC5: RESERVATION =====
class Reservation {
    private String guestName;
    private String roomType;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getGuestName() { return guestName; }
    public String getRoomType() { return roomType; }
}

// ===== UC5: QUEUE =====
class BookingRequestQueue {
    private Queue<Reservation> queue = new LinkedList<>();

    public void addRequest(Reservation r) {
        queue.add(r);
    }

    public Reservation getNextRequest() {
        return queue.poll(); // FIFO
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}

// ===== UC6: BOOKING SERVICE =====
class BookingService {

    private RoomInventory inventory;

    // Track allocated room IDs (uniqueness)
    private Set<String> allocatedRoomIds = new HashSet<>();

    // Map room type → allocated IDs
    private HashMap<String, Set<String>> allocationMap = new HashMap<>();

    public BookingService(RoomInventory inventory) {
        this.inventory = inventory;

        allocationMap.put("Single", new HashSet<>());
        allocationMap.put("Double", new HashSet<>());
        allocationMap.put("Suite", new HashSet<>());
    }

    // Generate unique room ID
    private String generateRoomId(String type) {
        return type.substring(0, 1).toUpperCase() + UUID.randomUUID().toString().substring(0, 4);
    }

    // Process queue
    public void processBookings(BookingRequestQueue queue) {

        System.out.println("\nProcessing Booking Requests...\n");

        while (!queue.isEmpty()) {

            Reservation r = queue.getNextRequest();
            String type = r.getRoomType();

            // Check availability
            if (inventory.getAvailability(type) > 0) {

                String roomId;

                // Ensure unique ID
                do {
                    roomId = generateRoomId(type);
                } while (allocatedRoomIds.contains(roomId));

                // Store ID
                allocatedRoomIds.add(roomId);
                allocationMap.get(type).add(roomId);

                // Update inventory
                inventory.decreaseAvailability(type);

                System.out.println("Booking Confirmed: "
                        + r.getGuestName()
                        + " -> " + type
                        + " Room | ID: " + roomId);

            } else {
                System.out.println("Booking Failed (No Availability): "
                        + r.getGuestName()
                        + " -> " + type);
            }
        }
    }
}

// ===== MAIN =====
public class BookMyStayApp {
    public static void main(String[] args) {

        RoomInventory inventory = new RoomInventory();

        BookingRequestQueue queue = new BookingRequestQueue();

        // Simulated requests
        queue.addRequest(new Reservation("Rahul", "Single"));
        queue.addRequest(new Reservation("Arun", "Double"));
        queue.addRequest(new Reservation("Priya", "Suite"));
        queue.addRequest(new Reservation("Kiran", "Single"));
        queue.addRequest(new Reservation("Vikram", "Suite")); // should fail

        BookingService bookingService = new BookingService(inventory);

        bookingService.processBookings(queue);
    }
}