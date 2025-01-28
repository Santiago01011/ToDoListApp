package UI;

import COMMON.common;
import DBH.TaskDAO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class RegisterFrame extends Frame{

    public RegisterFrame(String title){
        super(title);
        setTitle(title);
        setSize(400,400);
        setLocationRelativeTo(null);
        setLayout(null);
        setResizable(false);
        addRegisterUIComponents();

    }

    private void addRegisterUIComponents(){
        
        JLabel titleLabel = new JLabel("Register", SwingConstants.CENTER);
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
        addRegisterActionListeners(usernameField,emailField ,passwordField, rePasswordField, toggleColorButton, backButton, registerButton);


        add(titleLabel);
        add(usernameField);
        add(emailField);
        add(passwordField);
        add(rePasswordField);
        add(toggleColorButton);
        add(backButton);
        add(registerButton);
        
    }

    private void addRegisterActionListeners(JTextField usernameField,JTextField emailField ,JPasswordField passwordField, JPasswordField rePasswordField, JButton toggleColorButton, JButton backButton, JButton registerButton){
        

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new LoginFrame("Login").setVisible(true);
            }
        });

        toggleColorButton.addActionListener(e -> {
            common.toggleColorMode();
            dispose();
            new RegisterFrame("Register").setVisible(true);
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String email = emailField.getText();
                String password = new String(passwordField.getPassword());
                String rePassword = new String(rePasswordField.getPassword());
                if(username.equals("Username") || email.equals("Email") || password.equals("Password") || rePassword.equals("Password")){
                    JOptionPane.showMessageDialog(null, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
                }else if(!password.equals(rePassword)){
                    JOptionPane.showMessageDialog(null, "Passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
                }else{
                    doRegister(username,email , password);
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
                if(username.equals("Username") || email.equals("Email") || password.equals("Password") || rePassword.equals("Password")){
                    JOptionPane.showMessageDialog(null, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
                }else if(!password.equals(rePassword)){
                    JOptionPane.showMessageDialog(null, "Passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
                }else{
                    doRegister(username,email , password);
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
                if(username.equals("Username") || email.equals("Email") || password.equals("Password") || rePassword.equals("Password")){
                    JOptionPane.showMessageDialog(null, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
                }else if(!password.equals(rePassword)){
                    JOptionPane.showMessageDialog(null, "Passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
                }else{
                    doRegister(username,email , password);
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
                if(username.equals("Username") || email.equals("Email") || password.equals("Password") || rePassword.equals("Password")){
                    JOptionPane.showMessageDialog(null, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
                }else if(!password.equals(rePassword)){
                    JOptionPane.showMessageDialog(null, "Passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
                }else{
                    doRegister(username,email , password);
                }
            }
        });

    }

    private void doRegister(String username, String email, String password){
        if(TaskDAO.getUsername(username)){
            JOptionPane.showMessageDialog(null, "Username already exists", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if(TaskDAO.getEmail(email)){
            JOptionPane.showMessageDialog(null, "Email already exists", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        TaskDAO.registerUser(username, email, password);
        JOptionPane.showMessageDialog(null, "User registered successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
        dispose();
        new LoginFrame("Login").setVisible(true);
    }


}