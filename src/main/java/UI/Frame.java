package UI;

import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;

import COMMON.common;
import themes.CoffeYellow;
import themes.NigthBlue;

public class Frame extends JFrame{
    
    public Frame(String title) {
        super(title);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        refreshTheme();
        ImageIcon appIcon = common.getAppIcon();
        setIconImage(appIcon.getImage());
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

    private void applyThemeDefaults() {
        try {
            UIManager.setLookAndFeel(common.useNightMode ? new NigthBlue() : new CoffeYellow());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }
    }

    public void refreshTheme() {
        applyThemeDefaults();
        SwingUtilities.updateComponentTreeUI(this);
    }
}