import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

public class Admin {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/mash_mammad";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1234567890qwertyuiop";


    private String username;
    private static Scanner scanner = new Scanner(System.in);
    public static ArrayList<Long> restaurantIds = new ArrayList<>(); // Static global array
    private static long editingRestaurantId;
    // Constructor
    public Admin(String username) {
        this.username = username;
    }

    // Check if the current user is a valid admin
    // Check if the current user is a valid admin
    public boolean isAdmin() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            CallableStatement stmt = conn.prepareCall("{CALL CheckIfAdmin(?, ?)}");
            stmt.setString(1, this.username);
            stmt.registerOutParameter(2, Types.BOOLEAN);
            stmt.execute();
            return stmt.getBoolean(2);
        } catch (SQLException e) {
            System.out.println("ERROR !!!!! SQL Error: " + e.getMessage());
        }
        return false;
    }


    // Add a new admin
    public boolean addNewAdmin(String newAdminUsername) {
        if (!isAdmin()) {
            System.out.println("Access denied! Only admins can perform this action.");
            return false;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            CallableStatement stmt = conn.prepareCall("{CALL AddNewAdmin(?, ?, ?)}");
            stmt.setString(1, this.username);
            stmt.setString(2, newAdminUsername);
            stmt.registerOutParameter(3, Types.BOOLEAN);
            stmt.execute();

            boolean success = stmt.getBoolean(3);
            if (success) {
                System.out.println("Admin added successfully!");
                return true;
            } else {
                System.out.println("Failed to add admin. Make sure the user exists.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("ERROR !!!!! SQL Error: " + e.getMessage());
        }
        return false;
    }
    public void displayUsers(int pageNumber) {
        if (!isAdmin()) {
            System.out.println("Access denied! Only admins can perform this action.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Prepare the callable statement to call the stored procedure
            CallableStatement stmt = conn.prepareCall("{CALL getUsersByPage(?)}");
            stmt.setInt(1, pageNumber); // Set the page number

            ResultSet rs = stmt.executeQuery();

            System.out.println("List of Users (Page " + pageNumber + "):");
            boolean foundUsers = false;
            while (rs.next()) {
                foundUsers = true;
                String userType = rs.getString("userType");
                System.out.printf("[%s] Username: %s, Name: %s %s, Phone: %s%n",
                        userType,
                        rs.getString("username"),
                        rs.getString("name"),
                        rs.getString("lastName"),
                        rs.getString("phoneNum"));
            }

            if (!foundUsers) {
                System.out.println("No users found on this page.");
            }
        } catch (SQLException e) {
            System.out.println("ERROR !!!!! SQL Error: " + e.getMessage());
        }
    }

    // Display deleted admins
    // Display deleted admins with pagination
    public void displayDeletedAdmins() {
        if (!isAdmin()) {
            System.out.println("Access denied! Only admins can perform this action.");
            return;
        }

        int pageSize = 5; // Number of records per page
        int currentPage = 1;

        while (true) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                // Call stored procedure to fetch paginated results
                CallableStatement stmt = conn.prepareCall("{CALL GetDeletedAdminsPage(?, ?)}");
                stmt.setInt(1, currentPage); // Set the current page number
                stmt.setInt(2, pageSize);   // Set the page size

                ResultSet rs = stmt.executeQuery();

                System.out.printf("List of Deleted Admins (Page %d):%n", currentPage);
                boolean foundDeletedAdmins = false;

                while (rs.next()) {
                    foundDeletedAdmins = true;
                    System.out.printf("Username: %s%n", rs.getString("adminId"));
                }

                if (!foundDeletedAdmins) {
                    System.out.println("No deleted admins found on this page.");
                }

                // Navigation options
                System.out.println("\nOptions:");
                System.out.println("1. Next Page");
                System.out.println("2. Previous Page");
                System.out.println("3. Exit");
                System.out.print("Choose an option: ");

                int choice = scanner.nextInt();
                scanner.nextLine(); // Clear the newline character

                switch (choice) {
                    case 1 -> currentPage++; // Move to the next page
                    case 2 -> {
                        if (currentPage > 1) {
                            currentPage--; // Move to the previous page
                        } else {
                            System.out.println("Already on the first page.");
                        }
                    }
                    case 3 -> {
                        return; // Exit pagination
                    }
                    default -> System.out.println("Invalid option. Please try again.");
                }
            } catch (SQLException e) {
                System.out.println("ERROR !!!!! SQL Error: " + e.getMessage());
            }
        }
    }
    // Display deleted users with pagination
    public void displayDeletedUsers() {
        if (!isAdmin()) {
            System.out.println("Access denied! Only admins can perform this action.");
            return;
        }

        int pageSize = 5; // Number of records per page
        int currentPage = 1;

        while (true) {
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                // Call the stored procedure for paginated results
                CallableStatement stmt = conn.prepareCall("{CALL GetDeletedUsersPage(?, ?)}");
                stmt.setInt(1, currentPage); // Set the current page number
                stmt.setInt(2, pageSize);   // Set the page size

                ResultSet rs = stmt.executeQuery();

                System.out.printf("List of Deleted Users (Page %d):%n", currentPage);
                boolean foundDeletedUsers = false;

                while (rs.next()) {
                    foundDeletedUsers = true;
                    System.out.printf("Username: %s, Name: %s %s, Phone: %s%n",
                            rs.getString("username"),
                            rs.getString("name"),
                            rs.getString("lastName"),
                            rs.getString("phoneNum"));
                }

                if (!foundDeletedUsers) {
                    System.out.println("No deleted users found on this page.");
                }

                // Navigation options
                System.out.println("\nOptions:");
                System.out.println("1. Next Page");
                System.out.println("2. Previous Page");
                System.out.println("3. Exit");
                System.out.print("Choose an option: ");

                int choice = scanner.nextInt();
                scanner.nextLine(); // Clear the newline character

                switch (choice) {
                    case 1 -> currentPage++; // Move to the next page
                    case 2 -> {
                        if (currentPage > 1) {
                            currentPage--; // Move to the previous page
                        } else {
                            System.out.println("Already on the first page.");
                        }
                    }
                    case 3 -> {
                        return; // Exit pagination
                    }
                    default -> System.out.println("Invalid option. Please try again.");
                }
            } catch (SQLException e) {
                System.out.println("ERROR !!!!! SQL Error: " + e.getMessage());
            }
        }
    }

    // Delete a user or admin

    public boolean deleteUser(String targetUsername) {
        if (!isAdmin()) {
            System.out.println("Access denied! Only admins can perform this action.");
            return false;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Prepare the call to the stored procedure
            CallableStatement stmt = conn.prepareCall("{CALL DeleteUser(?, ?)}");
            stmt.setString(1, targetUsername);  // Pass the target user to be deleted
            stmt.setString(2, this.username);   // Pass the requesting admin's username
            stmt.execute();

            // Adjust according to the stored procedure's logic
            // There's no boolean result from the procedure, so we need to check for a result set
            ResultSet rs = stmt.getResultSet();
            if (rs != null && rs.next()) {
                String result = rs.getString("result");
                System.out.println(result);
                return result.equals("User deleted successfully.");
            }
        } catch (SQLException e) {
            System.out.println("ERROR !!!!! SQL Error: " + e.getMessage());
        }
        return false;
    }

    public void updateUser(String username) {
        Scanner sc = new Scanner(System.in);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            // Check if user exists
            String queryCheck = "SELECT * FROM users WHERE username = ? AND isDeleted = 0";
            PreparedStatement checkStmt = conn.prepareStatement(queryCheck);
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                System.out.println("User not found or is deleted.");
                return;
            }

            // Display current user details
            String name = rs.getString("name");
            String lastName = rs.getString("lastName");
            String phoneNum = rs.getString("phoneNum");

            System.out.println("Current user details:");
            System.out.println("1. Name: " + name);
            System.out.println("2. Last Name: " + lastName);
            System.out.println("3. Phone Number: " + phoneNum);

            System.out.println("Choose the field to update (1-3):");
            int choice = sc.nextInt();
            sc.nextLine(); // Consume newline

            String updateField = null;
            String newValue = null;

            switch (choice) {
                case 1:
                    updateField = "name";
                    System.out.println("Enter new name:");
                    newValue = sc.nextLine();
                    break;
                case 2:
                    updateField = "lastName";
                    System.out.println("Enter new last name:");
                    newValue = sc.nextLine();
                    break;
                case 3:
                    updateField = "phoneNum";
                    System.out.println("Enter new phone number:");
                    newValue = sc.nextLine();
                    break;
                default:
                    System.out.println("Invalid choice.");
                    return;
            }

            // Update the selected field
            String updateQuery = "UPDATE users SET " + updateField + " = ? WHERE username = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
            updateStmt.setString(1, newValue);
            updateStmt.setString(2, username);

            int rowsAffected = updateStmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("User updated successfully.");
            } else {
                System.out.println("Failed to update user.");
            }

        } catch (SQLException e) {
            System.out.println("ERROR !!!!! SQL Error: " + e.getMessage());
        }
    }
    // Retrieve admin privilege from a user
    public boolean retrieveAdminPrivilege(String username) {
        if (!isAdmin()) {
            System.out.println("Access denied! Only admins can perform this action.");
            return false;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Prepare the call to the stored procedure
            String storedProc = "{CALL RetrieveAdminPrivilege(?)}";
            try (CallableStatement stmt = conn.prepareCall(storedProc)) {
                // Set the input parameter for the username
                stmt.setString(1, username);

                // Execute the procedure
                stmt.execute();

                System.out.println("Admin privileges removed successfully.");
                return true;
            } catch (SQLException e) {
                System.out.println("ERROR !!!!! SQL Error while calling stored procedure: " + e.getMessage());
                return false;
            }
        } catch (SQLException e) {
            System.out.println("ERROR !!!!! Database connection error: " + e.getMessage());
            return false;
        }
    }


    // Users menu to manage users
    public void usersMenu() {
        int currentPage = 1; // Start on page 1
        boolean exitMenu = false;

        while (!exitMenu) {
            displayUsers(currentPage); // Show users for the current page

            System.out.println("\nOptions:");
            System.out.println("1. Add New User");
            System.out.println("2. Delete User");
            System.out.println("3. Update User Data");
            System.out.println("4. Add New Admin");
            System.out.println("5. Retrieve Admin Privilege");
            System.out.println("6. Display deleted users");
            System.out.println("7. Display deleted admins");
            System.out.println("8. Go to Next Page");
            System.out.println("9. Go to Previous Page");
            System.out.println("10. Back to Admin Menu");
            System.out.print("Select an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            switch (choice) {
                case 1:
                    System.out.println("Registering a new user:");
                    Main.register();
                    break;
                case 2:
                    System.out.print("Enter the username of the user to delete: ");
                    String deleteUser = scanner.nextLine();
                    deleteUser(deleteUser);
                    break;
                case 3:
                    System.out.print("Enter the username of the user to update: ");
                    String updateUser = scanner.nextLine();
                    updateUser(updateUser);
                    break;
                case 4:
                    System.out.print("Enter the username of the new admin: ");
                    String newAdmin = scanner.nextLine();
                    addNewAdmin(newAdmin);
                    break;
                case 5:
                    System.out.print("Enter the username of the admin to take admin privileges from: ");
                    String deleteAdmin = scanner.nextLine();
                    retrieveAdminPrivilege(deleteAdmin);
                    break;
                case 6:
                    displayDeletedUsers();
                    break;
                case 7:
                    displayDeletedAdmins();
                    break;
                case 8:
                    currentPage++; // Go to the next page
                    break;
                case 9:
                    if (currentPage > 1) {
                        currentPage--; // Go to the previous page
                    } else {
                        System.out.println("You are already on the first page!");
                    }
                    break;
                case 10:
                    exitMenu = true; // Exit to Admin Menu
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }


    // Display restaurants with pagination

    public boolean addRestaurant() {
        System.out.print("Enter manager username: ");
        String managerId = scanner.nextLine();
        System.out.print("Enter restaurant name: ");
        String name = scanner.nextLine();
        System.out.print("Enter minimum purchase: ");
        float minPurchase = scanner.nextFloat();
        scanner.nextLine(); // Consume newline
        System.out.print("Enter city: ");
        String city = scanner.nextLine();
        System.out.print("Enter address: ");
        String address = scanner.nextLine();
        System.out.print("Enter map location: ");
        String mapLoc = scanner.nextLine();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            CallableStatement stmt = conn.prepareCall("{CALL AddRestaurant(?, ?, NULL, ?, ?, ?, ?, ?, ?)}");
            stmt.setString(1, managerId);
            stmt.setString(2, name);
            stmt.setFloat(3, minPurchase);
            stmt.setString(4, city);
            stmt.setString(5, address);
            stmt.setString(6, mapLoc);
            stmt.registerOutParameter(7, Types.BOOLEAN);
            stmt.registerOutParameter(8, Types.BIGINT); // To retrieve the generated restaurant ID

            stmt.execute();

            boolean isSuccess = stmt.getBoolean(7);
            if (isSuccess) {
                editingRestaurantId = stmt.getLong(8); // Store the generated restaurant ID
                System.out.println("Restaurant added successfully!");
                return true;
            } else {
                System.out.println("Failed to add restaurant.");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("ERROR !!!!! SQL Error: " + e.getMessage());
            return false;
        }
    }
    public static void addOpenDayAndHours(long restaurantId) {


        if (restaurantId <= 0) {
            System.out.println("No restaurant is currently being edited. Please add a restaurant first.");
            return;
        }

        String[] weekdays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        HashSet<Integer> addedDays = new HashSet<>(); // To track selected days

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            CallableStatement openDayStmt = conn.prepareCall("{CALL AddOpenDay(?, ?, ?, ?)}");
            CallableStatement openHourStmt = conn.prepareCall("{CALL AddOpenHour(?, ?, ?, ?)}");

            while (addedDays.size() < weekdays.length) {
                System.out.println("Enter the weekday to add an open day (e.g., 1 for Monday), or type 'exit' to finish.");
                System.out.print("Your choice: ");
                String input = scanner.nextLine().trim();

                if (input.equalsIgnoreCase("exit")) {
                    break;
                }

                try {
                    int day = Integer.parseInt(input) - 1;
                    if (day < 0 || day >= weekdays.length || addedDays.contains(day)) {
                        System.out.println("Invalid or duplicate day number. Please try again.");
                        continue;
                    }

                    String weekday = weekdays[day];
                    System.out.println("Adding open day for: " + weekday);
//                    System.out.println("!!!!!!!!!!!!!!!!!!!!!");
//                    System.out.println(restaurantId);

                    // Add the open day and retrieve its ID
                    openDayStmt.setLong(1, restaurantId);
                    openDayStmt.setString(2, weekday);
                    openDayStmt.registerOutParameter(3, Types.BIGINT); // openDayId
                    openDayStmt.registerOutParameter(4, Types.BOOLEAN); // isSuccess

                    openDayStmt.execute();
                    boolean isDayAdded = openDayStmt.getBoolean(4);
                    long openDayId = openDayStmt.getLong(3);

                    if (!isDayAdded) {
                        System.out.println("Failed to add open day for: " + weekday);
                        continue;
                    }
                    System.out.println("Successfully added open day for: " + weekday);
                    addedDays.add(day);

                    // Add open hours for the newly added open day
                    while (true) {
                        System.out.println("Enter open hours for " + weekday + " (HH:mm-HH:mm). Type 'exit' to finish:");
                        System.out.print("Open hours: ");
                        String timeInput = scanner.nextLine();

                        if (timeInput.equalsIgnoreCase("exit")) {
                            break;
                        }

                        String[] times = timeInput.split("-");
                        if (times.length != 2) {
                            System.out.println("Invalid format. Use HH:mm-HH:mm.");
                            continue;
                        }

                        try {
                            Time openHour = Time.valueOf(times[0] + ":00");
                            Time closeHour = Time.valueOf(times[1] + ":00");

                            openHourStmt.setTime(1, openHour);
                            openHourStmt.setTime(2, closeHour);
                            openHourStmt.setLong(3, openDayId);
                            openHourStmt.registerOutParameter(4, Types.BOOLEAN);

                            openHourStmt.execute();

                            boolean isHourAdded = openHourStmt.getBoolean(4);
                            if (isHourAdded) {
                                System.out.println("Added open hour: " + openHour + " - " + closeHour);
                            } else {
                                System.out.println("Failed to add open hour: " + openHour + " - " + closeHour);
                            }
                        } catch (IllegalArgumentException e) {
                            System.out.println("Invalid time format. Please use HH:mm.");
                        }
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a valid weekday number.");
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
    }


    public void deleteRestaurant(long restaurantId) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Prepare the stored procedure call
            CallableStatement stmt = conn.prepareCall("{CALL DeleteRestaurant(?, ?)}");

            // Set input and output parameters
            stmt.setLong(1, restaurantId);
            stmt.registerOutParameter(2, Types.BOOLEAN);

            // Execute the stored procedure
            stmt.execute();

            // Check the result
            boolean isSuccess = stmt.getBoolean(2);
            if (isSuccess) {
                System.out.println("Restaurant has been successfully deleted.");
            } else {
                System.out.println("Failed to delete the restaurant. It may not exist or is already deleted.");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
    }

    public void displayRestaurants() {
        int pageSize = 5; // Number of records per page
        int currentPage = 1;

        while (true) {
            restaurantIds.clear(); // Clear the global array for the current page

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                CallableStatement stmt = conn.prepareCall("{CALL GetRestaurantsPage(?, ?)}");
                stmt.setInt(1, currentPage);
                stmt.setInt(2, pageSize);

                ResultSet rs = stmt.executeQuery();

                System.out.printf("List of Restaurants (Page %d):%n", currentPage);
                boolean foundRestaurants = false;

                while (rs.next()) {
                    foundRestaurants = true;
                    long id = rs.getLong("id");
                    restaurantIds.add(id); // Store restaurant IDs in the global array

                    System.out.printf("ID: %d, Name: %s, City: %s, Address: %s, Map Location: %s, Min Purchase: %.2f%n",
                            id,
                            rs.getString("name"),
                            rs.getString("city"),
                            rs.getString("address"),
                            rs.getString("mapLoc"),
                            rs.getFloat("minPurchase"));
                }

                if (!foundRestaurants) {
                    System.out.println("No restaurants found on this page.");
                }

                // Navigation options
                System.out.println("\nOptions:");
                System.out.println("1. Next Page");
                System.out.println("2. Previous Page");
                System.out.println("3. Exit");
                System.out.print("Choose an option: ");

                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1 -> currentPage++;
                    case 2 -> {
                        if (currentPage > 1) {
                            currentPage--;
                        } else {
                            System.out.println("Already on the first page.");
                        }
                    }
                    case 3 -> {
                        return; // Exit pagination
                    }
                    default -> System.out.println("Invalid option. Please try again.");
                }
            } catch (SQLException e) {
                System.out.println("ERROR !!!!! SQL Error: " + e.getMessage());
            }
        }
    }



    public void restaurantMenu() {
        int pageSize = 5; // Number of restaurants per page
        int currentPage = 1; // Start at page 1
        boolean exitMenu = false;

        while (!exitMenu) {
            restaurantIds.clear(); // Clear the list for the current page

            // Display the current page of restaurants
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                CallableStatement stmt = conn.prepareCall("{CALL GetRestaurantsPage(?, ?)}");
                stmt.setInt(1, currentPage);
                stmt.setInt(2, pageSize);

                ResultSet rs = stmt.executeQuery();

                System.out.printf("List of Restaurants (Page %d):%n", currentPage);
                boolean foundRestaurants = false;

                while (rs.next()) {
                    foundRestaurants = true;
                    long id = rs.getLong("id");
                    restaurantIds.add(id); // Store restaurant IDs in the global list

                    System.out.printf("%d. Name: %s, City: %s, Address: %s, Map Location: %s, Min Purchase: %.2f%n",
                            restaurantIds.size(), // Display the index starting from 1
                            rs.getString("name"),
                            rs.getString("city"),
                            rs.getString("address"),
                            rs.getString("mapLoc"),
                            rs.getFloat("minPurchase"));
                }

                if (!foundRestaurants) {
                    System.out.println("No restaurants found on this page.");
                }

            } catch (SQLException e) {
                System.out.println("SQL Error: " + e.getMessage());
            }

            // Display menu options below the restaurants
            System.out.println("\nOptions:");
            System.out.println("1. Select a Restaurant");
            System.out.println("2. Add New Restaurant");
            System.out.println("3. Next Page");
            System.out.println("4. Previous Page");
            System.out.println("5. Exit menu");
            System.out.print("Select an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            switch (choice) {
                case 1: // Select a restaurant
                    if (restaurantIds.isEmpty()) {
                        System.out.println("No restaurants to select on this page.");
                    } else {
                        System.out.println("Select a restaurant by its number:");
                        int restaurantChoice = scanner.nextInt();
                        scanner.nextLine(); // Consume newline

                        if (restaurantChoice > 0 && restaurantChoice <= restaurantIds.size()) {
                            long restaurantId = restaurantIds.get(restaurantChoice - 1);
                            System.out.println("You selected restaurant with ID: " + restaurantId);
                            System.out.println("1. Delete this restaurant");
                            System.out.println("2. Back to menu");
                            System.out.print("Choose an option: ");

                            int actionChoice = scanner.nextInt();
                            scanner.nextLine(); // Consume newline
                            if (actionChoice == 1) {
                                deleteRestaurant(restaurantId);
                            } else if (actionChoice == 2) {
                                System.out.println("Returning to menu...");
                            } else {
                                System.out.println("Invalid choice. Returning to menu.");
                            }
                        } else {
                            System.out.println("Invalid choice. Please select a valid restaurant number.");
                        }
                    }
                    break;

                case 2: // Add a new restaurant
                    System.out.println("Adding new restaurant:");
                    if (addRestaurant()) {
                        addOpenDayAndHours(editingRestaurantId);
                        System.out.println("Restaurant added successfully.");
                    } else {
                        deleteRestaurant(editingRestaurantId);
                        System.out.println("Failed to add restaurant.");
                    }
                    break;

                case 3: // Next page
                    currentPage++;
                    break;

                case 4: // Previous page
                    if (currentPage > 1) {
                        currentPage--;
                    } else {
                        System.out.println("Already on the first page.");
                    }
                    break;

                case 5: // Exit the menu
                    exitMenu = true;
                    break;

                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    public void deleteCategory(String categoryName) {
        String sql = "{CALL SoftDeleteCategory(?)}";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, categoryName);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Category '" + categoryName + "' marked as deleted.");
            } else {
                System.out.println("Category not found.");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
    }

    public static void addCategory(Scanner scanner) {
        System.out.print("Enter category name: ");
        String categoryName = scanner.nextLine().trim();

        if (categoryName.isEmpty()) {
            System.out.println("Category name cannot be empty!");
            return;
        }

        String sql = "{CALL AddCategory(?)}";

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            try (CallableStatement stmt = conn.prepareCall(sql)) {
                stmt.setString(1, categoryName);
                stmt.execute();
                System.out.println("Category added successfully!");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.out.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }


    public void categoryMenu() {
        Scanner scanner = new Scanner(System.in);
        int pageNumber = 1;
        int pageSize = 5;
        String userInput;

        while (true) {
            displayCategories(pageNumber, pageSize);

            System.out.println("\nOptions:");
            System.out.println("1. Delete a Category");
            System.out.println("2. Add a Category");
            System.out.println("3. Next Page");
            System.out.println("4. Previous Page");
            System.out.println("5. Exit menu");
            System.out.print("Select an option: ");
            userInput = scanner.nextLine().trim();

            switch (userInput) {
                case "1":
                    System.out.print("Enter category name to delete: ");
                    String categoryName = scanner.nextLine().trim();
                    deleteCategory(categoryName);
                    break;
                case "2":
                    addCategory(scanner);
                    break;
                case "3":
                    pageNumber++;
                    break;
                case "4":
                    if (pageNumber > 1) {
                        pageNumber--;
                    } else {
                        System.out.println("You are already on the first page.");
                    }
                    break;
                case "5":
                    System.out.println("Exiting category browser.");
                    return;
                default:
                    System.out.println("Invalid input. Try again.");
            }
        }
    }

    private void displayCategories(int pageNumber, int pageSize) {
        String sql = "{CALL GetCategoriesPaged(?, ?)}";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, pageNumber);
            stmt.setInt(2, pageSize);

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("\n--- Page " + pageNumber + " ---");
                boolean hasResults = false;

                while (rs.next()) {
                    hasResults = true;
                    String categoryName = rs.getString("categoryName");
                    boolean isDeleted = rs.getBoolean("isDeleted");
                    System.out.println(categoryName + " | Deleted: " + isDeleted);
                }

                if (!hasResults) {
                    System.out.println("No categories found.");
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
        }
    }

    // Admin menu
    public void adminMenu() {
        while (true) {
            System.out.println("Admin Menu:");
            System.out.println("1. Manage Users");
            System.out.println("2. Manage Restaurants");
            System.out.println("3. Manage Category");
            System.out.println("4. Go to user menu");
            System.out.println("5. Logout");
            System.out.print("Select an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    usersMenu();
                    break;
                case 2:
                    restaurantMenu();
                    break;
                case 3:
                    categoryMenu();
                    break;
                case 4:
                    UserMenu userMenu = new UserMenu();
                    userMenu.displayUserMenu(username);
                    break;
                case 5:
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }


// Login function that checks the credentials and returns whether login is successful
    public static boolean adminLogin(String username, String password) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            CallableStatement stmt = conn.prepareCall("{CALL AuthenticateUser(?, ?, ?)}");
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.registerOutParameter(3, Types.BOOLEAN);
            stmt.execute();

            return stmt.getBoolean(3);
        } catch (SQLException e) {
            System.out.println("ERROR !!!!! SQL Error: " + e.getMessage());
        }
        return false;
    }
}