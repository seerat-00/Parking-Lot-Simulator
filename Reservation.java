package parkinglot;

public class Reservation extends Thread {
    private Parking parking;

    public Reservation(Parking parking) {
        this.parking = parking;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000); // Check every second
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            for (Slot slot : parking.getSlots()) {
                if (slot.isReserved() && System.currentTimeMillis() > slot.getReservedUntil()) {
                    System.out.println("Reservation expired for slot " + slot.getSlotId());
                    slot.release();
                }
            }
        }
    }
}