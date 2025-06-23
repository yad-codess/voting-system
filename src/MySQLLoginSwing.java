import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MySQLLoginSwing {
    static final String URL = "jdbc:mysql://localhost:3306/votingsystem";
    static final String USER = "root";
    static final String PASSWORD = "yadu1020";

    public static void main(String[] args) {
        JFrame frame = new JFrame("MySQL Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 400);

        JPanel panel = new GradientPanel();
        panel.setLayout(new FlowLayout());

        JLabel userLabel = new JLabel("Username:");
        JTextField userText = new JTextField(20);
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordText = new JPasswordField(20);
        JLabel roleLabel = new JLabel("Role:");
        String[] roles = {"Voter", "Candidate", "Admin"};
        JComboBox<String> roleComboBox = new JComboBox<>(roles);
        JButton loginButton = new JButton("Login");
        JButton aadharButton = new JButton("Verify Aadhar");

        panel.add(userLabel);
        panel.add(userText);
        panel.add(passwordLabel);
        panel.add(passwordText);
        panel.add(roleLabel);
        panel.add(roleComboBox);
        panel.add(loginButton);
        panel.add(aadharButton);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = userText.getText();
                String password = new String(passwordText.getPassword());
                String selectedRole = (String) roleComboBox.getSelectedItem();

                if (login(username, password, selectedRole)) {
                    JOptionPane.showMessageDialog(null, "Login Successful!");
                    System.out.println("Logged in as: " + selectedRole);

                    if ("Voter".equals(selectedRole)) {
                        SwingUtilities.invokeLater(() -> new VotingSystemUI(new Voter(username)));
                    } else if ("Candidate".equals(selectedRole)) {
                        SwingUtilities.invokeLater(() -> new CandidateRegistrationUI(username));
                    } else if ("Admin".equals(selectedRole)) {
                        System.out.println("Redirecting to AdminPanel...");
                        SwingUtilities.invokeLater(() -> new AdminPanel().createAdminUI());
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid Username, Password, or Role.");
                }
            }
        });

        aadharButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String imagePath = JOptionPane.showInputDialog("Enter Aadhar image path:");
                if (imagePath != null && !imagePath.isEmpty()) {
                    String aadharNumber = extractAadharNumber(imagePath);
                    if (aadharNumber != null && !aadharNumber.isEmpty()) {
                        verifyAadharInDB(aadharNumber);
                    } else {
                        JOptionPane.showMessageDialog(null, "Could not extract Aadhar number.");
                    }
                }
            }
        });

        frame.add(panel);
        frame.setVisible(true);
    }

    private static boolean login(String username, String password, String role) {
        String query = "SELECT role FROM users WHERE username = ? AND password = ? AND role = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("User role in DB: " + rs.getString("role"));
                return true;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage());
        }
        return false;
    }

    private static String extractAadharNumber(String imagePath) {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata"); 
        tesseract.setLanguage("eng");

        try {
            String result = tesseract.doOCR(new File(imagePath));
            System.out.println(result);
            return filterAadharNumber(result); 
        } catch (TesseractException e) {
            System.out.println(imagePath);
            return null;
        }
    }
    private static String filterAadharNumber(String text) {
        // Regex for Aadhar number (12 consecutive digits)
        Pattern pattern = Pattern.compile("\\b\\d{12}\\b");
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group() : null;
    }

    private static void verifyAadharInDB(String aadharNumber) {
        String query = "SELECT * FROM voters WHERE aadhar = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = connection.prepareStatement(query)) {

            pstmt.setString(1, aadharNumber);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(null, "Aadhar verified successfully!");
                System.out.println("Aadhar exists in database.");
            } else {
                JOptionPane.showMessageDialog(null, "Aadhar not found in the database.");
                System.out.println("Aadhar not found.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Database Error: " + e.getMessage());
        }
    }

    static class GradientPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            Color color1 = new Color(135, 206, 235); // Light sky blue
            Color color2 = new Color(255, 182, 193); // Light pink
            GradientPaint gradientPaint = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
            g2d.setPaint(gradientPaint);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
