package parkinglot;

public class Car extends Thread {
    private Parking parking;
    private int carID;
    private Slot reservedSlot;

    public Car(Parking parking, int carID) {
        this.parking = parking;
        this.carID = carID;
    }

    @Override
    public void run() {
        reservedSlot = parking.reserveSlot("Car" + carID);
        if (reservedSlot != null) {
            System.out.println("Car " + carID + " reserved slot: " + reservedSlot.getSlotId());

            try {
                Thread.sleep(500); // Simulate delay before occupying
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (reservedSlot.occupy()) {
                System.out.println("Car " + carID + " occupied slot: " + reservedSlot.getSlotId());

                try {
                    Thread.sleep(3000); // Simulate parking duration
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
    }
}