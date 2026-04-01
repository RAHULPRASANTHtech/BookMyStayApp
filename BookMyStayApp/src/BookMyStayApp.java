import java.util.*;

// ================= UC9: CUSTOM EXCEPTION =================
class InvalidBookingException extends Exception {
    public InvalidBookingException(String message) {
        super(message);
    }
}

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

    public void decreaseAvailability(String type) throws InvalidBookingException {
        int current = getAvailability(type);

        if (current <= 0) {
            throw new InvalidBookingException("Cannot reduce availability below zero for " + type);
        }

        inventory.put(type, current - 1);
    }

    // 🔥 UC10: increase availability (rollback)
    public void increaseAvailability(String type) {
        inventory.put(type, getAvailability(type) + 1);
    }
}

// ================= UC5: RESERVATION =================
class Reservation {
    private String guestName;
    private String roomType;
    private String roomId;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getGuestName() { return guestName; }
    public String getRoomType() { return roomType; }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomId() { return roomId; }
}

// ================= UC5: QUEUE =================
class BookingRequestQueue {
    private Queue<Reservation> queue = new LinkedList<>();

    public synchronized void addRequest(Reservation r) {
        queue.add(r);
        System.out.println(Thread.currentThread().getName() +
                " added: " + r.getGuestName() + " -> " + r.getRoomType());
    }

    public synchronized Reservation getNextRequest() {
        return queue.poll();
    }

