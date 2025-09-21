package UI;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.SwingUtilities;

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


    private void applyThemeDefaults() {
        try {
            FlatAnimatedLafChange.showSnapshot();
            UIManager.setLookAndFeel(common.useNightMode ? new NigthBlue() : new CoffeYellow());
            FlatAnimatedLafChange.hideSnapshotWithAnimation();
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void refreshTheme() {
        applyThemeDefaults();
        SwingUtilities.updateComponentTreeUI(this);
    }
}