package main.java.UI;

import main.java.COMMON.common;
import main.java.DBH.TaskDAO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;



public class LoginFrame extends Frame{


    public LoginFrame(String title){
        super(title);
        setTitle(title);
        setSize(400,350);
        ImageIcon appIcon = new ImageIcon("src/main/assets/login_icon.png");
        setIconImage(appIcon.getImage());
        setLayout(null);
        setResizable(false);
        addLoginUIComponents();

    }

    private void addLoginUIComponents(){
        JLabel titleLabel = new JLabel("Login");
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 25));
        titleLabel.setForeground(common.getTextColor());
        titleLabel.setBounds(160, 20, 400, 30);
        
        JTextField usernameField = new JTextField("Username");
        usernameField.setFont(new Font("Dialog", Font.PLAIN, 15));
        usernameField.setForeground(common.getTextColor());
        usernameField.setBounds(100, 100, 200, 30);
        usernameField.setBackground(common.getTertiaryColor());

        JPasswordField passwordField = new JPasswordField("Password");
        passwordField.setFont(new Font("Dialog", Font.PLAIN, 15));
        passwordField.setForeground(common.getTextColor());
        passwordField.setBounds(100, 150, 200, 30);
        passwordField.setBackground(common.getTertiaryColor());

        ImageIcon toggleColorIcon = common.getModeIcon();
        JButton toggleColorButton = new JButton(toggleColorIcon);
        toggleColorButton.setBackground(common.getSecondaryColor());
        toggleColorButton.setForeground(common.getTextColor());
        toggleColorButton.setFont(new Font("Dialog", Font.BOLD, 12));
        toggleColorButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleColorButton.setBounds(340, 20, 30, 30);
        toggleColorButton.setToolTipText("Toggle color mode");

        JButton loginButton = new JButton("Login");
        loginButton.setBackground(common.getSecondaryColor());
        loginButton.setForeground(common.getTextColor());
        loginButton.setFont(new Font("Dialog", Font.BOLD, 12));
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setBounds(150, 210, 100, 30);

        JLabel registerLabel = new JLabel("Don't have an account? Register here");
        registerLabel.setFont(new Font("Dialog", Font.PLAIN, 12));
        registerLabel.setForeground(common.getTextColor());
        registerLabel.setBounds(100, 250, 250, 30);
        registerLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

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