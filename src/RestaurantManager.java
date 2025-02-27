import Entities.DeliveryFee;
import Entities.Item;
import Entities.Restaurant;
import Entities.UserOrder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Scanner;

public class RestaurantManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/mash_mammad";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1234567890qwertyuiop";

    private static final int PAGE_SIZE = 5;
    private static List<Long> openDayIds = new ArrayList<>();

    public static void manageRestaurants(String username, Scanner scanner) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            int page = 0;
            boolean hasMoreRestaurants = true;
            List<Restaurant> restaurantList = new ArrayList<>();

            while (hasMoreRestaurants) {
                restaurantList.clear();

                CallableStatement stmt = conn.prepareCall("{CALL GetOwnedRestaurants(?, ?, ?)}");
                stmt.setString(1, username);
                stmt.setInt(2, page * PAGE_SIZE);
                stmt.setInt(3, PAGE_SIZE);
                ResultSet rs = stmt.executeQuery();

                int count = 0;
                System.out.println("\n--- Your Owned Restaurants (Page " + (page + 1) + ") ---");

                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String city = rs.getString("city");
                    String address = rs.getString("address");
                    float minPurchase = rs.getFloat("minPurchase");

                    Restaurant restaurant = new Restaurant(id, name, city, address, minPurchase);
                    restaurantList.add(restaurant);

                    count++;
                    System.out.println(count + ". Name: " + name);
                    System.out.println("   City: " + city);
                    System.out.println("   Address: " + address);
                    System.out.println("   Min Purchase: " + minPurchase);
                    System.out.println();
                }

                if (count == 0) {
                    System.out.println("No restaurants found.");
                    return;
                }

                System.out.println("Options:");
                System.out.println("1. Previous Page");
                System.out.println("2. Next Page");
                System.out.println("3. Manage a Restaurant");
                System.out.println("4. Exit");
                System.out.print("Choose an option: ");

                int option = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (option) {
                    case 1:
                        if (page > 0) page--;
                        else System.out.println("Already on the first page.");
                        break;
                    case 2:
                        if (count == PAGE_SIZE) page++;
                        else System.out.println("No more pages available.");
                        break;
                    case 3:
                        System.out.print("Enter the restaurant number you want to manage: ");
                        int restaurantIndex = scanner.nextInt();
                        scanner.nextLine(); // Consume newline

                        if (restaurantIndex < 1 || restaurantIndex > restaurantList.size()) {
                            System.out.println("Invalid selection. Try again.");
                        } else {
                            Restaurant selectedRestaurant = restaurantList.get(restaurantIndex - 1);
                            System.out.println(selectedRestaurant.getName());
                            manageRestaurantOptions(conn, selectedRestaurant,scanner);
                        }
                        break;
                    case 4:
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid option. Try again.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }




    public static void manageRestaurantSettings(Connection conn, Restaurant restaurant) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n--- Restaurant Settings: " + restaurant.getName() + " ---");
        System.out.print("Enter new name (or press Enter to keep current: " + restaurant.getName() + "): ");
        String newName = scanner.nextLine().trim();
        if (newName.isEmpty()) newName = restaurant.getName();

        System.out.print("Enter image profile path (or press Enter to keep current): ");
        String imagePath = scanner.nextLine().trim();
        byte[] newImage = null;
        if (!imagePath.isEmpty()) {
            try {
                File imageFile = new File(imagePath);
                FileInputStream fis = new FileInputStream(imageFile);
                newImage = new byte[(int) imageFile.length()];
                fis.read(newImage);
                fis.close();
            } catch (IOException e) {
                System.out.println("Error reading image file: " + e.getMessage());
                return;
            }
        }

        System.out.print("Enter city (or press Enter to keep current: " + restaurant.getCity() + "): ");
        String newCity = scanner.nextLine().trim();
        if (newCity.isEmpty()) newCity = restaurant.getCity();

        System.out.print("Enter address detail (or press Enter to keep current: " + restaurant.getAddress() + "): ");
        String newAddress = scanner.nextLine().trim();
        if (newAddress.isEmpty()) newAddress = restaurant.getAddress();

        System.out.print("Enter map location (or press Enter to keep current: " + restaurant.getMapLocation() + "): ");
        String newMapLocation = scanner.nextLine().trim();
        if (newMapLocation.isEmpty()) newMapLocation = restaurant.getMapLocation();

        try (CallableStatement stmt = conn.prepareCall("{CALL UpdateRestaurantSettings(?, ?, ?, ?, ?, ?)}")) {
            stmt.setInt(1, restaurant.getId());
            stmt.setString(2, newName);
            if (newImage != null) {
                stmt.setBytes(3, newImage);
            } else {
                stmt.setBytes(3, restaurant.getImage());
            }
            stmt.setString(4, newCity);
            stmt.setString(5, newAddress);
            stmt.setString(6, newMapLocation);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                restaurant.setName(newName);
                if (newImage != null) {
                    restaurant.setImage(newImage);
                }
                restaurant.setCity(newCity);
                restaurant.setAddress(newAddress);
                restaurant.setMapLocation(newMapLocation);
                System.out.println("Restaurant settings updated successfully.");
            } else {
                System.out.println("Failed to update restaurant settings.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void manageRestaurantOptions(Connection conn, Restaurant restaurant,Scanner scanner) {

        while (true) {
            System.out.println("\n--- Manage Your Restaurant: " + restaurant.getName() + " ---");
            System.out.println("1. Settings");
            System.out.println("2. Manage Open Days & Hours");
            System.out.println("3. Manage Items");
            System.out.println("4. Delivery fee menu");
            System.out.println("5. Pending orders menu");
            System.out.println("6. Orders history menu");
            System.out.println("7. Exit");
            System.out.print("Choose an option: ");
            int option = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            if (option == 1) {
                manageRestaurantSettings(conn, restaurant);
            } else if (option == 2) {
                ManageOpenDays(conn, restaurant, scanner);
            } else if (option == 3) {
                handleRestaurantMenu(conn,restaurant,scanner);
            } else if (option == 4) {
                handleDeliveryFeeMenu(conn,restaurant,scanner);
                break;
            } else if (option == 5) {
                handleRestaurantPendingOrdersMenu(conn,restaurant.getId(),scanner);
                break;
            } else if (option == 6) {
                handleRestaurantOrdersHistoryMenu(conn,restaurant.getId(),scanner);
                break;
            }else if (option == 7) {
                System.out.println("Exiting management menu.");
                break;
            }else {
                System.out.println("Invalid option. Please try again.");
            }
        }
    }

    public static List<Long> displayOpenDaysForRestaurant(Connection conn, long restaurantId) {
        openDayIds.clear(); // Ensure old data is removed before adding new ones
        try (CallableStatement stmt = conn.prepareCall("{CALL GetOpenDaysForRestaurant(?, ?)}")) {
            stmt.setLong(1, restaurantId);
            stmt.registerOutParameter(2, Types.VARCHAR);
            stmt.execute();

            String openDayDataStr = stmt.getString(2);
            if (openDayDataStr != null && !openDayDataStr.isEmpty()) {
                String[] entries = openDayDataStr.split(",");

                System.out.println("\n--- Open Days for Restaurant ID: " + restaurantId + " ---");
                int count = 0;
                for (String entry : entries) {
                    openDayIds.add(Long.parseLong(entry));
                    CallableStatement stmt1 = conn.prepareCall("{CALL GetOpenDayName(?, ?)}");
                    stmt1.setLong(1, Long.parseLong(entry));  // Set the openDayId input
                    stmt1.registerOutParameter(2, Types.VARCHAR);  // Register the output parameter
                    stmt1.execute();

                    String openDayName = stmt1.getString(2);
                    System.out.println( openDayName);

                    count++;

                }

                if (count == 0) {
                    System.out.println("No open days found.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return openDayIds;
    }


    public static void ManageOpenDays(Connection conn, Restaurant restaurant, Scanner scanner) {

        System.out.println("\n--- Open Days for " + restaurant.getName() + " ---");
        displayOpenDaysForRestaurant(conn,restaurant.getId() );



        System.out.println("\nOptions:");
        System.out.println("1. Add Open Hour to an Open Day");
        System.out.println("2. Add an Open Day");
        System.out.println("3. Exit");
        System.out.print("Choose an option: ");

        int option = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        switch (option) {
            case 1:
                if (openDayIds.isEmpty()) {
                    System.out.println("No open days available to add an open hour.");
                    break;
                }
                System.out.print("Select an open day number: ");
                int selectedIndex = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                if (selectedIndex < 1 || selectedIndex > openDayIds.size()) {
                    System.out.println("Invalid selection. Try again.");
                } else {
                    addOpenHour(conn, openDayIds.get(selectedIndex - 1), scanner);
                }
                break;
            case 2:
                Admin.addOpenDayAndHours(restaurant.getId());
                break;
            case 3:
                System.out.println("Exiting...");
                return;
            default:
                System.out.println("Invalid option. Try again.");
        }
    }

    public static void addOpenHour(Connection conn, long openDayId, Scanner scanner) {
        System.out.print("Enter open hour (HH:MM:SS format): ");
        String openHour = scanner.nextLine();

        System.out.print("Enter close hour (HH:MM:SS format): ");
        String closeHour = scanner.nextLine();

        try (CallableStatement stmt = conn.prepareCall("{CALL AddOpenHour(?, ?, ?, ?)}")) {
            stmt.setString(1, openHour);
            stmt.setString(2, closeHour);
            stmt.setLong(3, openDayId);
            stmt.registerOutParameter(4, Types.BOOLEAN);

            stmt.execute();
            boolean isSuccess = stmt.getBoolean(4);

            if (isSuccess) {
                System.out.println("Open hour added successfully.");
            } else {
                System.out.println("Failed to add open hour.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



        public static void addItem( Connection conn,Scanner scanner,Restaurant restaurant) throws SQLException {



            System.out.println("Enter item title: ");
            String title = scanner.nextLine();

            System.out.println("Enter item price: ");
            float price = scanner.nextFloat();
            scanner.nextLine(); // Consume newline

            System.out.println("Enter item ingredients (comma-separated): ");
            String ingredient = scanner.nextLine();

            byte[] image = null; // Placeholder for image handling
            //!!!!!!!!!!!!!!!!!!


            String sql = "{CALL AddItem(?, ?, ?, ?, ?, ?, ?)}";
            CallableStatement stmt = conn.prepareCall(sql);

            stmt.setLong(1, restaurant.getId());
            stmt.setString(2, title);
            stmt.setBytes(3, image);
            stmt.setFloat(4, price);
            stmt.setString(5, ingredient);
            stmt.registerOutParameter(6, Types.BIGINT);  // itemId output
            stmt.registerOutParameter(7, Types.BOOLEAN); // isSuccess output

            stmt.execute();

            boolean isSuccess = stmt.getBoolean(7);
            if (isSuccess) {
                Long itemId = stmt.getLong(6);
                System.out.println("Item added successfully with ID: " + itemId);
            } else {
                System.out.println("Failed to insert item.");
            }
        }


    public static void fetchAndDisplayFoods(int restaurantId, int page, List<Item> currentItems) {
        int pageSize = PAGE_SIZE;
        int offset = (page - 1) * pageSize;
        currentItems.clear();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            CallableStatement stmt = conn.prepareCall("{CALL GetPaginatedFoods(?, ?, ?)}");
            stmt.setInt(1, restaurantId);
            stmt.setInt(2, pageSize);
            stmt.setInt(3, offset);
            ResultSet rs = stmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                count++;
                Item item = new Item(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getFloat("price"),
                        rs.getString("ingredient"),
                        rs.getString("category")
                );
                currentItems.add(item);
                System.out.println(count + ". Food Name: " + item.getName());
                System.out.println("   Price: " + item.getPrice());
                System.out.println("   Ingredients: " + item.getIngredients());
                System.out.println("   Category: " + item.getCategory());
            }

            if (count == 0) {
                System.out.println("No foods available for this restaurant.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteItemById(Item item,Connection conn) {
        long itemId = item.getId();
        try {
            CallableStatement stmt = conn.prepareCall("{CALL DeleteItemById(?, ?)}");

            stmt.setLong(1, itemId);
            stmt.registerOutParameter(2, Types.BOOLEAN);
            stmt.execute();

            boolean isSuccess = stmt.getBoolean(2);
            if (isSuccess) {
                System.out.println("Item deleted successfully.");
            } else {
                System.out.println("Item not found or already deleted.");
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateItemPrice(Scanner scanner,Connection conn,Item item) {

        long itemId = item.getId();

        System.out.println("Enter the new price: ");
        float newPrice = scanner.nextFloat();

        try {
            CallableStatement stmt = conn.prepareCall("{CALL UpdateItemPrice(?, ?, ?)}");

            stmt.setLong(1, itemId);
            stmt.setFloat(2, newPrice);
            stmt.registerOutParameter(3, Types.BOOLEAN);

            stmt.execute();

            boolean isSuccess = stmt.getBoolean(3);
            if (isSuccess) {
                System.out.println("Item price updated successfully.");
            } else {
                System.out.println("Item not found or already deleted.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addOrUpdateDeliveryFee(Connection conn, Scanner scanner, long restaurantId) {
        System.out.println("Enter the max distance for delivery fee: ");
        int maxDistance = scanner.nextInt();

        System.out.println("Enter the delivery cost: ");
        float cost = scanner.nextFloat();

        try {
            CallableStatement stmt = conn.prepareCall("{CALL AddOrUpdateDeliveryFee(?, ?, ?, ?)}");

            stmt.setLong(1, restaurantId);
            stmt.setInt(2, maxDistance);
            stmt.setFloat(3, cost);
            stmt.registerOutParameter(4, Types.BOOLEAN);

            stmt.execute();

            boolean isSuccess = stmt.getBoolean(4);
            if (isSuccess) {
                System.out.println("Delivery fee updated successfully.");
            } else {
                System.out.println("Failed to update delivery fee.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void fetchAndDisplayDeliveryFees(Connection conn, int restaurantId, int page, List<DeliveryFee> currentFees) {
        int pageSize = PAGE_SIZE;
        int offset = (page - 1) * pageSize;
        currentFees.clear();

        try {
            CallableStatement stmt = conn.prepareCall("{CALL GetPaginatedDeliveryFees(?, ?, ?)}");
            stmt.setInt(1, restaurantId);
            stmt.setInt(2, pageSize);
            stmt.setInt(3, offset);
            ResultSet rs = stmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                int id = rs.getInt("id");
                int maxDistance = rs.getInt("maxDistance");
                float cost = rs.getFloat("cost");

                DeliveryFee fee = new DeliveryFee(id, maxDistance, cost);
                currentFees.add(fee);

                count++;
                System.out.println(count + ". ID: " + id);
                System.out.println("   Max Distance: " + maxDistance + " km");
                System.out.println("   Cost: $" + cost);
            }

            if (count == 0) {
                System.out.println("No delivery fees found for this restaurant.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void deleteDeliveryFee(Connection conn, int feeId) {
        try {
            CallableStatement stmt = conn.prepareCall("{CALL DeleteDeliveryFee(?)}");
            stmt.setInt(1, feeId);
            stmt.execute();
            System.out.println("Delivery fee deleted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void handleDeliveryFeeMenu(Connection conn, Restaurant restaurant, Scanner scanner) {
        int page = 1;
        List<DeliveryFee> currentFees = new ArrayList<>();

        while (true) {
            System.out.println("\n--- Delivery Fees for Restaurant: " + restaurant.getName() + " (Page " + page + ") ---");
            fetchAndDisplayDeliveryFees(conn, restaurant.getId(), page, currentFees);

            System.out.println("\nOptions:");
            System.out.println("1. Next Page");
            System.out.println("2. Previous Page");
            System.out.println("3. Add Delivery Fee");
            System.out.println("4. Delete Delivery Fee");
            System.out.println("5. Exit");
            System.out.print("Select an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    page++;
                    break;
                case 2:
                    if (page > 1) {
                        page--;
                    } else {
                        System.out.println("You are already on the first page.");
                    }
                    break;
                case 3:
                    addOrUpdateDeliveryFee(conn, scanner, restaurant.getId());
                    break;
                case 4:
                    System.out.print("Enter the index of the delivery fee to delete: ");
                    int feeIndex = scanner.nextInt();
                    if (feeIndex > 0 && feeIndex <= currentFees.size()) {
                        DeliveryFee selectedFee = currentFees.get(feeIndex - 1);
                        deleteDeliveryFee(conn, selectedFee.getId());
                    } else {
                        System.out.println("Invalid index. Please try again.");
                    }
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }
    public static void fetchAndDisplayOrdersHistoryByRestaurant(Connection conn, int restaurantId, int page, List<UserOrder> currentOrders) {
        int pageSize = PAGE_SIZE;
        int offset = (page - 1) * pageSize;
        currentOrders.clear();

        try {
            // Call the stored procedure to get paginated orders for the restaurant based on the items
            CallableStatement stmt = conn.prepareCall("{CALL GetPaginatedOrdersByRestaurantPath(?, ?, ?)}");
            stmt.setInt(1, restaurantId);
            stmt.setInt(2, pageSize);
            stmt.setInt(3, offset);
            ResultSet rs = stmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                count++;
                UserOrder order = new UserOrder(
                        rs.getInt("id"),
                        rs.getInt("addressId"),
                        rs.getTimestamp("orderTime"),
                        rs.getBoolean("isPaid"),
                        rs.getString("orderStatus"),
                        rs.getBoolean("isDeleted")
                );
                currentOrders.add(order);

                System.out.println(count + ". Order ID: " + order.getId());
                System.out.println("   Address ID: " + order.getAddressId());
                System.out.println("   Order Time: " + order.getOrderTime());
                System.out.println("   Paid: " + (order.isPaid() ? "Yes" : "No"));
                System.out.println("   Status: " + order.getOrderStatus());
                System.out.println("   Deleted: " + (order.isDeleted() ? "Yes" : "No"));
            }

            if (count == 0) {
                System.out.println("No orders for this restaurant.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void fetchAndDisplayPendingOrdersByRestaurant(Connection conn, int restaurantId, int page, List<UserOrder> currentOrders) {
        int pageSize = PAGE_SIZE;
        int offset = (page - 1) * pageSize;
        currentOrders.clear();

        try {
            // Call the stored procedure to get paginated orders for the restaurant based on the items
            CallableStatement stmt = conn.prepareCall("{CALL GetPaginatedPendingOrdersByRestaurantPath(?, ?, ?)}");
            stmt.setInt(1, restaurantId);
            stmt.setInt(2, pageSize);
            stmt.setInt(3, offset);
            ResultSet rs = stmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                count++;
                UserOrder order = new UserOrder(
                        rs.getInt("id"),
                        rs.getInt("addressId"),
                        rs.getTimestamp("orderTime"),
                        rs.getBoolean("isPaid"),
                        rs.getString("orderStatus"),
                        rs.getBoolean("isDeleted")
                );
                currentOrders.add(order);

                System.out.println(count + ". Order ID: " + order.getId());
                System.out.println("   Address ID: " + order.getAddressId());
                System.out.println("   Order Time: " + order.getOrderTime());
                System.out.println("   Paid: " + (order.isPaid() ? "Yes" : "No"));
                System.out.println("   Status: " + order.getOrderStatus());
                System.out.println("   Deleted: " + (order.isDeleted() ? "Yes" : "No"));
            }

            if (count == 0) {
                System.out.println("No orders for this restaurant.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void updateOrderStatus(Connection conn, UserOrder order, String newStatus) {
        try {
            String query = "UPDATE userOrder SET orderStatus = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, newStatus);
                stmt.setInt(2, order.getId());

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Order status updated to: " + newStatus);
                } else {
                    System.out.println("No order found with the given ID or order is not paid.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private static void handleRestaurantPendingOrdersMenu(Connection conn, int restaurantId, Scanner scanner) {
        int page = 1;
        List<UserOrder> currentOrders = new ArrayList<>();

        while (true) {
            System.out.println("\n--- Orders for Restaurant ID: " + restaurantId + " (Page " + page + ") ---");
            fetchAndDisplayPendingOrdersByRestaurant(conn, restaurantId, page, currentOrders);

            System.out.println("\nOptions:");
            System.out.println("1. Next Page");
            System.out.println("2. Previous Page");
            System.out.println("3. Change Order Status");
            System.out.println("4. Exit");
            System.out.print("Select an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    page++;
                    break;
                case 2:
                    if (page > 1) {
                        page--;
                    } else {
                        System.out.println("You are already on the first page.");
                    }
                    break;
                case 3:
                    System.out.print("Enter the Order index to change status: ");
                    int orderId = scanner.nextInt();

                    // Find the order item by orderId - 1 (adjusting for zero-based index)
                    if (orderId > 0 && orderId <= currentOrders.size()) {
                        UserOrder selectedOrder = currentOrders.get(orderId - 1);

                        System.out.println("Current Status: " + selectedOrder.getOrderStatus());
                        System.out.println("Enter the new status:");
                        System.out.println("1. Approved");
                        System.out.println("2. Rejected");
                        int statusChoice = scanner.nextInt();

                        // Determine new status based on user input
                        String newStatus = "";
                        if (statusChoice == 1) {
                            newStatus = "Approved";
                        } else if (statusChoice == 2) {
                            newStatus = "Rejected";
                        } else {
                            System.out.println("Invalid choice. Please enter 1 for Approved or 2 for Rejected.");
                            break;
                        }

                        // Call method to update the order status
                        updateOrderStatus(conn, selectedOrder, newStatus);
                    } else {
                        System.out.println("Invalid Order ID.");
                    }
                    break;

                case 4:
                    return;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }
    private static void handleRestaurantOrdersHistoryMenu(Connection conn, int restaurantId, Scanner scanner) {
        int page = 1;
        List<UserOrder> currentOrders = new ArrayList<>();

        while (true) {
            System.out.println("\n--- Orders for Restaurant ID: " + restaurantId + " (Page " + page + ") ---");
            fetchAndDisplayOrdersHistoryByRestaurant(conn, restaurantId, page, currentOrders);

            System.out.println("\nOptions:");
            System.out.println("1. Next Page");
            System.out.println("2. Previous Page");
            System.out.println("3. Exit");
            System.out.print("Select an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    page++;
                    break;
                case 2:
                    if (page > 1) {
                        page--;
                    } else {
                        System.out.println("You are already on the first page.");
                    }
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    private static void handleRestaurantMenu(Connection conn,Restaurant restaurant,Scanner scanner) {
        int page = 1;

        List<Item> currentItems = new ArrayList<>();

        while (true) {
            System.out.println("\n--- Menu for Restaurant: " + restaurant.getName() + " (Page " + page + ") ---");
            fetchAndDisplayFoods(restaurant.getId(), page, currentItems);

            System.out.println("\nOptions:");
            System.out.println("1. Next Page");
            System.out.println("2. Previous Page");
            System.out.println("3. Delete item");
            System.out.println("4. Add item");
            System.out.println("5. Change item price");
            System.out.println("6. Exit page");
            System.out.print("Select an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    page++;
                    break;
                case 2:
                    if (page > 1) {
                        page--;
                    } else {
                        System.out.println("You are already on the first page.");
                    }
                    break;
                case 3:
                    System.out.print("Enter the index of the food to delete: ");
                    int itemIndex = scanner.nextInt();
                    if (itemIndex > 0 && itemIndex <= currentItems.size()) {
                        Item selectedItem = currentItems.get(itemIndex - 1);
                        deleteItemById(selectedItem,conn);
                    } else {
                        System.out.println("Invalid index. Please try again.");
                    }
                    break;
                case 4:
                    try{
                        addItem(conn,scanner,restaurant);}
                    catch (SQLException e) {
                        e.printStackTrace();
                    }
                    break;
                case 5:
                    System.out.print("Enter the index of the food to change price: ");
                    itemIndex = scanner.nextInt();
                    if (itemIndex > 0 && itemIndex <= currentItems.size()) {
                        Item selectedItem = currentItems.get(itemIndex - 1);
                        updateItemPrice(scanner,conn,selectedItem);
                    } else {
                        System.out.println("Invalid index. Please try again.");
                    }
                    break;
                case 6:
                    return;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }


}
