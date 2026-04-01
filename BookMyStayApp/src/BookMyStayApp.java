import java.util.HashMap;

// ================= UC3: ROOM INVENTORY =================
class RoomInventory {

    private HashMap<String, Integer> inventory;

    // Constructor → initialize inventory
    public RoomInventory() {
        inventory = new HashMap<>();

        inventory.put("Single", 5);
        inventory.put("Double", 3);
        inventory.put("Suite", 2);
    }

    // Get availability
    public int getAvailability(String roomType) {
        return inventory.getOrDefault(roomType, 0);
    }

    // Update availability
    public void updateAvailability(String roomType, int count) {
        inventory.put(roomType, count);
    }

    // Display inventory
    public void displayInventory() {
        System.out.println("===== Room Inventory =====");

        for (String type : inventory.keySet()) {
            System.out.println(type + " Rooms Available: " + inventory.get(type));
        }
    }
}

// ================= MAIN =================
public class BookMyStayApp {

    public static void main(String[] args) {

        System.out.println("=== UC3: Centralized Inventory ===\n");

        // Initialize inventory
        RoomInventory inventory = new RoomInventory();

        // Display current inventory
        inventory.displayInventory();

        // Example: update inventory
        System.out.println("\nUpdating Single room availability...\n");

        inventory.updateAvailability("Single", 4);

        // Display again
        inventory.displayInventory();

        // Example: check availability
        System.out.println("\nAvailable Double Rooms: "
                + inventory.getAvailability("Double"));
    }
}