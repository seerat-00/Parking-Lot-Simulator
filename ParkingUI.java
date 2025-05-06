package parkinglot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class ParkingUI {
    private Parking parking;
    private JFrame frame;
    private JPanel slotPanel;
    private Map<Slot, JPanel> slotComponents = new HashMap<>();
    private int carCounter = 0;

    public ParkingUI(Parking parking) {
        this.parking = parking;
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("Parking Lot UI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 650); // Enhanced window size for better layout
        frame.setLayout(new BorderLayout());

        // Create a JPanel for the slots with a grid layout
        slotPanel = new JPanel(new GridLayout(0, 4, 20, 20)); // Increased spacing for better clarity

        for (Slot slot : parking.getSlots()) {
            JPanel panel = createSlotPanel(slot);
            slotPanel.add(panel);
            slotComponents.put(slot, panel);
        }

        JScrollPane scrollPane = new JScrollPane(slotPanel); // Adding a scroll bar in case slots overflow
        frame.add(scrollPane, BorderLayout.CENTER);

        // Adding the footer for extra information or controls if needed
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(50, 50, 50));
        JLabel footerLabel = new JLabel("Parking Lot Management System", JLabel.CENTER);
        footerLabel.setForeground(Color.WHITE);
        footerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        footerPanel.add(footerLabel);
        frame.add(footerPanel, BorderLayout.SOUTH);

        frame.setVisible(true);

        // Periodic update to the slot status
        new Thread(() -> {
            while (true) {
                updateSlotStatus();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private JPanel createSlotPanel(Slot slot) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 3)); // Slightly darker border for better contrast
        panel.setBackground(new Color(245, 245, 245)); // Subtle gray background for slots
        panel.setPreferredSize(new Dimension(220, 180)); // Consistent panel sizes for uniformity
        panel.setLayout(new BorderLayout(10, 10));

        // Slot ID Label
        JLabel label = new JLabel("Slot " + slot.getSlotId(), SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        label.setForeground(new Color(60, 60, 60)); // Dark gray text
        panel.add(label, BorderLayout.NORTH);

        // Button panel with margin for separation
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        buttonPanel.setBackground(new Color(245, 245, 245));

        // Reserve Button
        JButton reserveBtn = createButton("Reserve", Color.GRAY, e -> reserveSlot(slot));
        buttonPanel.add(reserveBtn);

        // Occupy Button
        JButton occupyBtn = createButton("Occupy", Color.LIGHT_GRAY, e -> occupySlot(slot));
        buttonPanel.add(occupyBtn);

        // Leave Button
        JButton leaveBtn = createButton("Leave", Color.GRAY, e -> leaveSlot(slot));
        buttonPanel.add(leaveBtn);

        panel.add(buttonPanel, BorderLayout.CENTER);

        return panel;
    }

    // Create a button with hover effect and rounded corners
    private JButton createButton(String text, Color color, ActionListener actionListener) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setForeground(new Color(40, 40, 40));
        button.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2, true));
        button.setPreferredSize(new Dimension(180, 40));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(180, 180, 180)); // Light hover effect
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color); // Reset to original color
            }
        });

        button.addActionListener(actionListener);
        return button;
    }

    private void reserveSlot(Slot slot) {
        if (slot.reserve("ManualCar")) {
            System.out.println("Manually reserved slot " + slot.getSlotId());
        } else {
            System.out.println("Slot " + slot.getSlotId() + " is not available to reserve.");
        }
        updateSlotStatus();
    }

    private void occupySlot(Slot slot) {
        if (slot.occupy()) {
            System.out.println("Manually occupied slot " + slot.getSlotId());
        } else {
            System.out.println("Cannot occupy slot " + slot.getSlotId());
        }
        updateSlotStatus();
    }

    private void leaveSlot(Slot slot) {
        slot.release();
        updateSlotStatus();
        System.out.println("Slot " + slot.getSlotId() + " is now empty and available for manual reservation.");
    }

    public void updateSlotStatus() {
        for (Slot slot : parking.getSlots()) {
            JPanel panel = slotComponents.get(slot);
            if (slot.isOccupied()) {
                panel.setBackground(new Color(255, 94, 94)); // Red for occupied
            } else if (slot.isReserved()) {
                panel.setBackground(new Color(255, 255, 102)); // Yellow for reserved
            } else {
                panel.setBackground(new Color(204, 255, 204)); // Light green for available
            }
        }
    }

    public static void main(String[] args) {
        Parking parking = new Parking(8); // 8 slots
        new Reservation(parking).start(); // Starts reservation expiration checker
        SwingUtilities.invokeLater(() -> new ParkingUI(parking));
    }
}