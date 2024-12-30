package UI;

import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.UIManager;

import COMMON.common;

public class Frame extends JFrame{
    //constructor
    public Frame(String title){
        // Get the screen size
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        // Set the size relative to the screen size
        setSize((int)(screenSize.width * 0.3), (int)(screenSize.height));
        setLocationRelativeTo(null);

        //set default values for frame components
        UIManager.put("Button.font", new Font("Dialog", Font.BOLD, 12));
        UIManager.put("Button.background", common.getSecondaryColor());
        UIManager.put("Button.foreground", common.getTextColor());

        UIManager.put("Label.font", new Font("Dialog", Font.PLAIN, 12));
        UIManager.put("Label.foreground", common.getTextColor());

        UIManager.put("TextField.font", new Font("Dialog", Font.PLAIN, 12));
        UIManager.put("TextField.background", common.getTertiaryColor());
        UIManager.put("TextField.foreground", common.getTextColor());
        UIManager.put("TextField.caretForeground", common.getTextColor());

        UIManager.put("PasswordField.font", new Font("Dialog", Font.PLAIN, 12));
        UIManager.put("PasswordField.background", common.getTertiaryColor());
        UIManager.put("PasswordField.foreground", common.getTextColor());
        UIManager.put("PasswordField.caretForeground", common.getTextColor());

        UIManager.put("TextArea.font", new Font("Dialog", Font.PLAIN, 14));
        UIManager.put("TextArea.background", common.getTertiaryColor());
        UIManager.put("TextArea.foreground", common.getTextColor());



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