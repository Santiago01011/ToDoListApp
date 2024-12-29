package main.java.UI;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTextField;

import main.java.COMMON.common;

public class Frame extends JFrame{
    //constructor
    public Frame(String title){
        // Get the screen size
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        // Set the size relative to the screen size
        setSize((int)(screenSize.width * 0.3), (int)(screenSize.height));
        setLocationRelativeTo(null);
        //setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ImageIcon appIcon = new ImageIcon("src/main/assets/app_icon.png");
        setIconImage(appIcon.getImage());
        getContentPane().setBackground(common.getPrimaryColor());
    }


        public void addFocusListeners(JTextField textField, String defaultText){
        textField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (textField.getText().equals(defaultText)) {
                    textField.setText("");
                }
            }
        });
        textField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setText(defaultText);
                }
            }
        });
    }
}