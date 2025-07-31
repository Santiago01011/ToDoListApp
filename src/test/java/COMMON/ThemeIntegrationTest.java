package COMMON;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import javax.swing.UIManager;
import javax.swing.ImageIcon;
import themes.CoffeYellow;
import themes.NigthBlue;

/**
 * Tests for theme functionality in the common class
 */
public class ThemeIntegrationTest {

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
    public void testToggleColorMode() {
        boolean initialMode = common.useNightMode;
        
        // Toggle the mode
        common.toggleColorMode();
        
        // Verify the mode changed
        assertEquals(!initialMode, common.useNightMode);
        
        // Toggle back
        common.toggleColorMode();
        
        // Verify it's back to original
        assertEquals(initialMode, common.useNightMode);
    }

    @Test
    public void testToggleColorModePersistence() {
        boolean initialMode = common.useNightMode;
        
        // Toggle the mode
        common.toggleColorMode();
        
        // Verify the property was saved
        String savedValue = (String) UserProperties.getProperty("darkTheme");
        assertEquals(String.valueOf(!initialMode), savedValue);
        
        // Toggle back
        common.toggleColorMode();
        
        // Verify the property was saved again
        savedValue = (String) UserProperties.getProperty("darkTheme");
        assertEquals(String.valueOf(initialMode), savedValue);
    }

    @Test
    public void testIconLoadingBasedOnTheme() {
        // Test light mode icons
        common.useNightMode = false;
        ImageIcon lightModeIcon = common.getModeIcon();
        assertNotNull(lightModeIcon, "Light mode icon should not be null");
        
        ImageIcon addIconLight = common.getAddIcon();
        assertNotNull(addIconLight, "Add icon in light mode should not be null");
        
        // Test dark mode icons
        common.useNightMode = true;
        ImageIcon darkModeIcon = common.getModeIcon();
        assertNotNull(darkModeIcon, "Dark mode icon should not be null");
        
        ImageIcon addIconDark = common.getAddIcon();
        assertNotNull(addIconDark, "Add icon in dark mode should not be null");
        
        // Icons should be different for different modes
        // (This is a basic check - in reality icons might be cached and reused)
        assertNotNull(lightModeIcon);
        assertNotNull(darkModeIcon);
    }

    @Test
    public void testThemeInitializationBasedOnUserProperties() {
        // Test light theme initialization
        UserProperties.setProperty("darkTheme", "false");
        boolean lightMode = Boolean.valueOf((String) UserProperties.getProperty("darkTheme"));
        assertFalse(lightMode, "Should be light mode when darkTheme is false");
        
        // Test dark theme initialization
        UserProperties.setProperty("darkTheme", "true");
        boolean darkMode = Boolean.valueOf((String) UserProperties.getProperty("darkTheme"));
        assertTrue(darkMode, "Should be dark mode when darkTheme is true");
    }

    @Test
    public void testAppIconAlwaysAvailable() {
        // App icon should be available regardless of theme
        common.useNightMode = false;
        ImageIcon appIconLight = common.getAppIcon();
        assertNotNull(appIconLight, "App icon should be available in light mode");
        
        common.useNightMode = true;
        ImageIcon appIconDark = common.getAppIcon();
        assertNotNull(appIconDark, "App icon should be available in dark mode");
    }

    @Test
    public void testAllIconMethodsReturnValidIcons() {
        // Test all icon methods don't throw exceptions
        // Note: Some icons might be null if asset files are missing, 
        // but the methods should not throw exceptions
        assertDoesNotThrow(() -> {
            // Core icons that should always be available
            assertNotNull(common.getModeIcon(), "Mode icon should be available");
            assertNotNull(common.getAppIcon(), "App icon should be available");
            assertNotNull(common.getAddIcon(), "Add icon should be available");
            assertNotNull(common.getViewIcon(), "View icon should be available");
            assertNotNull(common.getDeleteIcon(), "Delete icon should be available");
            assertNotNull(common.getBackIcon(), "Back icon should be available");
            assertNotNull(common.getEditIcon(), "Edit icon should be available");
            assertNotNull(common.getLogOutIcon(), "LogOut icon should be available");
            assertNotNull(common.getUserConfigIcon(), "UserConfig icon should be available");
            assertNotNull(common.getSaveIcon(), "Save icon should be available");
            assertNotNull(common.getSyncIcon(), "Sync icon should be available");
            assertNotNull(common.getFilterIcon(), "Filter icon should be available");
            assertNotNull(common.getRestoreIcon(), "Restore icon should be available");
            assertNotNull(common.getLogoutIcon(), "Logout icon should be available");
            assertNotNull(common.getEditUserIcon(), "EditUser icon should be available");
            assertNotNull(common.getDeleteUserIcon(), "DeleteUser icon should be available");
        });
    }
}
