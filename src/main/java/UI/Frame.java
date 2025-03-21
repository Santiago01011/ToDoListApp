package UI;

import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarkLaf;

import COMMON.common;

public class Frame extends JFrame{
    
    public Frame(String title){
        super(title);
        FlatDarkLaf.setup();
        //common.toggleColorMode(); //for testing purposes
        // // Get the screen size
        // java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        // // Set the size relative to the screen size
        // setSize((int)(screenSize.width * 0.3), (int)(screenSize.height));
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

        UIManager.put("ComboBox.font", new Font("Consolas", Font.PLAIN, 14));
        UIManager.put("ComboBox.background", common.getTertiaryColor());
        UIManager.put("ComboBox.foreground", common.getTextColor());
        
        UIManager.put("TextArea.font", new Font("Consolas", Font.PLAIN, 16));
        UIManager.put("TextArea.background", common.getTertiaryColor());
        UIManager.put("TextArea.foreground", common.getTextColor());

        //add the default balues to BorderFactory.createTitledBorder("Add New Task", TitledBorder.CENTER, TitledBorder.TOP);
        UIManager.put("TitledBorder.font", new Font("Consolas", Font.BOLD, 14));
        UIManager.put("TitledBorder.titleColor", common.getTextColor());
        UIManager.put("TitledBorder.border", javax.swing.BorderFactory.createLineBorder(common.getTextColor()));

        UIManager.put("Panel.background", common.getPanelColor());

        UIManager.put("OptionPane.messageForeground", common.getTextColor());
        UIManager.put("OptionPane.background", common.getPrimaryColor());
        UIManager.put("Panel.background", common.getPrimaryColor());
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