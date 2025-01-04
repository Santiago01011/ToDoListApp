package UI;

import COMMON.common;
import DBH.TaskDAO;
import COMMON.UserProperties;


import javax.swing.*;

import java.awt.*;
import java.text.SimpleDateFormat;


public class LoginFrame extends Frame{

    private static boolean keepLoggedIn = Boolean.valueOf((String) UserProperties.getProperty("rememberMe"));
    private String username = (String) UserProperties.getProperty("username");
    private String password = (String) UserProperties.getProperty("password");
    
        public LoginFrame(String title){
            super(title);
            setTitle(title);
            setSize(400,350);
            setLocationRelativeTo(null);            
            setLayout(null);
            setResizable(false);
            addLoginUIComponents();
        }
    
        private void addLoginUIComponents(){
            JLabel titleLabel = new JLabel("Login");
            titleLabel.setFont(new Font("Dialog", Font.BOLD, 30));
            titleLabel.setBounds(150, 30, 100, 35);
    
            JTextField usernameField = new JTextField("Username");
            usernameField.setBounds(100, 90, 200, 30);
    
            JPasswordField passwordField = new JPasswordField("Password");
            passwordField.setBounds(100, 140, 200, 30);
            
            JCheckBox keepLoggedInCheckBox = new JCheckBox("  Keep me logged in");
            keepLoggedInCheckBox.setBounds(100, 170, 200, 30);
            keepLoggedInCheckBox.setOpaque(false);
            keepLoggedInCheckBox.setForeground(common.getTextColor());
    
            ImageIcon toggleColorIcon = common.getModeIcon();
            JButton toggleColorButton = new JButton(toggleColorIcon);
            toggleColorButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            toggleColorButton.setBounds(340, 30, 30, 30);
            toggleColorButton.setToolTipText("Toggle color mode");
    
            JButton loginButton = new JButton("Login");
            loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            loginButton.setBounds(150, 210, 100, 30);
    
            JLabel registerLabel = new JLabel("Don't have an account? Register here");
            registerLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            registerLabel.setBounds(100, 250, 250, 30);
    
            addFocusListeners(usernameField, "Username");
            addFocusListeners(passwordField, "Password");
            addLoginActionListeners(usernameField, passwordField, toggleColorButton, loginButton, registerLabel, keepLoggedInCheckBox);
            
            add(titleLabel);
            add(usernameField);
            add(passwordField);
            add(keepLoggedInCheckBox);
            add(toggleColorButton);
            add(loginButton);
            add(registerLabel);
     
            SwingUtilities.invokeLater(() -> {
                titleLabel.requestFocusInWindow();
                System.out.println(username);
                if(username != "" && password != "" && username != null){
                    usernameField.setText(username);
                    passwordField.setText(password);
                    doLogin(usernameField, passwordField);
                }
            });            
        }
    
        private void addLoginActionListeners(JTextField usernameField, JPasswordField passwordField, JButton toggleColorButton, JButton loginButton, JLabel registerLabel, JCheckBox keepLoggedInCheckBox){
            
            loginButton.addActionListener(e -> {
                if (!usernameField.getText().equals("Username")){
                    doLogin(usernameField, passwordField);
                }
            });
    
            usernameField.addActionListener(e -> {
                if (!usernameField.getText().equals("")) {
                    doLogin(usernameField, passwordField);
                }
            });
    
            passwordField.addActionListener(e -> {
                if (!new String(passwordField.getPassword()).equals("")){
                    doLogin(usernameField, passwordField);
                }
            });
            
            toggleColorButton.addActionListener(e -> {
                common.toggleColorMode();
                dispose();
                SwingUtilities.invokeLater(() -> {
                    new LoginFrame("Login").setVisible(true);
                });
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
            
            keepLoggedInCheckBox.addActionListener(e -> {
                keepLoggedIn = !keepLoggedIn;
            });
    }

    public void doLogin(JTextField usernameField, JPasswordField passwordField){
        if(TaskDAO.validateUserFromDatabase(usernameField.getText(), new String(passwordField.getPassword()))){
            dispose();
            String usernameLogged = usernameField.getText();
            SwingUtilities.invokeLater(() -> {
                new TasksFrame("ToDoList", TaskDAO.getUserId(usernameLogged)).setVisible(true);
            });
        }
        else{
            JOptionPane.showMessageDialog(LoginFrame.this, "Invalid username or password", "Error", JOptionPane.ERROR_MESSAGE);
        }
        if(keepLoggedIn){
            //System.out.println("Keep me logged in");
            UserProperties.setProperty("username", usernameField.getText());
            UserProperties.setProperty("password", new String(passwordField.getPassword()));
            UserProperties.setProperty("lastSession", new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
            UserProperties.setProperty("rememberMe", "true");
        }
        else{
            //System.out.println("Don't keep me logged in");
            UserProperties.logOut();
        }
        usernameField.setText("Username");
        passwordField.setText("Password");
    }

}