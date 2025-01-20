package UI;

import net.miginfocom.swing.*;
import javax.swing.*;
import java.awt.*;
import COMMON.common;
import DBH.TaskDAO;
import COMMON.UserProperties;
import java.text.SimpleDateFormat;
import javax.swing.SwingUtilities;

public class LoginFrame extends Frame {
    private static boolean keepLoggedIn = Boolean.valueOf((String) UserProperties.getProperty("rememberMe"));
    private String username = (String) UserProperties.getProperty("username");
    private String password = (String) UserProperties.getProperty("password");
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JCheckBox keepLoggedInCheckBox;
    private JLabel titleLabel;
    private JButton toggleColorButton;
    private JLabel registerLabel;

    public LoginFrame(String title) {
        super(title);
        setSize(400, 350);
        setLocationRelativeTo(null);
        setLayout(new MigLayout("fill, insets 30", "[grow]", "[]20[]10[]20[]20[]20[]"));
        setResizable(false);
        setupComponents();
    }

    private void setupComponents() {
        JPanel headerPanel = new JPanel(new MigLayout("insets 0", "[]push[]", "[]"));
        headerPanel.setOpaque(false);
        
        titleLabel = new JLabel("Login");
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 30));
        
        toggleColorButton = new JButton(common.getModeIcon());
        toggleColorButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleColorButton.setToolTipText("Toggle color mode");
        
        headerPanel.add(titleLabel);
        headerPanel.add(toggleColorButton);

        usernameField = new JTextField("Username");
        passwordField = new JPasswordField("Password");
        keepLoggedInCheckBox = new JCheckBox("Keep me logged in");
        keepLoggedInCheckBox.setFont(new Font("Consolas", Font.PLAIN, 14));
        keepLoggedInCheckBox.setOpaque(false);
        
        loginButton = new JButton("Login");
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        registerLabel = new JLabel("Don't have an account? Register here");
        registerLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        updateComponentColors();
        
        add(headerPanel, "growx, wrap");
        add(usernameField, "growx, h 30!, wrap");
        add(passwordField, "growx, h 30!, wrap");
        add(keepLoggedInCheckBox, "left, wrap");
        add(loginButton, "w 100!, center, wrap");
        add(registerLabel, "center");

        addFocusListeners(usernameField, "Username");
        addFocusListeners(passwordField, "Password");

        toggleColorButton.addActionListener(e -> {
            common.toggleColorMode();
            updateComponentColors();
            SwingUtilities.updateComponentTreeUI(this);
        });

        setupActionListeners(registerLabel);

        SwingUtilities.invokeLater(() -> {
            titleLabel.requestFocusInWindow();
            String rememberMe = (String) UserProperties.getProperty("rememberMe");
            if (rememberMe != null && !rememberMe.equals("false")) {
                usernameField.setText(username);
                passwordField.setText(password);
                doLogin(usernameField, passwordField);
            }
        });
    }

    private void updateComponentColors() {
        usernameField.setBackground(common.getTertiaryColor());
        usernameField.setForeground(common.getTextColor());
        passwordField.setBackground(common.getTertiaryColor());
        passwordField.setForeground(common.getTextColor());
        loginButton.setBackground(common.getSecondaryColor());
        loginButton.setForeground(common.getTextColor());
        keepLoggedInCheckBox.setForeground(common.getTextColor());
        toggleColorButton.setBackground(common.getPrimaryColor());
        toggleColorButton.setIcon(common.getModeIcon());
        registerLabel.setForeground(common.getTextColor());
        titleLabel.setForeground(common.getTextColor());

        getContentPane().setBackground(common.getPrimaryColor());
        repaint();
    }

    private void setupActionListeners(JLabel registerLabel) {
        loginButton.addActionListener(e -> {
            if (!usernameField.getText().equals("Username")) {
                doLogin(usernameField, passwordField);
            }
        });

        usernameField.addActionListener(e -> {
            if (!usernameField.getText().isEmpty()) {
                doLogin(usernameField, passwordField);
            }
        });

        passwordField.addActionListener(e -> {
            if (!new String(passwordField.getPassword()).isEmpty()) {
                doLogin(usernameField, passwordField);
            }
        });

        registerLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                dispose();
                SwingUtilities.invokeLater(() -> {
                    new RegisterFrame("Register").setVisible(true);
                });
            }
        });

        keepLoggedInCheckBox.addActionListener(e -> keepLoggedIn = !keepLoggedIn);
    }

    private void doLogin(JTextField usernameField, JPasswordField passwordField) {
        if (TaskDAO.validateUserFromDatabase(usernameField.getText(), new String(passwordField.getPassword()))) {
            dispose();
            String usernameLogged = usernameField.getText();
            SwingUtilities.invokeLater(() -> {
                new TasksFrame("ToDoList", TaskDAO.getUserId(usernameLogged)).setVisible(true);
            });
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password", "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        if (keepLoggedIn) {
            UserProperties.setProperty("username", usernameField.getText());
            UserProperties.setProperty("password", new String(passwordField.getPassword()));
            UserProperties.setProperty("lastSession", new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
            UserProperties.setProperty("rememberMe", "true");
        } else {
            UserProperties.logOut();
        }
        
        usernameField.setText("Username");
        passwordField.setText("Password");
    }
}