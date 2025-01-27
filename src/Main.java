import java.sql.*;
import java.util.Scanner;

public class Main {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/mash_mammad";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "1234567890qwertyuiop";

    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            System.out.println("Welcome to Mash Mammad Food Delivery System");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");
            System.out.print("Select an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    login();
                    break;
                case 2:
                    register();
                    break;
                case 3:
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    private static void login() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            CallableStatement stmt = conn.prepareCall("{CALL AuthenticateUser(?, ?, ?)}");
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.registerOutParameter(3, Types.BOOLEAN);
            stmt.execute();

            boolean isAuthenticated = stmt.getBoolean(3);
            if (isAuthenticated) {
                System.out.println("Login successful!");
                UserMenu userMenu = new UserMenu();
                userMenu.displayUserMenu(username);
            } else {
                System.out.println("Invalid credentials!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void register() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.print("Enter first name: ");
        String firstName = scanner.nextLine();
        System.out.print("Enter last name: ");
        String lastName = scanner.nextLine();
        System.out.print("Enter phone number: ");
        String phoneNum = scanner.nextLine();
        System.out.print("Enter city: ");
        String city = scanner.nextLine();
        System.out.print("Enter address: ");
        String address = scanner.nextLine();
        System.out.print("Enter map location: ");
        String mapLoc = scanner.nextLine();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            CallableStatement stmt = conn.prepareCall("{CALL RegisterUser(?, ?, ?, ?, ?, ?)}");
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, firstName);
            stmt.setString(4, lastName);
            stmt.setString(5, phoneNum);
            stmt.registerOutParameter(6, Types.BOOLEAN);
            stmt.execute();

            boolean isRegistered = stmt.getBoolean(6);
            if (isRegistered) {
                System.out.println("Registration successful! Adding default address...");

                // Ensure city exists in city table
                CallableStatement cityStmt = conn.prepareCall("{CALL AddCity(?)}");
                cityStmt.setString(1, city);
                cityStmt.execute();

                // Add default address
                CallableStatement addressStmt = conn.prepareCall("{CALL AddAddress(?, ?, ?, ?, ?)}");
                addressStmt.setString(1, username);
                addressStmt.setString(2, city);
                addressStmt.setString(3, address);
                addressStmt.setString(4, mapLoc);
                addressStmt.setBoolean(5, true);
                addressStmt.execute();

                System.out.println("Default address added successfully.");
            } else {
                System.out.println("Username already exists!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
