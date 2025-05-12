package parkinglot;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Parking {
    private final List<Slot> slots;
    private final Database database;
    private final Lock mutex = new ReentrantLock();

    public Parking(int numSlots, Database database) {
        this.database = database;
        this.slots = new ArrayList<>();

        for (int i = 0; i < numSlots; i++) {
            database.insertParkingSlot(i + 1);
            slots.add(new Slot(i + 1, database));
        }
    }

    public Slot reserveSlot(int carID) {
        mutex.lock();
        try {
            for (Slot slot : slots) {
                if (!slot.isReserved() && !slot.isOccupied()) {
                    if (slot.reserve(carID)) {
                        System.out.println("Slot " + slot.getSlotId() + " successfully reserved by Car " + carID);
                        return slot;
                    }
                }
            }
            System.out.println("No available slot for Car " + carID);
            return null;
        } finally {
            mutex.unlock();
        }
    }

    public List<Slot> getSlots() {
        mutex.lock();
        try {
            return new ArrayList<>(slots);
        } finally {
            mutex.unlock();
        }
    }
} 
