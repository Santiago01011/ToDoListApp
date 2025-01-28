package COMMON;

import javax.swing.UIDefaults;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarkLaf;

public class NightTheme extends FlatDarkLaf {
    @Override
    public String getName() {
        return "Night Theme";
    }

    public static boolean setup() {
        // Install the theme
        boolean success = FlatDarkLaf.setup(new NightTheme());

        // Apply custom UI defaults
        UIDefaults defaults = UIManager.getLookAndFeelDefaults();
        defaults.put("Component.background", common.getPrimaryColor());
        defaults.put("Component.foreground", common.getTextColor());
        defaults.put("Button.background", common.getSecondaryColor());
        defaults.put("Button.foreground", common.getTextColor());
        defaults.put("TextField.background", common.getTertiaryColor());
        defaults.put("TextField.foreground", common.getTextColor());
        defaults.put("TextField.caretForeground", common.getTextColor());
        defaults.put("Label.foreground", common.getTextColor());
        defaults.put("Separator.background", common.getTextColor());
        defaults.put("Panel.background", common.getPrimaryColor());
        defaults.put("Frame.background", common.getPrimaryColor());

        return success;
    }

    @Override
    public boolean isDark() {
        return true;
    }

    @Override
    public String getDescription() {
        return "Night theme for the application";
    }
    
}
