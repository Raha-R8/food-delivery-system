import Entities.Restaurant;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class RestaurantManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/mash_mammad";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1234567890qwertyuiop";
    private static final int PAGE_SIZE = 5;

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
                            manageRestaurantOptions(conn, selectedRestaurant);
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

    public static void manageRestaurantOptions(Connection conn, Restaurant restaurant) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n--- Manage Your Restaurant: " + restaurant.getName() + " ---");
            System.out.println("1. Change Name");
            System.out.println("2. Exit");
            System.out.print("Choose an option: ");
            int option = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            if (option == 1) {
                System.out.print("Enter the new restaurant name: ");
                String newName = scanner.nextLine();

                try (CallableStatement stmt = conn.prepareCall("{CALL UpdateRestaurantName(?, ?)}")) {
                    stmt.setInt(1, restaurant.getId());
                    stmt.setString(2, newName);
                    int rowsUpdated = stmt.executeUpdate();

                    if (rowsUpdated > 0) {
                        restaurant.setName(newName);
                        System.out.println("Restaurant name updated successfully.");
                    } else {
                        System.out.println("Failed to update restaurant name.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else if (option == 2) {
                System.out.println("Exiting management menu.");
                break;
            } else {
                System.out.println("Invalid option. Please try again.");
            }
        }
    }
}
