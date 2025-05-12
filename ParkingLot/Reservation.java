package parkinglot;

import javax.swing.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;

public class Reservation extends Thread {
    private final Parking parking;
    private final ParkingUI ui;
    private final Lock mutex = new ReentrantLock();

    public Reservation(Parking parking, ParkingUI ui) {
        this.parking = parking;
        this.ui = ui;
    }

    @Override
    public void run() {
        while (true) {
            mutex.lock();
            try {
                for (Slot slot : parking.getSlots()) {
                    if (slot.isReserved() && slot.getReservationTimeLeft() <= 0) {
                        slot.release();
                        System.out.println("Reservation expired for slot " + slot.getSlotId());
                        SwingUtilities.invokeLater(ui::updateSlotStatus);
                    }
                }
            } finally {
                mutex.unlock();
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}
        }
    }
}
