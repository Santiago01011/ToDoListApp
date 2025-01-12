package UI;


import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;

import COMMON.DayTheme;
import COMMON.NightTheme;
import COMMON.common;


import net.miginfocom.swing.MigLayout;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;



public class NtaskFrame extends JFrame {
    public NtaskFrame() {
        super();
        this.setTitle("Ntask");
        this.setIconImage(new ImageIcon("assets/app_icon.png").getImage());
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        this.setLayout(new BorderLayout());
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //DayTheme.setup();

        JPanel panel = new JPanel(new MigLayout("fillx, insets 30", "[right]rel[grow,fill]", "[center]"));
        panel.setOpaque(false);
        panel.add(new JLabel("Label 1:"), "align center");
        JTextField textField = new JTextField(15);
        textField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Enter text here");
        panel.add(textField, "align center, growx");
        panel.add(new JLabel("Label 2:"), "align center");
        panel.add(new JTextField(15), "growx");
        panel.add(new JButton("Submit"), "span, align center");

        panel.add(new JLabel("This is a theme switcher demo."), "span, align center");
        panel.add(new JButton("Sample Button"), "span, align center");
        JButton switchThemeButton = new JButton("Switch Theme");
        panel.add(switchThemeButton, "span, align center");

        this.add(panel, BorderLayout.CENTER);

         // Add action listener to switch themes
            switchThemeButton.addActionListener(e -> {
                try {
                    // Toggle the theme
                    common.toggleColorMode();

                    if(common.useNightMode){
                        NightTheme.setup();
                    } else{
                        DayTheme.setup();
                    }

                    // Update the UI
                    SwingUtilities.updateComponentTreeUI(this);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> {
            new NtaskFrame();
        });
    }
}
