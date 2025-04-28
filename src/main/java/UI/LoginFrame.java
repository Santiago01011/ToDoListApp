package UI;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import COMMON.UserProperties;
import COMMON.common;
import controller.TaskController;
import controller.UserController;
import model.TaskHandler;
import DBH.NewDBHandler;
import UI.TaskDashboardFrame;
import net.miginfocom.swing.MigLayout;
import javax.swing.JOptionPane;

public class LoginFrame extends Frame{
    private UserController userController;


    public LoginFrame(String title) {
        super(title);
        setResizable(false);
        setSize(400, 350);
        setLocationRelativeTo(null);
        setLayout(new MigLayout("fill, insets 30", "[grow]", "[]20[]10[]20[]20[]20[]"));
        setupComponents();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Shutting down application...");
                System.exit(0);
            }
        });
    }

    public void setController(UserController userController) {
        this.userController = userController;
    }

    private void setupComponents() {
        JPanel headerPanel = new JPanel(new MigLayout("insets 0", "[]push[]", "[]"));
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Login");
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 30));
        
        JButton toggleColorButton = new JButton(common.getModeIcon());
        toggleColorButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleColorButton.setToolTipText("Toggle color mode");
        
        headerPanel.add(titleLabel);
        headerPanel.add(toggleColorButton);

        JTextField usernameField = new JTextField("Username");
        JPasswordField passwordField = new JPasswordField("Password");
        JCheckBox keepLoggedInCheckBox = new JCheckBox("Keep me logged in");
        keepLoggedInCheckBox.setFont(new Font("Consolas", Font.PLAIN, 14));
        keepLoggedInCheckBox.setForeground(common.getTextColor());
        keepLoggedInCheckBox.setOpaque(false);
        
        JButton loginButton = new JButton("Login");
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel registerLabel = new JLabel("Don't have an account? Register here");
        registerLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        
        add(headerPanel, "growx, wrap");
        add(usernameField, "growx, h 30!, wrap");
        add(passwordField, "growx, h 30!, wrap");
        add(keepLoggedInCheckBox, "left, wrap");
        add(loginButton, "w 100!, center, wrap");
        add(registerLabel, "center");

        addFocusListeners(usernameField, "Username");
        addFocusListeners(passwordField, "Password");

        usernameField.addActionListener(e -> loginButton.doClick());
        passwordField.addActionListener(e -> loginButton.doClick());

        registerLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
            //userController.showRegistration();
            LoginFrame.this.dispose();
            }
        });

        // toggleColorButton.addActionListener(e -> {
        //     common.toggleColorMode();
        //     SwingUtilities.updateComponentTreeUI(this);
        //     toggleColorButton.setIcon(common.getModeIcon());
        //     keepLoggedInCheckBox.setForeground(common.getTextColor());
        // });
       

        loginButton.addActionListener(e -> {
            userController.setUserName(usernameField.getText());
            userController.setPassword(new String(passwordField.getPassword()));
            boolean remember = keepLoggedInCheckBox.isSelected();
            userController.setKeepLoggedIn(remember);
            UserProperties.setProperty("rememberMe", Boolean.toString(remember));
            boolean success = userController.doLogin();
            if (success) {
                userController.launchDashboard(LoginFrame.this);
            } else {
                JOptionPane.showMessageDialog(LoginFrame.this,
                    "Login failed. Please check your credentials and try again.",
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        SwingUtilities.invokeLater(() -> {
            titleLabel.requestFocusInWindow();
            if (userController.getKeepLoggedIn()) {
                usernameField.setText(userController.getUserName());
                passwordField.setText(UserProperties.getProperty("password").toString());
                keepLoggedInCheckBox.setSelected(true);
                loginButton.doClick();
            } else {
                this.setVisible(true);
            }
        });
    }
    
}
