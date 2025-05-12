package parkinglot;

import java.sql.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Database {
    private static Database instance;
    private Connection conn;
    private static final String URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASSWORD = "chehak03";
    private final Lock mutex = new ReentrantLock();

    private Database() {
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            conn.setAutoCommit(true);
            System.out.println("Connected to PostgreSQL database.");
        } catch (Exception e) {
            System.err.println("Connection failed: " + e.getMessage());
        }
    }

    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    public int insertCar() {
        mutex.lock();
        try {
            String sql = "INSERT INTO cars (car_parked_at) VALUES (DEFAULT) RETURNING car_id";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int carId = rs.getInt("car_id");
                    System.out.println("Car inserted with ID: " + carId);
                    return carId;
                } else {
                    System.out.println("Car insertion failed, no car_id returned.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Insert car failed: " + e.getMessage());
        } finally {
            mutex.unlock();
        }
        return -1;
    }

    public boolean carExists(int carId) {
        mutex.lock();
        try {
            String checkCarSql = "SELECT car_id FROM cars WHERE car_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(checkCarSql)) {
                stmt.setInt(1, carId);
                ResultSet rs = stmt.executeQuery();
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Car existence check failed: " + e.getMessage());
        } finally {
            mutex.unlock();
        }
        return false;
    }

    public void updateSlotStatus(int slotId, boolean isReserved, boolean isOccupied, int reservedBy, Timestamp reservedUntil) {
        mutex.lock();
        try {
            String checkCarSql = "SELECT car_id FROM cars WHERE car_id = ?";
            try (PreparedStatement checkCarStmt = conn.prepareStatement(checkCarSql)) {
                checkCarStmt.setInt(1, reservedBy);
                ResultSet rs = checkCarStmt.executeQuery();

                if (!rs.next()) {
                    System.out.println("Car with ID " + reservedBy + " does not exist. Inserting car...");
                    insertCar();
                }

                String sql = "UPDATE slots SET is_reserved = ?, is_occupied = ?, reserved_by = ?, reserved_until = ?, updated_at = CURRENT_TIMESTAMP WHERE slot_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setBoolean(1, isReserved);
                    stmt.setBoolean(2, isOccupied);
                    stmt.setInt(3, reservedBy);
                    stmt.setTimestamp(4, reservedUntil);
                    stmt.setInt(5, slotId);

                    int rows = stmt.executeUpdate();
                    if (rows > 0) {
                        System.out.println("Slot status updated for Slot ID: " + slotId);
                    } else {
                        System.out.println("No rows were updated. The slot ID might not exist.");
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Slot update failed: " + e.getMessage());
        } finally {
            mutex.unlock();
        }
    }

    public void insertReservation(int slotId, int carId, Timestamp reservedUntil) {
        mutex.lock();
        try {
            String sql = "INSERT INTO reservations (slot_id, car_id, reserved_until) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, slotId);
                stmt.setInt(2, carId);
                stmt.setTimestamp(3, reservedUntil);

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    System.out.println("Reservation inserted: Slot " + slotId + " for Car " + carId);
                } else {
                    System.out.println("Reservation insert failed for Car: " + carId);
                }
            }
        } catch (SQLException e) {
            System.err.println("Insert reservation failed: " + e.getMessage());
        } finally {
            mutex.unlock();
        }
    }

    public void insertParkingSlot(int slotId) {
        mutex.lock();
        try {
            String sql = "INSERT INTO slots (slot_id, is_reserved, is_occupied, reserved_by, reserved_until) " +
                         "VALUES (?, FALSE, FALSE, NULL, NULL) " +
                         "ON CONFLICT (slot_id) DO NOTHING";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, slotId);
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    System.out.println("Parking slot " + slotId + " inserted into the database.");
                } else {
                    System.out.println("Parking slot " + slotId + " already exists.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Insert parking slot failed: " + e.getMessage());
        } finally {
            mutex.unlock();
        }
    }

    public void processPayment(int carId, double amount) {
        mutex.lock();
        try {
            String sql = "INSERT INTO payments (car_id, amount) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, carId);
                stmt.setDouble(2, amount);
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    System.out.println("Payment recorded for Car ID: " + carId + ", Amount: $" + amount);
                } else {
                    System.out.println("Payment failed for Car ID: " + carId);
                }
            }
        } catch (SQLException e) {
            System.err.println("Insert payment failed: " + e.getMessage());
        } finally {
            mutex.unlock();
        }
    }

    public void close() {
        mutex.lock();
        try {
            if (conn != null) {
                conn.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Close failed: " + e.getMessage());
        } finally {
            mutex.unlock();
        }
    }
}
