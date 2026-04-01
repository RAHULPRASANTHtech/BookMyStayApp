import java.util.*;
import java.io.*;

// ================= UC9: CUSTOM EXCEPTION =================
class InvalidBookingException extends Exception {
    public InvalidBookingException(String message) {
        super(message);
    }
}

// ================= ROOM DOMAIN =================
abstract class Room implements Serializable {
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
class RoomInventory implements Serializable {
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

    public void increaseAvailability(String type) {
        inventory.put(type, getAvailability(type) + 1);
    }
}

// ================= UC5: RESERVATION =================
class Reservation implements Serializable {
    private String guestName;
    private String roomType;
    private String roomId;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getGuestName() { return guestName; }
    public String getRoomType() { return roomType; }

    public void setRoomId(String roomId) { this.roomId = roomId; }
    public String getRoomId() { return roomId; }
}

// ================= UC5: QUEUE (THREAD SAFE) =================
class BookingRequestQueue {
    private Queue<Reservation> queue = new LinkedList<>();

    public synchronized void addRequest(Reservation r) {
        queue.add(r);
        System.out.println(Thread.currentThread().getName() +
                " added: " + r.getGuestName());
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
        }

        if (inventory.getAvailability("Double") > 0) {
            System.out.println("Double Room:");
            dbl.displayDetails();
        }

        if (inventory.getAvailability("Suite") > 0) {
            System.out.println("Suite Room:");
            suite.displayDetails();
        }
    }
}

// ================= UC9: VALIDATOR =================
class BookingValidator {
    private static final Set<String> VALID =
            new HashSet<>(Arrays.asList("Single", "Double", "Suite"));

    public static void validate(Reservation r, RoomInventory inventory)
            throws InvalidBookingException {

        if (r.getGuestName() == null || r.getGuestName().isEmpty())
            throw new InvalidBookingException("Invalid guest name");

        if (!VALID.contains(r.getRoomType()))
            throw new InvalidBookingException("Invalid room type");

        if (inventory.getAvailability(r.getRoomType()) <= 0)
            throw new InvalidBookingException("No availability");
    }
}

// ================= UC8: HISTORY =================
class BookingHistory implements Serializable {
    private List<Reservation> history = new ArrayList<>();

    public void addBooking(Reservation r) {
        history.add(r);
    }

    public boolean removeBooking(String roomId) {
        return history.removeIf(r -> r.getRoomId().equals(roomId));
    }

    public List<Reservation> getAllBookings() {
        return history;
    }
}

// ================= UC8: REPORT =================
class BookingReportService {
    public void generateReport(List<Reservation> history) {
        System.out.println("\n===== REPORT =====");
        for (Reservation r : history) {
            System.out.println(r.getGuestName() + " -> " + r.getRoomType() + " | " + r.getRoomId());
        }
        System.out.println("Total: " + history.size());
    }
}

// ================= UC6 + UC11: BOOKING SERVICE =================
class BookingService {

    private RoomInventory inventory;
    private BookingHistory history;
    private Set<String> allocated = new HashSet<>();

    public BookingService(RoomInventory inventory, BookingHistory history) {
        this.inventory = inventory;
        this.history = history;
    }

    private String generateId(String type) {
        return type.charAt(0) + UUID.randomUUID().toString().substring(0,4);
    }

    public void processBookings(BookingRequestQueue queue) {

        while (true) {
            Reservation r;

            synchronized (queue) {
                if (queue.isEmpty()) break;
                r = queue.getNextRequest();
            }

            try {
                synchronized (this) {

                    BookingValidator.validate(r, inventory);

                    String id;
                    do {
                        id = generateId(r.getRoomType());
                    } while (allocated.contains(id));

                    allocated.add(id);
                    inventory.decreaseAvailability(r.getRoomType());

                    r.setRoomId(id);
                    history.addBooking(r);

                    System.out.println(Thread.currentThread().getName() +
                            " CONFIRMED: " + id);

                }

            } catch (InvalidBookingException e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }
}

// ================= UC10: CANCELLATION =================
class CancellationService {

    private RoomInventory inventory;
    private BookingHistory history;
    private Stack<String> stack = new Stack<>();

    public CancellationService(RoomInventory i, BookingHistory h) {
        inventory = i;
        history = h;
    }

    public void cancel(String id, String type) {

        if (!history.removeBooking(id)) {
            System.out.println("Cancel failed");
            return;
        }

        stack.push(id);
        inventory.increaseAvailability(type);

        System.out.println("Cancelled: " + id);
    }
}

// ================= UC7 =================
class Service {
    String name; double price;
    Service(String n, double p){name=n;price=p;}
}

// ================= UC12: PERSISTENCE =================
class PersistenceService {

    private static final String FILE = "data.dat";

    public static void save(RoomInventory i, BookingHistory h) {
        try(ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(FILE))){
            o.writeObject(i);
            o.writeObject(h);
            System.out.println("Saved!");
        } catch(Exception e){
            System.out.println("Save error");
        }
    }

    public static Object[] load() {
        try(ObjectInputStream o = new ObjectInputStream(new FileInputStream(FILE))){
            return new Object[]{o.readObject(), o.readObject()};
        } catch(Exception e){
            System.out.println("Fresh start");
            return null;
        }
    }
}

// ================= UC11 THREAD =================
class Worker implements Runnable {
    private BookingRequestQueue q;
    private BookingService s;

    public Worker(BookingRequestQueue q, BookingService s){
        this.q=q; this.s=s;
    }

    public void run(){
        s.processBookings(q);
    }
}

// ================= MAIN =================
public class BookMyStayApp {

    public static void main(String[] args) {

        System.out.println("=== BookMyStay ===");

        Object[] data = PersistenceService.load();

        RoomInventory inventory;
        BookingHistory history;

        if(data!=null){
            inventory = (RoomInventory)data[0];
            history = (BookingHistory)data[1];
        } else {
            inventory = new RoomInventory();
            history = new BookingHistory();
        }

        BookingRequestQueue q = new BookingRequestQueue();

        q.addRequest(new Reservation("Rahul","Single"));
        q.addRequest(new Reservation("Arun","Double"));
        q.addRequest(new Reservation("Priya","Suite"));

        BookingService service = new BookingService(inventory, history);

        Thread t1 = new Thread(new Worker(q, service));
        Thread t2 = new Thread(new Worker(q, service));

        t1.start(); t2.start();

        try { t1.join(); t2.join(); } catch(Exception e){}

        CancellationService c = new CancellationService(inventory, history);

        if(!history.getAllBookings().isEmpty()){
            Reservation r = history.getAllBookings().get(0);
            c.cancel(r.getRoomId(), r.getRoomType());
        }

        new BookingReportService().generateReport(history.getAllBookings());

        PersistenceService.save(inventory, history);
    }
}