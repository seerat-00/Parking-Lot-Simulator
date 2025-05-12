package parkinglot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.sql.Timestamp;
import java.time.LocalDateTime;


public class ParkingUI {
    private Parking parking;
    private JFrame frame;
    private JPanel slotPanel;
    private JLabel adminLabel;
    private Map<Slot, JPanel> slotComponents = new HashMap<>();
    private Map<Slot, JLabel> timeLabels = new HashMap<>();
     private Database database;

    public ParkingUI(Parking parking, Database database) {
        this.database = database;
        this.parking = parking;
        initializeUI();
        new Reservation(parking, this).start(); 
        startTimerUpdate(); 
    }

    private void initializeUI() {
        frame = new JFrame("Parking Lot UI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 650);
        frame.setLayout(new BorderLayout());

        slotPanel = new JPanel(new GridLayout(0, 4, 20, 20));

        for (Slot slot : parking.getSlots()) {
            JPanel panel = createSlotPanel(slot);
            slotPanel.add(panel);
            slotComponents.put(slot, panel);
        }

        JScrollPane scrollPane = new JScrollPane(slotPanel);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(new Color(50, 50, 50));
        JLabel footerLabel = new JLabel("Parking Lot Management System", JLabel.CENTER);
        footerLabel.setForeground(Color.WHITE);
        footerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        footerPanel.add(footerLabel, BorderLayout.NORTH);

        adminLabel = new JLabel("Occupied: 0 | Reserved: 0", JLabel.CENTER);
        adminLabel.setForeground(Color.YELLOW);
        adminLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        footerPanel.add(adminLabel, BorderLayout.SOUTH);

        frame.add(footerPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private JPanel createSlotPanel(Slot slot) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 3));
        panel.setBackground(new Color(245, 245, 245));
        panel.setPreferredSize(new Dimension(220, 180));

        JLabel label = new JLabel("Slot " + slot.getSlotId(), SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(label, BorderLayout.NORTH);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(new Color(245, 245, 245));
        JLabel statusLabel = new JLabel("Status: Available", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        statusPanel.add(statusLabel, BorderLayout.NORTH);

        JLabel timeLabel = new JLabel("", SwingConstants.CENTER);
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusPanel.add(timeLabel, BorderLayout.CENTER); 

        panel.add(statusPanel, BorderLayout.SOUTH);  
        panel.putClientProperty("statusLabel", statusLabel);

        timeLabels.put(slot, timeLabel);  

        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        buttonPanel.setBackground(new Color(245, 245, 245));

        buttonPanel.add(createButton("Reserve", Color.GRAY, e -> reserveSlot(slot)));
        buttonPanel.add(createButton("Occupy", Color.LIGHT_GRAY, e -> occupySlot(slot)));
        buttonPanel.add(createButton("Leave", Color.GRAY, e -> leaveSlot(slot)));

        panel.add(buttonPanel, BorderLayout.CENTER);
        return panel;
    }

    private JButton createButton(String text, Color color, ActionListener listener) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setForeground(new Color(40, 40, 40));
        button.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2, true));
        button.setPreferredSize(new Dimension(180, 40));
        button.addActionListener(listener);
        return button;
    }

    private void reserveSlot(Slot slot) {
        int carId = database.insertCar();  

        if (carId != -1) {
            if (slot.reserve(carId)) {  
                System.out.println("Reserved slot " + slot.getSlotId() + " for car " + carId);

                Timestamp reservedUntil = Timestamp.valueOf(LocalDateTime.now().plusHours(2));

                database.updateSlotStatus(slot.getSlotId(), true, false, carId, reservedUntil);

                database.insertReservation(slot.getSlotId(), carId, reservedUntil);

                double amount = 20.00;
                database.processPayment(carId, amount);

                JOptionPane.showMessageDialog(frame,
                        "Reservation confirmed.\nSlot: " + slot.getSlotId() +
                        "\nCar ID: " + carId +
                        "\nAmount Paid: $" + amount,
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);

            } else {
                JOptionPane.showMessageDialog(frame,
                        "Cannot reserve slot " + slot.getSlotId() + ". It may already be reserved.",
                        "Reservation Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(frame,
                    "Failed to insert car. Try again.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        updateSlotStatus();
    }



    private void occupySlot(Slot slot) {
        if (slot.occupy()) {
            System.out.println("Occupied slot " + slot.getSlotId());
        } else {
            System.out.println("Cannot occupy slot " + slot.getSlotId());
        }
        updateSlotStatus();
    }

    private void leaveSlot(Slot slot) {
        slot.release();
        updateSlotStatus();
        System.out.println("Slot " + slot.getSlotId() + " released.");
    }

    public void updateSlotStatus() {
        int reservedCount = 0;
        int occupiedCount = 0;

        for (Slot slot : parking.getSlots()) {
            JPanel panel = slotComponents.get(slot);
            JLabel statusLabel = (JLabel) panel.getClientProperty("statusLabel");
            JLabel timeLabel = timeLabels.get(slot);

            SwingUtilities.invokeLater(() -> {
                if (slot.isOccupied()) {
                    panel.setBackground(new Color(255, 94, 94));
                    if (statusLabel != null) statusLabel.setText("Status: Occupied");
                    if (timeLabel != null) timeLabel.setText("");
                } else if (slot.isReserved()) {
                    panel.setBackground(new Color(255, 255, 102));
                    if (statusLabel != null) statusLabel.setText("Status: Reserved");
                    long timeLeft = slot.getReservationTimeLeft();
                    if (timeLabel != null)
                        timeLabel.setText(formatTimeLeft(timeLeft));
                } else {
                    panel.setBackground(new Color(204, 255, 204));
                    if (statusLabel != null) statusLabel.setText("Status: Available");
                    if (timeLabel != null) timeLabel.setText("");
                }
            });

            if (slot.isOccupied()) occupiedCount++;
            if (slot.isReserved()) reservedCount++;
        }

        final int occ = occupiedCount;
        final int res = reservedCount;
        SwingUtilities.invokeLater(() -> 
            adminLabel.setText("Occupied: " + occ + " | Reserved: " + res));
    }

    private String formatTimeLeft(long millis) {
        long seconds = millis / 1000;
        long min = seconds / 60;
        long sec = seconds % 60;
        return String.format("Time Left: %02d:%02d", min, sec);
    }

    private void startTimerUpdate() {
        new Timer(1000, e -> updateSlotStatus()).start(); 
    }
}