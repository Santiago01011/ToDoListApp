package UI;

import COMMON.common;
import DBH.TaskDAO;
import io.github.cdimascio.dotenv.Dotenv;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class LoginFrame extends Frame{
    
    private static final Dotenv dotenv = Dotenv.load();
    public static final String appUsername = dotenv.get("APP_USERNAME");
    private static final String password = dotenv.get("PASSWORD");

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
        usernameField.setBounds(100, 100, 200, 30);

        JPasswordField passwordField = new JPasswordField("Password");
        passwordField.setBounds(100, 150, 200, 30);

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
        addLoginActionListeners(usernameField, passwordField, toggleColorButton, loginButton, registerLabel);
        
        add(titleLabel);
        add(usernameField);
        add(passwordField);
        add(toggleColorButton);
        add(loginButton);
        add(registerLabel);
 
        SwingUtilities.invokeLater(() -> {
            titleLabel.requestFocusInWindow();
            if(appUsername != null && password != null){
                usernameField.setText(appUsername);
                passwordField.setText(password);
                doLogin(usernameField, passwordField);
            }
        });

        
    }

    private void addLoginActionListeners(JTextField usernameField, JPasswordField passwordField, JButton toggleColorButton, JButton loginButton, JLabel registerLabel){
        
        loginButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                if (!usernameField.getText().equals("Username")){
                    doLogin(usernameField, passwordField);
                }
            }
        });

        usernameField.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                if (!usernameField.getText().equals("")){
                    doLogin(usernameField, passwordField);
                }
            }
        });

        passwordField.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                if (!new String(passwordField.getPassword()).equals("")){
                    doLogin(usernameField, passwordField);
                }
            }
        });
        
        toggleColorButton.addActionListener(e -> {
            common.toggleColorMode();
            dispose();
            new LoginFrame("Login").setVisible(true);
        });

        registerLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                dispose();
                new RegisterFrame("Register").setVisible(true);
            }
        });    
    }

    public void doLogin(JTextField usernameField, JPasswordField passwordField){
        if(TaskDAO.validateUserFromDatabase(usernameField.getText(), new String(passwordField.getPassword()))){
            dispose();
            new TasksFrame("ToDoList", TaskDAO.getUserId(usernameField.getText())).setVisible(true);
        }
        else{
            JOptionPane.showMessageDialog(LoginFrame.this, "Invalid username or password", "Error", JOptionPane.ERROR_MESSAGE);
        }
        usernameField.setText("Username");
        passwordField.setText("Password");
    }

}