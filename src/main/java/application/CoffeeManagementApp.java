package application;

import com.formdev.flatlaf.FlatLightLaf;
import network.Client;
import ui.LoginFrame;

import javax.swing.*;

public class CoffeeManagementApp {
    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf: " + ex.getMessage());
        }

        try {
            System.out.println("Starting Coffee Management Application...");
            Client client = new Client();

            client.connect();
            System.out.println("Connected to server successfully.");

            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame(client);
                loginFrame.setVisible(true);
            });

        } catch (Exception e) {
            String errorMsg = "Không thể kết nối đến Server!";

            JOptionPane.showMessageDialog(null, errorMsg, "Lỗi Kết Nối", JOptionPane.ERROR_MESSAGE);
            System.err.println("Connection error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
