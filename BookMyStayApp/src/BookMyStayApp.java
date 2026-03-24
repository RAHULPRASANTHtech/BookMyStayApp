import java.util.*;

// ================= ROOM DOMAIN =================
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

// ================= UC3: INVENTORY =================
class RoomInventory {
    private HashMap<String, Integer> inventory = new HashMap<>();

    public RoomInventory() {
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

// ================= UC5: RESERVATION =================
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

// ================= UC5: QUEUE =================
class BookingRequestQueue {
    private Queue<Reservation> queue = new LinkedList<>();

    public void addRequest(Reservation r) {
        queue.add(r);
        System.out.println("Request Added: " + r.getGuestName() + " -> " + r.getRoomType());
    }

    public Reservation getNextRequest() {
        return queue.poll();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}

// ================= UC4: SEARCH =================
class RoomSearchService {
    public void searchAvailableRooms(RoomInventory inventory,
                                     Room single, Room dbl, Room suite) {

        System.out.println("\nAvailable Rooms:\n");

        if (inventory.getAvailability("Single") > 0) {
            System.out.println("Single Room:");
            single.displayDetails();
            System.out.println("Available: " + inventory.getAvailability("Single") + "\n");
        }

        if (inventory.getAvailability("Double") > 0) {
            System.out.println("Double Room:");
            dbl.displayDetails();
            System.out.println("Available: " + inventory.getAvailability("Double") + "\n");
        }

        if (inventory.getAvailability("Suite") > 0) {
            System.out.println("Suite Room:");
            suite.displayDetails();
            System.out.println("Available: " + inventory.getAvailability("Suite") + "\n");
        }
    }
}

// ================= UC6: BOOKING SERVICE =================
class BookingService {

    private RoomInventory inventory;
    private Set<String> allocatedRoomIds = new HashSet<>();
    private HashMap<String, Set<String>> allocationMap = new HashMap<>();

    public BookingService(RoomInventory inventory) {
        this.inventory = inventory;

        allocationMap.put("Single", new HashSet<>());
        allocationMap.put("Double", new HashSet<>());
        allocationMap.put("Suite", new HashSet<>());
    }

    private String generateRoomId(String type) {
        return type.substring(0,1).toUpperCase() + UUID.randomUUID().toString().substring(0,4);
    }

    public List<String> processBookings(BookingRequestQueue queue) {

        List<String> confirmedIds = new ArrayList<>();

        System.out.println("\nProcessing Bookings...\n");

        while (!queue.isEmpty()) {

            Reservation r = queue.getNextRequest();
            String type = r.getRoomType();

            if (inventory.getAvailability(type) > 0) {

                String roomId;

                do {
                    roomId = generateRoomId(type);
                } while (allocatedRoomIds.contains(roomId));

                allocatedRoomIds.add(roomId);
                allocationMap.get(type).add(roomId);
                inventory.decreaseAvailability(type);

                confirmedIds.add(roomId);

                System.out.println("Booking Confirmed: "
                        + r.getGuestName()
                        + " -> " + type
                        + " | Room ID: " + roomId);

            } else {
                System.out.println("Booking Failed: "
                        + r.getGuestName()
                        + " -> " + type);
            }
        }

        return confirmedIds;
    }
}

// ================= UC7: SERVICE =================
class Service {
    private String name;
    private double price;

    public Service(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public String getName() { return name; }
    public double getPrice() { return price; }
}

// ================= UC7: SERVICE MANAGER =================
class AddOnServiceManager {

    private HashMap<String, List<Service>> serviceMap = new HashMap<>();

    public void addService(String reservationId, Service service) {
        serviceMap.putIfAbsent(reservationId, new ArrayList<>());
        serviceMap.get(reservationId).add(service);

        System.out.println("Service Added: " + service.getName() + " to " + reservationId);
    }

    public double calculateTotalCost(String reservationId) {
        double total = 0;
        List<Service> services = serviceMap.getOrDefault(reservationId, new ArrayList<>());

        for (Service s : services) {
            total += s.getPrice();
        }

        return total;
    }

    public void displayServices(String reservationId) {

        System.out.println("\nServices for Reservation: " + reservationId);

        List<Service> services = serviceMap.get(reservationId);

        if (services == null || services.isEmpty()) {
            System.out.println("No services selected.");
            return;
        }

        for (Service s : services) {
            System.out.println("- " + s.getName() + " : " + s.getPrice());
        }

        System.out.println("Total Add-On Cost: " + calculateTotalCost(reservationId));
    }
}

// ================= MAIN =================
public class BookMyStayApp {
    public static void main(String[] args) {

        System.out.println("===== Welcome to Book My Stay App =====");

        // Rooms
        Room single = new SingleRoom();
        Room dbl = new DoubleRoom();
        Room suite = new SuiteRoom();

        // Inventory
        RoomInventory inventory = new RoomInventory();

        // UC4: Search
        RoomSearchService search = new RoomSearchService();
        search.searchAvailableRooms(inventory, single, dbl, suite);

        // UC5: Queue
        BookingRequestQueue queue = new BookingRequestQueue();
        queue.addRequest(new Reservation("Rahul", "Single"));
        queue.addRequest(new Reservation("Arun", "Double"));
        queue.addRequest(new Reservation("Priya", "Suite"));
        queue.addRequest(new Reservation("Kiran", "Single"));

        // UC6: Booking
        BookingService bookingService = new BookingService(inventory);
        List<String> reservationIds = bookingService.processBookings(queue);

        // UC7: Add-On Services
        AddOnServiceManager serviceManager = new AddOnServiceManager();

        Service wifi = new Service("WiFi", 200);
        Service breakfast = new Service("Breakfast", 300);
        Service spa = new Service("Spa", 800);

        if (!reservationIds.isEmpty()) {
            String id = reservationIds.get(0);

            serviceManager.addService(id, wifi);
            serviceManager.addService(id, breakfast);
            serviceManager.addService(id, spa);

            serviceManager.displayServices(id);
        }
    }
}