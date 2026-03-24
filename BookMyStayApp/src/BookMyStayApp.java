import java.util.HashMap;

// Abstract Room Class
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

// Room Types
class SingleRoom extends Room {
    public SingleRoom() {
        super(1, 250, 1500.0);
    }
}

class DoubleRoom extends Room {
    public DoubleRoom() {
        super(2, 400, 2500.0);
    }
}

class SuiteRoom extends Room {
    public SuiteRoom() {
        super(3, 750, 5000.0);
    }
}

// 🔥 NEW: Inventory Class (UC3 Core)
class RoomInventory {
    private HashMap<String, Integer> inventory;

    public RoomInventory() {
        inventory = new HashMap<>();

        // Initialize availability
        inventory.put("Single", 5);
        inventory.put("Double", 3);
        inventory.put("Suite", 2);
    }

    // Get availability
    public int getAvailability(String type) {
        return inventory.getOrDefault(type, 0);
    }

    // Update availability
    public void updateAvailability(String type, int count) {
        inventory.put(type, count);
    }

    // Display all inventory
    public void displayInventory(Room single, Room dbl, Room suite) {

        System.out.println("Hotel Room Inventory Status\n");

        System.out.println("Single Room:");
        single.displayDetails();
        System.out.println("Available Rooms: " + getAvailability("Single") + "\n");

        System.out.println("Double Room:");
        dbl.displayDetails();
        System.out.println("Available Rooms: " + getAvailability("Double") + "\n");

        System.out.println("Suite Room:");
        suite.displayDetails();
        System.out.println("Available Rooms: " + getAvailability("Suite"));
    }
}

// Main Class
public class BookMyStayApp {
    public static void main(String[] args) {

        // Create room objects (same as UC2)
        Room single = new SingleRoom();
        Room dbl = new DoubleRoom();
        Room suite = new SuiteRoom();

        // 🔥 Use centralized inventory
        RoomInventory inventory = new RoomInventory();

        // Display inventory
        inventory.displayInventory(single, dbl, suite);
    }
}