    public synchronized boolean isEmpty() {
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

// ================= UC9: VALIDATOR =================
class BookingValidator {

    private static final Set<String> VALID_TYPES =
            new HashSet<>(Arrays.asList("Single", "Double", "Suite"));

    public static void validate(Reservation r, RoomInventory inventory)
            throws InvalidBookingException {

        if (r.getGuestName() == null || r.getGuestName().isEmpty()) {
            throw new InvalidBookingException("Guest name cannot be empty");
        }

        if (!VALID_TYPES.contains(r.getRoomType())) {
            throw new InvalidBookingException("Invalid room type: " + r.getRoomType());
        }

        if (inventory.getAvailability(r.getRoomType()) <= 0) {
            throw new InvalidBookingException("No availability for " + r.getRoomType());
        }
    }
}

// ================= UC8: BOOKING HISTORY =================
class BookingHistory {
    private List<Reservation> history = new ArrayList<>();

    public void addBooking(Reservation r) {
        history.add(r);
    }

    public List<Reservation> getAllBookings() {
        return history;
    }

    // 🔥 UC10: remove booking
    public boolean removeBooking(String roomId) {
        Iterator<Reservation> it = history.iterator();

        while (it.hasNext()) {
            Reservation r = it.next();

            if (r.getRoomId().equals(roomId)) {
                it.remove();
                return true;
            }
        }

        return false;
    }
}

// ================= UC8: REPORT =================
class BookingReportService {
    public void generateReport(List<Reservation> history) {

        System.out.println("\n===== BOOKING REPORT =====\n");

        for (Reservation r : history) {
            System.out.println("Guest: " + r.getGuestName()
                    + " | Type: " + r.getRoomType()
                    + " | Room ID: " + r.getRoomId());
        }

        System.out.println("\nTotal Bookings: " + history.size());
    }
}

// ================= UC6: BOOKING SERVICE =================
class BookingService {

    private RoomInventory inventory;
    private BookingHistory history;
    private Set<String> allocatedRoomIds = new HashSet<>();

    public BookingService(RoomInventory inventory, BookingHistory history) {
        this.inventory = inventory;
        this.history = history;
    }

    private String generateRoomId(String type) {
        return type.substring(0,1) + UUID.randomUUID().toString().substring(0,4);
    }

    public List<Reservation> processBookings(BookingRequestQueue queue) {

        List<Reservation> confirmed = new ArrayList<>();

        while (true) {

            Reservation r;

            synchronized (queue) {
                if (queue.isEmpty()) break;
                r = queue.getNextRequest();
            }

            try {
                synchronized (this) { // 🔥 CRITICAL SECTION

                    BookingValidator.validate(r, inventory);

                    String roomId;

                    do {
                        roomId = generateRoomId(r.getRoomType());
                    } while (allocatedRoomIds.contains(roomId));

                    allocatedRoomIds.add(roomId);

                    inventory.decreaseAvailability(r.getRoomType());

                    r.setRoomId(roomId);
                    confirmed.add(r);
                    history.addBooking(r);

                    System.out.println(Thread.currentThread().getName() +
                            " CONFIRMED: " + r.getGuestName() +
                            " -> " + r.getRoomType() +
                            " | ID: " + roomId);
                }

            } catch (InvalidBookingException e) {
                System.out.println(Thread.currentThread().getName() +
                        " ERROR: " + e.getMessage());
            }
        }

        return confirmed;
    }
}

// ================= UC10: CANCELLATION SERVICE =================
class CancellationService {

    private RoomInventory inventory;
    private BookingHistory history;

    private Stack<String> rollbackStack = new Stack<>();

    public CancellationService(RoomInventory inventory, BookingHistory history) {
        this.inventory = inventory;
        this.history = history;
    }
    class ConcurrentBookingProcessor implements Runnable {

        private BookingRequestQueue queue;
        private BookingService bookingService;

        public ConcurrentBookingProcessor(BookingRequestQueue queue, BookingService bookingService) {
            this.queue = queue;
            this.bookingService = bookingService;
        }

        @Override
        public void run() {
            // Each thread processes bookings safely
            synchronized (bookingService) {
                bookingService.processBookings(queue);
            }
        }
    }
    public void cancelBooking(String roomId, String roomType) {

        System.out.println("\nAttempting Cancellation: " + roomId);

        boolean exists = history.removeBooking(roomId);

        if (!exists) {
            System.out.println("Cancellation Failed: Booking not found.");
            return;
        }

        rollbackStack.push(roomId);

        inventory.increaseAvailability(roomType);

        System.out.println("Cancellation Successful: " + roomId);
        System.out.println("Rollback Stack: " + rollbackStack);
    }
}

// ================= MAIN =================
public class BookMyStayApp {
    public static void main(String[] args) {

        System.out.println("===== Book My Stay =====");

        RoomInventory inventory = new RoomInventory();

        RoomSearchService search = new RoomSearchService();
        search.searchAvailableRooms(inventory,
                new SingleRoom(), new DoubleRoom(), new SuiteRoom());

// Shared queue
        BookingRequestQueue queue = new BookingRequestQueue();

// Simulate multiple guests
        queue.addRequest(new Reservation("Rahul", "Single"));
        queue.addRequest(new Reservation("Arun", "Double"));
        queue.addRequest(new Reservation("Priya", "Suite"));
        queue.addRequest(new Reservation("Kiran", "Single"));
        queue.addRequest(new Reservation("John", "Double"));

// Shared services
        BookingHistory history = new BookingHistory();
        BookingService bookingService = new BookingService(inventory, history);

// 🔥 MULTIPLE THREADS
        Thread t1 = new Thread(new ConcurrentBookingProcessor(queue, bookingService), "Thread-1");
        Thread t2 = new Thread(new ConcurrentBookingProcessor(queue, bookingService), "Thread-2");
        Thread t3 = new Thread(new ConcurrentBookingProcessor(queue, bookingService), "Thread-3");

// Start threads
        t1.start();
        t2.start();
        t3.start();

// Wait for completion
        try {
            t1.join();
            t2.join();
            t3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<Reservation> confirmed = service.processBookings(queue);

        // 🔥 UC10: Cancellation
        CancellationService cancelService = new CancellationService(inventory, history);

        if (!confirmed.isEmpty()) {
            Reservation r = confirmed.get(confirmed.size() - 1);
            cancelService.cancelBooking(r.getRoomId(), r.getRoomType());
        }

        // invalid cancellation
        cancelService.cancelBooking("INVALID", "Single");

        // Report
        BookingReportService report = new BookingReportService();
        report.generateReport(history.getAllBookings());
    }
}