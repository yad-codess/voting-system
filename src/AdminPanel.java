import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminPanel {

    private static final String URL = "jdbc:mysql://localhost:3306/votingsystem";
    private static final String USER = "root";
    private static final String PASSWORD = "yadu1020";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminPanel().createAdminUI());
    }

    public void createAdminUI() {
        JFrame frame = new JFrame("Admin Panel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new GridLayout(4, 1, 10, 10));

        JButton addVoterButton = new JButton("Add Voter");
        JButton addCandidateButton = new JButton("Add Candidate");
        JButton viewUsersButton = new JButton("View Users");
        JButton viewResultsButton = new JButton("View Results");
        JButton logoutButton = new JButton("Logout");

        addVoterButton.addActionListener(e -> addVoter());
        addCandidateButton.addActionListener(e -> addCandidate());
        viewUsersButton.addActionListener(e -> viewUsers());
        viewResultsButton.addActionListener(e -> viewResults());

        logoutButton.addActionListener(e -> {
            frame.dispose(); // Close AdminPanel
            SwingUtilities.invokeLater(() -> MySQLLoginSwing.main(null)); // Reopen login page
        });

        frame.add(addVoterButton);
        frame.add(addCandidateButton);
        frame.add(viewUsersButton);
        frame.add(logoutButton);
        frame.add(viewResultsButton);

        frame.setVisible(true);
    }

    private void addVoter() {
        JTextField usernameField = new JTextField(10);
        JTextField aadharField = new JTextField(12);

        JPanel panel = new JPanel();
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(Box.createHorizontalStrut(15));
        panel.add(new JLabel("Aadhar:"));
        panel.add(aadharField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Add Voter", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String aadhar = aadharField.getText();

            if (aadhar.length() == 12) {
                String password = generatePassword(username, aadhar);
                addUserToDB(username, password, "Voter");
            } else {
                JOptionPane.showMessageDialog(null, "Invalid Aadhar number.");
            }
        }
    }

    private void viewResults() {
        String query = "SELECT party, COUNT(*) as vote_count FROM voters GROUP BY party ORDER BY vote_count DESC";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            ResultSet rs = pstmt.executeQuery();
            StringBuilder results = new StringBuilder("Voting Results:\n\n");

            int maxVotes = 0;
            String winningParty = "No votes cast"; // Default message if no votes found

            while (rs.next()) {
                String party = rs.getString("party");
                int voteCount = rs.getInt("vote_count");
                results.append(party).append(": ").append(voteCount).append(" votes\n");

                if (voteCount > maxVotes) {
                    maxVotes = voteCount;
                    winningParty = party;
                }
            }

            results.append("\nThe winning party is: ").append(winningParty);

            JOptionPane.showMessageDialog(null, results.toString());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error retrieving results: " + e.getMessage());
        }
    }

    private void addCandidate() {
        JTextField usernameField = new JTextField(10);
        JTextField nameField = new JTextField(20);
        JTextField localityField = new JTextField(20);
        JTextField partyField = new JTextField(15);
        JTextField aadharField = new JTextField(12);

        JPanel panel = new JPanel();
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(Box.createHorizontalStrut(15));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Aadhar no: "));
        panel.add(aadharField);
        panel.add(Box.createHorizontalStrut(15));
        panel.add(new JLabel("Locality:"));
        panel.add(localityField);
        panel.add(Box.createHorizontalStrut(15));
        panel.add(new JLabel("Party:"));
        panel.add(partyField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Add Candidate", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String name = nameField.getText();
            String locality = localityField.getText();
            String party = partyField.getText();
            String aadhar = aadharField.getText();

            String password = generatePassword(username, aadhar);
            addUserToDB(username, password, "Candidate");
            addCandidateToDB(username, name, locality, party);
        }
    }

    private String generatePassword(String username, String aadhar) {
        String firstTwoLetters = username.length() >= 2 ? username.substring(0, 2) : username;
        String lastFourDigits = aadhar.length() >= 4 ? aadhar.substring(aadhar.length() - 4) : aadhar;
        String password = firstTwoLetters + lastFourDigits;
        return password.length() > 10 ? password.substring(0, 10) : password;
    }

    private void addUserToDB(String username, String password, String role) {
        String query = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(null, role + " added successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error adding " + role + ": " + e.getMessage());
        }
    }

    private void addCandidateToDB(String username, String name, String locality, String party) {
        String query = "INSERT INTO candidates (username, name, locality, party) VALUES (?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, name);
            pstmt.setString(3, locality);
            pstmt.setString(4, party);
            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(null, "Candidate details added successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error adding candidate details: " + e.getMessage());
        }
    }

    private void viewUsers() {
        String query = "SELECT username, role FROM users";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            ResultSet rs = pstmt.executeQuery();
            StringBuilder userList = new StringBuilder("Users:\n\n");

            while (rs.next()) {
                String username = rs.getString("username");
                String role = rs.getString("role");
                userList.append("Username: ").append(username)
                        .append(", Role: ").append(role).append("\n");
            }

            if (userList.length() == 7) {
                JOptionPane.showMessageDialog(null, "No users found in the database.");
            } else {
                JOptionPane.showMessageDialog(null, userList.toString());
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error retrieving users: " + e.getMessage());
        }
    }
}
