import Entities.Restaurant;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
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
            System.out.println("1. Settings");
            System.out.println("2. Exit");
            System.out.print("Choose an option: ");
            int option = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            if (option == 1) {
                manageRestaurantSettings(conn, restaurant);
            } else if (option == 2) {
                System.out.println("Exiting management menu.");
                break;
            } else {
                System.out.println("Invalid option. Please try again.");
            }
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

}
