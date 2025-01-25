import java.sql.*;
import java.util.Scanner;

public class Main {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/mash_mammad";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password";

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
                // Redirect based on user type (Admin, Manager, Customer)
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
                System.out.println("Registration successful! You can now log in.");
            } else {
                System.out.println("Username already exists!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
