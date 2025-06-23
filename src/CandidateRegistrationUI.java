import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CandidateRegistrationUI {
    JFrame frame;
    String username;

    public CandidateRegistrationUI(String username) {
        this.username = username;
        frame = new JFrame("Candidate Registration");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new GridLayout(5, 2));

        JLabel nameLabel = new JLabel("Name:");
        JTextField nameField = new JTextField(20);
        JLabel localityLabel = new JLabel("Locality:");
        JTextField localityField = new JTextField(20);
        JLabel partyLabel = new JLabel("Party:");
        JTextField partyField = new JTextField(20);
        JButton registerButton = new JButton("Register");
        JButton logoutButton = new JButton("Logout");

        frame.add(nameLabel);
        frame.add(nameField);
        frame.add(localityLabel);
        frame.add(localityField);
        frame.add(partyLabel);
        frame.add(partyField);
        frame.add(registerButton);
        frame.add(logoutButton);

        // Register button action listener
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText();
                String locality = localityField.getText();
                String party = partyField.getText();
                if (registerCandidate(username, name, locality, party)) {
                    JOptionPane.showMessageDialog(frame, "Registration Successful!");
                    frame.dispose(); // Close the registration window
                } else {
                    JOptionPane.showMessageDialog(frame, "Registration Failed.");
                }
            }
        });

        // Logout button action listener
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose(); // Close the registration window
                SwingUtilities.invokeLater(() -> MySQLLoginSwing.main(null)); // Reopen login screen
            }
        });

        frame.setVisible(true);
    }

    private boolean registerCandidate(String username, String name, String locality, String party) {
        String query = "INSERT INTO candidates (username, name, locality, party) VALUES (?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(MySQLLoginSwing.URL, MySQLLoginSwing.USER, MySQLLoginSwing.PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, name);
            pstmt.setString(3, locality);
            pstmt.setString(4, party);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage());
            return false;
        }
    }
}
