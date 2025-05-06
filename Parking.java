package parkinglot;

import java.util.List;
import java.util.ArrayList;

public class Parking {
    private List<Slot> slots;

    public Parking(int numSlots) {
        slots = new ArrayList<>();
        for (int i = 0; i < numSlots; i++) {
            slots.add(new Slot(i + 1));
        }
    }

    public synchronized Slot reserveSlot(String carID) {
        for (Slot slot : slots) {
            if (!slot.isReserved() && !slot.isOccupied()) {
                if (slot.reserve(carID)) {
                    return slot;
                }
            }
        }
        return null;
    }

    public List<Slot> getSlots() {
        return slots;
    }
}