package parkinglot;

import java.sql.*;
import java.util.concurrent.Semaphore;

public class Car extends Thread {
    private Parking parking;
    private int carID;
    private Slot reservedSlot;
    private Database database;
    private Semaphore semaphore;  

    public Car(Parking parking, int carID, Database database, Semaphore semaphore) {
        this.parking = parking;
        this.carID = carID;
        this.database = database;
        this.semaphore = semaphore;
    }

    @Override
    public void run() {
            try {
                semaphore.acquire();  
                reservedSlot = parking.reserveSlot(carID); 
                if (reservedSlot != null) {
                    System.out.println("Car " + carID + " reserved slot: " + reservedSlot.getSlotId());

                    Timestamp reservedUntil = new Timestamp(System.currentTimeMillis() + 5 * 60 * 1000);  
                    database.insertReservation(reservedSlot.getSlotId(), carID, reservedUntil);  

                    try {
                        Thread.sleep(500); 
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (reservedSlot.occupy()) {
                        System.out.println("Car " + carID + " occupied slot: " + reservedSlot.getSlotId());

                        try {
                            Thread.sleep(3000); 
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        reservedSlot.release();
                        System.out.println("Car " + carID + " left the slot.");
                    } else {
                        System.out.println("Car " + carID + " failed to occupy the reserved slot.");
                        reservedSlot.release();
                    }
                } else {
                    System.out.println("Car " + carID + " could not reserve any slot.");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                semaphore.release();  
            }
        } 

}

