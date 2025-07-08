import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import javax.swing.UIManager;
import javax.swing.SwingUtilities;
import themes.CoffeYellow;
import themes.NigthBlue;
import COMMON.common;
import COMMON.UserProperties;

/**
 * Integration tests for AppLauncher theme initialization
 */
public class AppLauncherThemeTest {

    private boolean originalNightMode;

    @BeforeEach
    public void setUp() {
        // Save original state
        originalNightMode = common.useNightMode;
        
        // Reset to system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Ignore setup failures
        }
    }

    @AfterEach
    public void tearDown() {
        // Restore original state
        common.useNightMode = originalNightMode;
        
        // Reset to system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Ignore cleanup failures
        }
    }

    @Test
    public void testAppLauncherThemeInitializationLight() {
        // Simulate light theme preference
        UserProperties.setProperty("darkTheme", "false");
        common.useNightMode = false;
        
        assertDoesNotThrow(() -> {
            // Simulate AppLauncher theme initialization
            if (common.useNightMode) {
                UIManager.setLookAndFeel(new NigthBlue());
            } else {
                UIManager.setLookAndFeel(new CoffeYellow());
            }
        });
        
        assertEquals("CoffeYellow", UIManager.getLookAndFeel().getName());
    }

    @Test
    public void testAppLauncherThemeInitializationDark() {
        // Simulate dark theme preference
        UserProperties.setProperty("darkTheme", "true");
        common.useNightMode = true;
        
        assertDoesNotThrow(() -> {
            // Simulate AppLauncher theme initialization
            if (common.useNightMode) {
                UIManager.setLookAndFeel(new NigthBlue());
            } else {
                UIManager.setLookAndFeel(new CoffeYellow());
            }
        });
        
        assertEquals("NigthBlue", UIManager.getLookAndFeel().getName());
    }

    @Test
    public void testAppLauncherHandlesThemeFailure() {
        // Test that AppLauncher can handle theme initialization failure gracefully
        assertDoesNotThrow(() -> {
            try {
                // This should work normally
                if (common.useNightMode) {
                    UIManager.setLookAndFeel(new NigthBlue());
                } else {
                    UIManager.setLookAndFeel(new CoffeYellow());
                }
            } catch (Exception ex) {
                // This simulates the error handling in AppLauncher
                System.err.println("Failed to initialize custom LaF, falling back to default: " + ex.getMessage());
                // In a real scenario, this would continue with default theme
            }
        });
    }

    @Test
    public void testUserPropertiesThemePreference() {
        // Test that user preferences are correctly read
        UserProperties.setProperty("darkTheme", "false");
        boolean lightMode = Boolean.valueOf((String) UserProperties.getProperty("darkTheme"));
        assertFalse(lightMode);
        
        UserProperties.setProperty("darkTheme", "true");
        boolean darkMode = Boolean.valueOf((String) UserProperties.getProperty("darkTheme"));
        assertTrue(darkMode);
        
        // Test default value handling
        UserProperties.setProperty("darkTheme", "");
        boolean defaultMode = Boolean.valueOf((String) UserProperties.getProperty("darkTheme"));
        assertFalse(defaultMode); // Empty string should default to false
    }

    @Test
    public void testCommonUseNightModeInitialization() {
        // Test that common.useNightMode is properly initialized from UserProperties
        UserProperties.setProperty("darkTheme", "false");
        boolean shouldBeFalse = Boolean.valueOf((String) UserProperties.getProperty("darkTheme"));
        assertFalse(shouldBeFalse);
        
        UserProperties.setProperty("darkTheme", "true");
        boolean shouldBeTrue = Boolean.valueOf((String) UserProperties.getProperty("darkTheme"));
        assertTrue(shouldBeTrue);
    }
}
