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
        setSize(400,350);
        ImageIcon appIcon = new ImageIcon("src/main/assets/login_icon.png");
        setIconImage(appIcon.getImage());
        setLayout(null);
        setResizable(false);
        addRegisterUIComponents();

    }

    private void addRegisterUIComponents(){
        
        JLabel titleLabel = new JLabel("Register", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 25));
        titleLabel.setForeground(common.getTextColor());
        titleLabel.setBounds(0, 20, 400, 30);
        
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

        JPasswordField rePasswordField = new JPasswordField("Password");
        rePasswordField.setFont(new Font("Dialog", Font.PLAIN, 15));
        rePasswordField.setForeground(common.getTextColor());
        rePasswordField.setBounds(100, 200, 200, 30);
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
        registerButton.setBounds(150, 250, 100, 30);

        addFocusListeners(usernameField, "Username");
        addFocusListeners(passwordField, "Password");
        addFocusListeners(rePasswordField, "Password");
        addRegisterActionListeners(usernameField, passwordField, rePasswordField, toggleColorButton, backButton, registerButton);


        add(titleLabel);
        add(usernameField);
        add(passwordField);
        add(rePasswordField);
        add(toggleColorButton);
        add(backButton);
        add(registerButton);
        
    }

    private void addRegisterActionListeners(JTextField usernameField, JPasswordField passwordField, JPasswordField rePasswordField, JButton toggleColorButton, JButton backButton, JButton registerButton){
        

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
                String password = new String(passwordField.getPassword());
                String rePassword = new String(rePasswordField.getPassword());
                if(username.equals("Username") || password.equals("Password") || rePassword.equals("Password")){
                    JOptionPane.showMessageDialog(null, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
                }else if(!password.equals(rePassword)){
                    JOptionPane.showMessageDialog(null, "Passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
                }else{
                    doRegister(username, password);
                }
            }
        });

        usernameField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                String rePassword = new String(rePasswordField.getPassword());
                if(username.equals("Username") || password.equals("Password") || rePassword.equals("Password")){
                    JOptionPane.showMessageDialog(null, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
                }else if(!password.equals(rePassword)){
                    JOptionPane.showMessageDialog(null, "Passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
                }else{
                    doRegister(username, password);
                }
            }
        });

        rePasswordField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                String rePassword = new String(rePasswordField.getPassword());
                if(username.equals("Username") || password.equals("Password") || rePassword.equals("Password")){
                    JOptionPane.showMessageDialog(null, "Please fill in all fields", "Error", JOptionPane.ERROR_MESSAGE);
                }else if(!password.equals(rePassword)){
                    JOptionPane.showMessageDialog(null, "Passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
                }else{
                    doRegister(username, password);
                }
            }
        });

    }

    private void doRegister(String username, String password){
        if(TaskDAO.getUsername(username)){
            JOptionPane.showMessageDialog(null, "Username already exists", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        TaskDAO.registerUser(username, password);
        JOptionPane.showMessageDialog(null, "User registered successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
        dispose();
        new LoginFrame("Login").setVisible(true);
    }


}