package parkinglot;

import java.sql.Timestamp;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;

public class Slot {
    private final int slotID;
    private boolean isReserved;
    private boolean isOccupied;
    private int reservedBy;
    private long reservedUntil;

    private final long reservationDuration = 5 * 60 * 1000; 
    private final Database database;

    private final Lock mutex = new ReentrantLock();  
    
    public Slot(int slotID, Database database) {
        this.slotID = slotID;
        this.database = database;
    }

    public boolean reserve(int carID) {
        mutex.lock(); 
        try {
            if (!isReserved && !isOccupied) {
                isReserved = true;
                reservedBy = carID;
                reservedUntil = System.currentTimeMillis() + reservationDuration;

                System.out.println("Slot " + slotID + " reserved by Car " + carID);
                database.updateSlotStatus(slotID, true, false, carID, new Timestamp(reservedUntil));
                database.insertReservation(slotID, carID, new Timestamp(reservedUntil));
                return true;
            }
            return false;
        } finally {
            mutex.unlock(); 
        }
    }

    public boolean occupy() {
        mutex.lock();
        try {
            if (isReserved && !isOccupied && System.currentTimeMillis() < reservedUntil) {
                isOccupied = true;
                System.out.println("Slot " + slotID + " is occupied.");
                return true;
            }
            return false;
        } finally {
            mutex.unlock();
        }
    }

    public void release() {
        mutex.lock();
        try {
            isReserved = false;
            isOccupied = false;
            reservedBy = 0;
            System.out.println("Slot " + slotID + " is now empty.");
        } finally {
            mutex.unlock();
        }
    }

    public boolean isReserved() {
        mutex.lock();
        try {
            return isReserved;
        } finally {
            mutex.unlock();
        }
    }

    public boolean isOccupied() {
        mutex.lock();
        try {
            return isOccupied;
        } finally {
            mutex.unlock();
        }
    }

    public long getReservedUntil() {
        mutex.lock();
        try {
            return reservedUntil;
        } finally {
            mutex.unlock();
        }
    }

    public long getReservationTimeLeft() {
        mutex.lock();
        try {
            if (!isReserved) return 0;
            return Math.max(0, reservedUntil - System.currentTimeMillis());
        } finally {
            mutex.unlock();
        }
    }

    public int getSlotId() {
        return slotID;
    }
}
