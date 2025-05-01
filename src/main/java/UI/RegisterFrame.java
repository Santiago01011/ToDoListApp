package UI;

import COMMON.UserProperties;
import COMMON.common;
import controller.UserController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class RegisterFrame extends Frame {
    private UserController userController;
    private JLabel titleLabel;

    public RegisterFrame(String title) {
        super(title);
        setTitle(title);
        setSize(400, 400);
        setLocationRelativeTo(null);
        setLayout(null);
        setResizable(false);
        addRegisterUIComponents();

    }

    public void setController(UserController userController) {
        this.userController = userController;
    }

    public void setUserController(UserController userController) {
        this.userController = userController;
    }

    private void addRegisterUIComponents() {

        titleLabel = new JLabel("Register", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 25));
        titleLabel.setForeground(common.getTextColor());
        titleLabel.setBounds(140, 50, 100, 30);

        JTextField usernameField = new JTextField("Username");
        usernameField.setFont(new Font("Dialog", Font.PLAIN, 15));
        usernameField.setForeground(common.getTextColor());
        usernameField.setBounds(90, 100, 200, 30);
        usernameField.setBackground(common.getTertiaryColor());

        JTextField emailField = new JTextField("Email");
        emailField.setFont(new Font("Dialog", Font.PLAIN, 15));
        emailField.setForeground(common.getTextColor());
        emailField.setBounds(90, 150, 200, 30);
        emailField.setBackground(common.getTertiaryColor());

        JPasswordField passwordField = new JPasswordField("Password");
        passwordField.setFont(new Font("Dialog", Font.PLAIN, 15));
        passwordField.setForeground(common.getTextColor());
        passwordField.setBounds(90, 200, 200, 30);
        passwordField.setBackground(common.getTertiaryColor());

        JPasswordField rePasswordField = new JPasswordField("Password");
        rePasswordField.setFont(new Font("Dialog", Font.PLAIN, 15));
        rePasswordField.setForeground(common.getTextColor());
        rePasswordField.setBounds(90, 250, 200, 30);
        rePasswordField.setBackground(common.getTertiaryColor());

        ImageIcon toggleColorIcon = common.getModeIcon();
        JButton toggleColorButton = new JButton(toggleColorIcon);
        toggleColorButton.setBackground(common.getSecondaryColor());
        toggleColorButton.setForeground(common.getTextColor());
        toggleColorButton.setFont(new Font("Dialog", Font.BOLD, 12));
        toggleColorButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleColorButton.setBounds(340, 20, 30, 30);
        toggleColorButton.setToolTipText("Toggle color mode");

        ImageIcon backIcon = common.getBackIcon();
        JButton backButton = new JButton(backIcon);
        backButton.setBackground(common.getSecondaryColor());
        backButton.setForeground(common.getTextColor());
        backButton.setFont(new Font("Dialog", Font.BOLD, 12));
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.setBounds(15, 20, 30, 30);
        backButton.setToolTipText("Back");

        JButton registerButton = new JButton("Register");
        registerButton.setBackground(common.getSecondaryColor());
        registerButton.setForeground(common.getTextColor());
        registerButton.setFont(new Font("Dialog", Font.BOLD, 12));
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerButton.setBounds(140, 300, 100, 30);

        addFocusListeners(usernameField, "Username");
        addFocusListeners(emailField, "Email");
        addFocusListeners(passwordField, "Password");
        addFocusListeners(rePasswordField, "Password");
        addRegisterActionListeners(usernameField, emailField, passwordField, rePasswordField, toggleColorButton,
                backButton, registerButton);

        add(titleLabel);
        add(usernameField);
        add(emailField);
        add(passwordField);
        add(rePasswordField);
        add(toggleColorButton);
        add(backButton);
        add(registerButton);

    }

    private void addRegisterActionListeners(JTextField usernameField, JTextField emailField,
            JPasswordField passwordField, JPasswordField rePasswordField, JButton toggleColorButton, JButton backButton,
            JButton registerButton) {

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(() -> {
                    LoginFrame loginFrame = new LoginFrame("Login");
                    loginFrame.setController(new UserController());
                    loginFrame.setVisible(true);
                    dispose();
                });
            }
        });

        toggleColorButton.addActionListener(e -> {
            common.toggleColorMode();
            UserProperties.setProperty("darkTheme", String.valueOf(common.useNightMode));
            refreshTheme();
            titleLabel.setForeground(common.getTextColor());
            usernameField.setBackground(common.getTertiaryColor());
            usernameField.setForeground(common.getTextColor());
            emailField.setBackground(common.getTertiaryColor());
            emailField.setForeground(common.getTextColor());
            passwordField.setBackground(common.getTertiaryColor());
            passwordField.setForeground(common.getTextColor());
            rePasswordField.setBackground(common.getTertiaryColor());
            rePasswordField.setForeground(common.getTextColor());
            backButton.setBackground(common.getSecondaryColor());
            backButton.setIcon(common.getBackIcon());
            registerButton.setBackground(common.getSecondaryColor());
            registerButton.setForeground(common.getTextColor());
            toggleColorButton.setBackground(common.getPrimaryColor());
            toggleColorButton.setIcon(common.getModeIcon());
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String email = emailField.getText();
                String password = new String(passwordField.getPassword());
                String rePassword = new String(rePasswordField.getPassword());
                if (username.equals("Username") || email.equals("Email") || password.equals("Password")
                        || rePassword.equals("Password")) {
                    JOptionPane.showMessageDialog(null, "Please fill in all fields", "Error",
                            JOptionPane.ERROR_MESSAGE);
                } else if (!password.equals(rePassword)) {
                    JOptionPane.showMessageDialog(null, "Passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    doRegister(username, email, password);
                }
            }
        });

        usernameField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String email = emailField.getText();
                String password = new String(passwordField.getPassword());
                String rePassword = new String(rePasswordField.getPassword());
                if (username.equals("Username") || email.equals("Email") || password.equals("Password")
                        || rePassword.equals("Password")) {
                    JOptionPane.showMessageDialog(null, "Please fill in all fields", "Error",
                            JOptionPane.ERROR_MESSAGE);
                } else if (!password.equals(rePassword)) {
                    JOptionPane.showMessageDialog(null, "Passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    doRegister(username, email, password);
                }
            }
        });

        emailField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String email = emailField.getText();
                String password = new String(passwordField.getPassword());
                String rePassword = new String(rePasswordField.getPassword());
                if (username.equals("Username") || email.equals("Email") || password.equals("Password")
                        || rePassword.equals("Password")) {
                    JOptionPane.showMessageDialog(null, "Please fill in all fields", "Error",
                            JOptionPane.ERROR_MESSAGE);
                } else if (!password.equals(rePassword)) {
                    JOptionPane.showMessageDialog(null, "Passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    doRegister(username, email, password);
                }
            }
        });

        rePasswordField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String email = emailField.getText();
                String password = new String(passwordField.getPassword());
                String rePassword = new String(rePasswordField.getPassword());
                if (username.equals("Username") || email.equals("Email") || password.equals("Password")
                        || rePassword.equals("Password")) {
                    JOptionPane.showMessageDialog(null, "Please fill in all fields", "Error",
                            JOptionPane.ERROR_MESSAGE);
                } else if (!password.equals(rePassword)) {
                    JOptionPane.showMessageDialog(null, "Passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    doRegister(username, email, password);
                }
            }
        });

    }

    private void doRegister(String username, String email, String password) {
        userController.setUserName(username);
        userController.setUserEmail(email);
        userController.setPassword(password);
        boolean success = userController.doRegister();
        if (success) {
            JOptionPane.showMessageDialog(this, "Registration successful! Check your email to validate the account.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame("Login");
                loginFrame.setController(new UserController());
                loginFrame.setVisible(true);
                dispose();
            });
        } else {
            JOptionPane.showMessageDialog(this, "Registration failed. Please try again.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

}