package parkinglot;

import javax.swing.SwingUtilities;

public class ParkingLot {
    public static void main(String[] args) {
        Database database = Database.getInstance();  
        Parking parking = new Parking(12, database);  
        SwingUtilities.invokeLater(() -> new ParkingUI(parking, database));  
    }
}
