import Entities.Item;
import Entities.Restaurant;

import java.sql.*;
import java.util.*;

public class UserMenu {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/mash_mammad";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1234567890qwertyuiop";
    private static final int PAGE_SIZE = 5;


    private static Scanner scanner = new Scanner(System.in);

    public static void displayUserMenu(String username) {
        int page = 1;
        List<Restaurant> restaurants = new ArrayList<>();

        while (true) {
            System.out.println("\n--- Restaurant List (Page " + page + ") ---");
            fetchAndDisplayRestaurants(page, restaurants);

            System.out.println("\nOptions:");
            System.out.println("1. Next Page");
            System.out.println("2. Previous Page");
            System.out.println("3. Select Restaurant");
            System.out.println("4. View Paid Orders");
            System.out.println("5. View Shopping Cart");
            System.out.println("6. View Owned Restaurants");
            System.out.println("7. Settings");
            System.out.println("8. Logout");
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
                    System.out.print("Enter the index of the restaurant: ");
                    int index = scanner.nextInt();
                    scanner.nextLine();

                    if (index > 0 && index <= restaurants.size()) {
                        Restaurant selectedRestaurant = restaurants.get(index - 1);
                        handleRestaurantMenu(selectedRestaurant, username);
                    } else {
                        System.out.println("Invalid index. Please try again.");
                    }
                    break;
                case 4:
                    displayOrders(username, true);
                    break;
                case 5:
                    displayOrders(username, false);
                    break;
                case 6:
                    RestaurantManager restaurantManager = new RestaurantManager();
                    restaurantManager.manageRestaurants(username, scanner);
                    break;
                case 7:
                    if (authenticateUser(username)) {
                        handleSettings(username);
                    } else {
                        System.out.println("Incorrect password. Returning to menu.");
                    }
                    break;
                case 8:
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    private static boolean authenticateUser(String username) {
        System.out.print("Enter your password: ");
        String password = scanner.nextLine();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM users WHERE username = ? AND password = ? AND isDeleted = 0");
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private static void fetchAndDisplayRestaurants(int page, List<Restaurant> restaurants) {
        int pageSize = 5;
        int offset = (page - 1) * pageSize;
        restaurants.clear();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            CallableStatement stmt = conn.prepareCall("{CALL GetPaginatedRestaurants(?, ?)}");
            stmt.setInt(1, pageSize);
            stmt.setInt(2, offset);
            ResultSet rs = stmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                count++;
                Restaurant restaurant = new Restaurant(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("city"),
                        rs.getString("address"),
                        rs.getFloat("minPurchase")
                );
                restaurants.add(restaurant);
                System.out.println(count + ". Name: " + restaurant.getName());
            }

            if (count == 0) {
                System.out.println("No restaurants available on this page.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void handleRestaurantMenu(Restaurant restaurant, String username) {
        int page = 1;
        int orderId = getOrCreateOrder(restaurant.getId(), username);
        List<Item> currentItems = new ArrayList<>();

        while (true) {
            System.out.println("\n--- Menu for Restaurant: " + restaurant.getName() + " (Page " + page + ") ---");
            fetchAndDisplayFoods(restaurant.getId(), page, orderId, currentItems);

            System.out.println("\nOptions:");
            System.out.println("1. Next Page");
            System.out.println("2. Previous Page");
            System.out.println("3. Add Food to Order");
            System.out.println("4. Remove Food from Order");
            System.out.println("5. Pay for Order");
            System.out.println("6. Go Back to Restaurant List");
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
                    System.out.print("Enter the index of the food to add: ");
                    int itemIndex = scanner.nextInt();
                    if (itemIndex > 0 && itemIndex <= currentItems.size()) {
                        Item selectedItem = currentItems.get(itemIndex - 1);
                        System.out.print("Enter the quantity: ");
                        int quantity = scanner.nextInt();
                        addFoodToOrder(orderId, selectedItem.getId(), quantity);
                    } else {
                        System.out.println("Invalid index. Please try again.");
                    }
                    break;
                case 4:
                    System.out.print("Enter the index of the food to remove: ");
                    int removeIndex = scanner.nextInt();
                    if (removeIndex > 0 && removeIndex <= currentItems.size()) {
                        Item selectedItem = currentItems.get(removeIndex - 1);
                        removeFoodFromOrder(orderId, selectedItem.getId());
                    } else {
                        System.out.println("Invalid index. Please try again.");
                    }
                    break;
                case 5:
                    payForOrder(orderId);
                    break;
                case 6:
                    return;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }


    private static void fetchAndDisplayFoods(int restaurantId, int page, int orderId, List<Item> currentItems) {
        int pageSize = PAGE_SIZE;
        int offset = (page - 1) * pageSize;
        currentItems.clear();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            CallableStatement stmt = conn.prepareCall("{CALL GetPaginatedFoods(?, ?, ?)}");
            stmt.setInt(1, restaurantId);
            stmt.setInt(2, pageSize);
            stmt.setInt(3, offset);
            ResultSet rs = stmt.executeQuery();

            PreparedStatement cartStmt = conn.prepareStatement(
                    "SELECT oi.itemId, oi.quantity FROM orderItem oi WHERE oi.orderId = ?");
            cartStmt.setInt(1, orderId);
            ResultSet cartRs = cartStmt.executeQuery();

            Map<Integer, Integer> cartQuantities = new HashMap<>();
            while (cartRs.next()) {
                cartQuantities.put(cartRs.getInt("itemId"), cartRs.getInt("quantity"));
            }

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

                int quantity = cartQuantities.getOrDefault(item.getId(), 0);
                System.out.println(count + ". Food Name: " + item.getName());
                System.out.println("   Price: " + item.getPrice());
                System.out.println("   Ingredients: " + item.getIngredients());
                System.out.println("   Category: " + item.getCategory());
                System.out.println("   Quantity in Cart: " + quantity);
            }

            if (count == 0) {
                System.out.println("No foods available for this restaurant.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static int getOrCreateOrder(int restaurantId, String username) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            CallableStatement stmt = conn.prepareCall("{CALL GetOrCreateOrder(?, ?, ?)}");
            stmt.setInt(1, restaurantId);
            stmt.setString(2, username);
            stmt.registerOutParameter(3, Types.INTEGER);
            stmt.execute();
            return stmt.getInt(3);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static void addFoodToOrder(int orderId, int foodId, int quantity) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            CallableStatement stmt = conn.prepareCall("{CALL AddFoodToOrder(?, ?, ?)}");
            stmt.setInt(1, orderId);
            stmt.setInt(2, foodId);
            stmt.setInt(3, quantity);
            stmt.execute();
            System.out.println("Food added to order successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void removeFoodFromOrder(int orderId, int foodId) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            CallableStatement stmt = conn.prepareCall("{CALL RemoveFoodFromOrder(?, ?)}");
            stmt.setInt(1, orderId);
            stmt.setInt(2, foodId);
            stmt.execute();
            System.out.println("Food removed from order successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void payForOrder(int orderId) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            CallableStatement stmt = conn.prepareCall("{CALL PayForOrder(?)}");
            stmt.setInt(1, orderId);
            stmt.execute();
            System.out.println("Order paid successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private static void displayOrders(String username, boolean isPaid) {
        List<String> orders = new ArrayList<>();
        Map<Long, String> orderMap = new LinkedHashMap<>();
        Map<Long, List<String>> orderItemsMap = new HashMap<>();
        Scanner scanner = new Scanner(System.in);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            CallableStatement stmt = conn.prepareCall(isPaid ? "{CALL GetPaidOrders(?)}" : "{CALL GetUnpaidOrders(?)}");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                long orderId = rs.getLong("id");
                float totalPrice = rs.getFloat("totalPrice");
                float deliveryFee = totalPrice * 0.05f; // 5% of total price

                String orderInfo = "\nOrder Time: " + rs.getTimestamp("orderTime") +
                        "\nTotal Price: " + totalPrice +
                        "\nDelivery Fee: " + deliveryFee +  // Add this line
                        "\nItems: ";
                orderMap.put(orderId, orderInfo);
                orderItemsMap.put(orderId, new ArrayList<>());
            }


            for (Long orderId : orderMap.keySet()) {
                PreparedStatement itemStmt = conn.prepareStatement("SELECT i.title FROM orderItem oi JOIN item i ON oi.itemId = i.id WHERE oi.orderId = ?");
                itemStmt.setLong(1, orderId);
                ResultSet itemRs = itemStmt.executeQuery();

                while (itemRs.next()) {
                    orderItemsMap.get(orderId).add(itemRs.getString("title"));
                }

                orders.add(orderMap.get(orderId) + String.join(", ", orderItemsMap.get(orderId)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        if (orders.isEmpty()) {
            System.out.println(isPaid ? "No paid orders found." : "No items in the shopping cart.");
            return;
        }

        int currentPage = 0;
        while (true) {
            System.out.println("\n--- " + (isPaid ? "Paid Orders" : "Shopping Cart") + " (Page " + (currentPage + 1) + ") ---");
            int start = currentPage * PAGE_SIZE;
            int end = Math.min(start + PAGE_SIZE, orders.size());
            List<Long> orderIdsOnPage = new ArrayList<>(orderMap.keySet()).subList(start, end);

            for (int i = start; i < end; i++) {
                System.out.println((i - start + 1) + ". " + orders.get(i));
            }

            System.out.println("\nOptions:");
            if (currentPage > 0) {
                System.out.println("1. Previous Page");
            }
            if (end < orders.size()) {
                System.out.println("2. Next Page");
            }
            if (isPaid) {
                System.out.println("3. Reorder an order");
            } else {
                System.out.println("3. Pay an order");
            }
            System.out.println("4. Back to User Menu");

            System.out.print("Choose an option: ");
            String choice = scanner.nextLine();

            if (choice.equals("1") && currentPage > 0) {
                currentPage--;
            } else if (choice.equals("2") && end < orders.size()) {
                currentPage++;
            } else if (isPaid && choice.equals("3")) {
                System.out.print("Enter the number of the order to reorder: ");
                try {
                    int orderIndex = Integer.parseInt(scanner.nextLine()) - 1;
                    if (orderIndex >= 0 && orderIndex < (end - start)) {
                        long orderId = orderIdsOnPage.get(orderIndex);
                        reorderOrder(orderId, username);
                    } else {
                        System.out.println("Invalid order selection.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a number.");
                }

            } else if (!isPaid && choice.equals("3")) {
                System.out.print("Enter the number of the order to pay: ");
                try {
                    int orderIndex = Integer.parseInt(scanner.nextLine()) - 1;
                    if (orderIndex >= 0 && orderIndex < (end - start)) {
                        long orderId = orderIdsOnPage.get(orderIndex);
                        payOrder(orderId);
                        orderMap.remove(orderId);
                        orders.remove(orderIndex);
                        orderIdsOnPage.remove(orderIndex);
                    } else {
                        System.out.println("Invalid order selection.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a number.");
                }
            } else if (choice.equals("4")) {
                break;
            } else {
                System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void payOrder(long orderId) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            CallableStatement stmt = conn.prepareCall("{CALL PayForOrder(?)}");
            stmt.setLong(1, orderId);
            stmt.execute();
            System.out.println("Order paid successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void reorderOrder(long orderId, String username) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            CallableStatement getOrderProc = conn.prepareCall("{CALL GetOrCreateOrder(?, ?, ?)}");
            getOrderProc.setLong(1, getRestaurantIdForOrder(orderId, conn));
            getOrderProc.setString(2, username);
            getOrderProc.registerOutParameter(3, Types.INTEGER);
            getOrderProc.execute();

            long newOrderId = getOrderProc.getInt(3);

            PreparedStatement itemStmt = conn.prepareStatement("SELECT itemId, quantity FROM orderItem WHERE orderId = ?");
            itemStmt.setLong(1, orderId);
            ResultSet itemRs = itemStmt.executeQuery();

            while (itemRs.next()) {
                CallableStatement addFoodProc = conn.prepareCall("{CALL AddFoodToOrder(?, ?, ?)}");
                addFoodProc.setLong(1, newOrderId);
                addFoodProc.setLong(2, itemRs.getLong("itemId"));
                addFoodProc.setInt(3, itemRs.getInt("quantity"));
                addFoodProc.execute();
            }

            System.out.println("Reorder successful. New order ID: " + newOrderId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static long getRestaurantIdForOrder(long orderId, Connection conn) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT i.restaurantId FROM orderItem oi JOIN item i ON oi.itemId = i.id WHERE oi.orderId = ?");
        stmt.setLong(1, orderId);
        ResultSet rs = stmt.executeQuery();
        return rs.next() ? rs.getLong("restaurantId") : -1;
    }

    public static void main(String[] args) {
        System.out.println("Welcome to the User Menu");
        System.out.print("Enter your username: ");
        String username = scanner.nextLine();
        displayUserMenu(username);
    }

    private static void handleSettings(String username) {
        while (true) {
            System.out.println("\n--- Settings ---");
            System.out.println("1. Edit Profile");
            System.out.println("2. Add a New Address");
            System.out.println("3. View and Set Default Address");
            System.out.println("4. Back to Main Menu");
            System.out.print("Select an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    editUserProfile(username);
                    break;
                case 2:
                    addNewAddress(username);
                    break;
                case 3:
                    viewAndSetDefaultAddress(username);
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    private static void editUserProfile(String username) {
        System.out.println("\n--- Edit Profile ---");
        System.out.println("Leave fields blank to keep current values.");

        System.out.print("Enter new username: ");
        String newUsername = scanner.nextLine();
        System.out.print("Enter new first name: ");
        String newFirstName = scanner.nextLine();
        System.out.print("Enter new last name: ");
        String newLastName = scanner.nextLine();
        System.out.print("Enter new password: ");
        String newPassword = scanner.nextLine();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            CallableStatement stmt = conn.prepareCall("{CALL UpdateUserInfo(?, ?, ?, ?, ?)}");
            stmt.setString(1, username);
            stmt.setString(2, newUsername.isEmpty() ? null : newUsername);
            stmt.setString(3, newFirstName.isEmpty() ? null : newFirstName);
            stmt.setString(4, newLastName.isEmpty() ? null : newLastName);
            stmt.setString(5, newPassword.isEmpty() ? null : newPassword);

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Profile updated successfully.");

                if (!newUsername.isEmpty()) {
                    System.out.println("Username updated. You need to log in again.");
                    System.exit(0);
                }
            } else {
                System.out.println("Failed to update profile. Please try again.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private static void addNewAddress(String username) {
        System.out.print("Enter city: ");
        String city = scanner.nextLine();
        System.out.print("Enter address: ");
        String address = scanner.nextLine();
        System.out.print("Enter map location: ");
        String mapLocation = scanner.nextLine();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            conn.setAutoCommit(false);

            // Ensure the city exists in the city table
            PreparedStatement cityStmt = conn.prepareStatement(
                    "INSERT IGNORE INTO city (city) VALUES (?)");
            cityStmt.setString(1, city);
            cityStmt.executeUpdate();

            // Add the address
            CallableStatement stmt = conn.prepareCall("{CALL AddAddress(?, ?, ?, ?, ?)}");
            stmt.setString(1, username);
            stmt.setString(2, city);
            stmt.setString(3, address);
            stmt.setString(4, mapLocation);
            stmt.setBoolean(5, false); // Set default to false
            stmt.execute();

            conn.commit();
            System.out.println("Address added successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private static void viewAndSetDefaultAddress(String username) {
        List<Address> addresses = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT id, city, address, mapLoc, isDefault FROM address WHERE userId = ? AND isDeleted = 0");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            System.out.println("\n--- Your Addresses ---");
            int count = 0;
            while (rs.next()) {
                count++;
                Address address = new Address(
                        rs.getInt("id"),
                        rs.getString("city"),
                        rs.getString("address"),
                        rs.getString("mapLoc"),
                        rs.getBoolean("isDefault")
                );
                addresses.add(address);
                System.out.println(count + ". " + address);
            }

            if (addresses.isEmpty()) {
                System.out.println("No addresses found.");
                return;
            }

            System.out.print("Enter the index of the address to set as default: ");
            int index = scanner.nextInt();
            scanner.nextLine();

            if (index > 0 && index <= addresses.size()) {
                Address selectedAddress = addresses.get(index - 1);
                setDefaultAddress(selectedAddress.getId(), username);
            } else {
                System.out.println("Invalid selection. Returning to settings menu.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void setDefaultAddress(int addressId, String username) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            conn.setAutoCommit(false);

            PreparedStatement resetStmt = conn.prepareStatement(
                    "UPDATE address SET isDefault = false WHERE userId = ?");
            resetStmt.setString(1, username);
            resetStmt.executeUpdate();

            PreparedStatement setDefaultStmt = conn.prepareStatement(
                    "UPDATE address SET isDefault = true WHERE id = ?");
            setDefaultStmt.setInt(1, addressId);
            setDefaultStmt.executeUpdate();

            conn.commit();

            System.out.println("Default address updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


