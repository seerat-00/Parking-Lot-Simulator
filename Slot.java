package parkinglot;

public class Slot {
    private int slotID;
    private boolean isReserved;
    private boolean isOccupied;
    private String reservedBy;
    private long reservedUntil;

    public Slot(int slotID) {
        this.slotID = slotID;
        this.isReserved = false;
        this.isOccupied = false;
    }

    public synchronized boolean reserve(String carID) {
        if (!isReserved && !isOccupied) {
            this.isReserved = true;
            this.reservedBy = carID;
            this.reservedUntil = System.currentTimeMillis() + 5 * 60 * 1000;
            System.out.println("Slot " + slotID + " reserved by " + carID);
            return true;
        }
        return false;
    }

    public synchronized boolean occupy() {
        if (isReserved && !isOccupied && System.currentTimeMillis() < reservedUntil) {
            this.isOccupied = true;
            System.out.println("Slot " + slotID + " is occupied.");
            return true;
        }
        return false;
    }

    public synchronized void release() {
        this.isReserved = false;
        this.isOccupied = false;
        this.reservedBy = null;
        System.out.println("Slot " + slotID + " is now empty.");
    }

    public synchronized boolean isReserved() {
        return isReserved;
    }

    public synchronized boolean isOccupied() {
        return isOccupied;
    }

    public synchronized long getReservedUntil() {
        return reservedUntil;
    }

    public int getSlotId() {
        return slotID;
    }
}
