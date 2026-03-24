import java.util.*;

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

// UC3: Inventory
class RoomInventory {
    private HashMap<String, Integer> inventory;

    public RoomInventory() {
        inventory = new HashMap<>();
        inventory.put("Single", 5);
        inventory.put("Double", 3);
        inventory.put("Suite", 2);
    }

    public int getAvailability(String type) {
        return inventory.getOrDefault(type, 0);
    }
}

// UC4: Search Service (READ ONLY)
class RoomSearchService {
    public void searchAvailableRooms(RoomInventory inventory,
                                     Room single,
                                     Room dbl,
                                     Room suite) {

        System.out.println("Available Rooms for Booking\n");

        if (inventory.getAvailability("Single") > 0) {
            System.out.println("Single Room:");
            single.displayDetails();
            System.out.println("Available Rooms: " + inventory.getAvailability("Single") + "\n");
        }

        if (inventory.getAvailability("Double") > 0) {
            System.out.println("Double Room:");
            dbl.displayDetails();
            System.out.println("Available Rooms: " + inventory.getAvailability("Double") + "\n");
        }

        if (inventory.getAvailability("Suite") > 0) {
            System.out.println("Suite Room:");
            suite.displayDetails();
            System.out.println("Available Rooms: " + inventory.getAvailability("Suite"));
        }
    }
}

// 🔥 UC5: Reservation (Booking Request)
class Reservation {
    private String guestName;
    private String roomType;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getRoomType() {
        return roomType;
    }
}

// 🔥 UC5: Booking Queue (FIFO)
class BookingRequestQueue {
    private Queue<Reservation> queue;

    public BookingRequestQueue() {
        queue = new LinkedList<>();
    }

    // Add request
    public void addRequest(Reservation reservation) {
        queue.add(reservation);
        System.out.println("Request added: " + reservation.getGuestName() +
                " -> " + reservation.getRoomType());
    }

    // Display queue
    public void displayQueue() {
        System.out.println("\nBooking Request Queue (FIFO Order):\n");

        for (Reservation r : queue) {
            System.out.println(r.getGuestName() + " requested " + r.getRoomType());
        }
    }
}

// Main Class
public class BookMyStayApp {
    public static void main(String[] args) {

        // Rooms
        Room single = new SingleRoom();
        Room dbl = new DoubleRoom();
        Room suite = new SuiteRoom();

        // Inventory
        RoomInventory inventory = new RoomInventory();

        // Search (UC4)
        RoomSearchService search = new RoomSearchService();
        search.searchAvailableRooms(inventory, single, dbl, suite);

        // 🔥 Booking Requests (UC5)
        BookingRequestQueue bookingQueue = new BookingRequestQueue();

        // Simulating guest requests
        bookingQueue.addRequest(new Reservation("Rahul", "Single"));
        bookingQueue.addRequest(new Reservation("Arun", "Double"));
        bookingQueue.addRequest(new Reservation("Priya", "Suite"));
        bookingQueue.addRequest(new Reservation("Kiran", "Single"));

        // Display queue
        bookingQueue.displayQueue();
    }
}