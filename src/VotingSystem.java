import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class VotingSystem {
    static final String URL = "jdbc:mysql://localhost:3306/votingsystem"; 
    static final String USER = "root"; 
    static final String PASSWORD = "yadu1020"; 

    public static void vote(Voter voter, String party) {
        voter.vote(party);
    }
}

class Voter {
    private String username;

    public Voter(String username) {
        this.username = username;
    }

    public void vote(String party) {
        registerVoteInDB(party);
    }

    private void registerVoteInDB(String party) {
        String query = "INSERT INTO voters (username, party) VALUES (?, ?)";

        try (Connection connection = DriverManager.getConnection(VotingSystem.URL, VotingSystem.USER, VotingSystem.PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, party);
            pstmt.executeUpdate();
            System.out.println("Vote registered for party: " + party);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage());
            System.out.println("Could not vote");
        }
    }
}

class VotingSystemUI {
    private JFrame frame;
    private Voter voter;

    public VotingSystemUI(Voter voter) {
        this.voter = voter;
        frame = new JFrame("Voting System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new GridLayout(3, 2));

        // Symbols and buttons for five political parties
        String[] parties = {"BJP", "INC", "AAP", "TMC", "CPI"};
        String[] symbols = {
            "C:\\Users\\Yadushree\\Pictures\\Screenshots\\bjp.png", 
            "C:\\Users\\Yadushree\\Pictures\\Screenshots\\inc.png",
            "C:\\Users\\Yadushree\\Pictures\\Screenshots\\aap.png",
            "C:\\Users\\Yadushree\\Pictures\\Screenshots\\tmc.png",
            "C:\\Users\\Yadushree\\Pictures\\Screenshots\\cpi.png"
        };

        for (int i = 0; i < parties.length; i++) {
            JPanel partyPanel = new JPanel();
            partyPanel.setLayout(new BoxLayout(partyPanel, BoxLayout.Y_AXIS)); 

            ImageIcon partyIcon = new ImageIcon(symbols[i]);
            Image scaledImage = partyIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH); // Scale to desired size
            JButton partyButton = new JButton(new ImageIcon(scaledImage));
            partyButton.setActionCommand(parties[i]);
            partyButton.addActionListener(new VoteActionListener());

            JLabel partyLabel = new JLabel(parties[i]);
            partyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            partyButton.setAlignmentX(Component.CENTER_ALIGNMENT);

            partyPanel.add(partyButton);
            partyPanel.add(partyLabel);
            frame.add(partyPanel);
        }

        // Add logout button
        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(new LogoutActionListener());
        frame.add(logoutButton);

        frame.setVisible(true);
    }

    private class VoteActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String selectedParty = e.getActionCommand();
            VotingSystem.vote(voter, selectedParty);
            JOptionPane.showMessageDialog(frame, "You voted for: " + selectedParty);
        }
    }

    private class LogoutActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            frame.dispose(); // Close the voting window
            SwingUtilities.invokeLater(() -> MySQLLoginSwing.main(null)); // Reopen the login screen
        }
    }
}
