package UI;

import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.UIManager;

import COMMON.common;

public class Frame extends JFrame{
    static {
        if ("true".equals(System.getProperty("netbeans.designTime"))) {
            // Set the default values for the frame components
            UIManager.put("Button.font", new Font("Consolas", Font.BOLD, 14));
            UIManager.put("Button.background", common.getSecondaryColor());
            UIManager.put("Button.foreground", common.getTextColor());

            UIManager.put("Label.font", new Font("Consolas", Font.PLAIN, 14));
            UIManager.put("Label.foreground", common.getTextColor());

            UIManager.put("TextField.font", new Font("Consolas", Font.PLAIN, 14));
            UIManager.put("TextField.background", common.getTertiaryColor());
            UIManager.put("TextField.foreground", common.getTextColor());
            UIManager.put("TextField.caretForeground", common.getTextColor());

            UIManager.put("PasswordField.font", new Font("Consolas", Font.PLAIN, 14));
            UIManager.put("PasswordField.background", common.getTertiaryColor());
            UIManager.put("PasswordField.foreground", common.getTextColor());
            UIManager.put("PasswordField.caretForeground", common.getTextColor());

            UIManager.put("TextArea.font", new Font("Consolas", Font.PLAIN, 16));
            UIManager.put("TextArea.background", common.getTertiaryColor());
            UIManager.put("TextArea.foreground", common.getTextColor());

            UIManager.put("Separator.background", common.getTextColor());
        }
    }
    
    // No-argument constructor for GUI Builder
    public Frame() {
        this("Default Title");
    }
    //constructor
    public Frame(String title){
        super(title);
        // Get the screen size
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        // Set the size relative to the screen size
        setSize((int)(screenSize.width * 0.3), (int)(screenSize.height));
        setLocationRelativeTo(null);

        //set default values for frame components
        UIManager.put("Button.font", new Font("Consolas", Font.BOLD, 14));
        UIManager.put("Button.background", common.getSecondaryColor());
        UIManager.put("Button.foreground", common.getTextColor());

        UIManager.put("Label.font", new Font("Consolas", Font.PLAIN, 14));
        UIManager.put("Label.foreground", common.getTextColor());

        UIManager.put("TextField.font", new Font("Consolas", Font.PLAIN, 14));
        UIManager.put("TextField.background", common.getTertiaryColor());
        UIManager.put("TextField.foreground", common.getTextColor());
        UIManager.put("TextField.caretForeground", common.getTextColor());

        UIManager.put("PasswordField.font", new Font("Consolas", Font.PLAIN, 14));
        UIManager.put("PasswordField.background", common.getTertiaryColor());
        UIManager.put("PasswordField.foreground", common.getTextColor());
        UIManager.put("PasswordField.caretForeground", common.getTextColor());

        UIManager.put("TextArea.font", new Font("Consolas", Font.PLAIN, 16));
        UIManager.put("TextArea.background", common.getTertiaryColor());
        UIManager.put("TextArea.foreground", common.getTextColor());



        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        ImageIcon appIcon = common.getAppIcon();
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