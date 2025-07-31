package themes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.io.InputStream;

/**
 * Tests for CoffeYellow custom theme
 */
public class CoffeYellowTest {

    @BeforeEach
    public void setUp() {
        // Reset UIManager before each test
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Ignore setup failures
        }
    }

    @AfterEach
    public void tearDown() {
        // Reset to system look and feel after each test
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Ignore cleanup failures
        }
    }

    @Test
    public void testThemeCreation() {
        assertDoesNotThrow(() -> {
            CoffeYellow theme = new CoffeYellow();
            assertNotNull(theme);
        });
    }

    @Test
    public void testThemeName() {
        CoffeYellow theme = new CoffeYellow();
        assertEquals("CoffeYellow", theme.getName());
        assertEquals("CoffeYellow", CoffeYellow.NAME);
    }

    @Test
    public void testThemeDescription() {
        CoffeYellow theme = new CoffeYellow();
        assertEquals("CoffeYellow Theme", theme.getDescription());
    }

    @Test
    public void testThemeSetup() {
        boolean setupResult = CoffeYellow.setup();
        assertTrue(setupResult, "Theme setup should succeed");
        
        // Verify the theme is actually set
        assertEquals("CoffeYellow", UIManager.getLookAndFeel().getName());
    }

    @Test
    public void testThemeInstallLafInfo() {
        assertDoesNotThrow(() -> {
            CoffeYellow.installLafInfo();
        });
    }

    @Test
    public void testThemeCanBeSetAsLookAndFeel() {
        assertDoesNotThrow(() -> {
            UIManager.setLookAndFeel(new CoffeYellow());
        });
        
        assertEquals("CoffeYellow", UIManager.getLookAndFeel().getName());
    }

    @Test
    public void testThemePropertiesFileExists() {
        // Test that the properties file can be loaded
        InputStream propertiesStream = CoffeYellow.class.getResourceAsStream("/themes/CoffeYellow.properties");
        assertNotNull(propertiesStream, "CoffeYellow.properties file should exist in resources/themes/");
        
        assertDoesNotThrow(() -> {
            if (propertiesStream != null) {
                propertiesStream.close();
            }
        });
    }

    @Test
    public void testThemeUIProperties() {
        // Set up the theme
        CoffeYellow.setup();
        
        // Test some key UI properties are set (these should come from the properties file)
        assertNotNull(UIManager.get("Button.font"));
        assertNotNull(UIManager.get("Label.font"));
        assertNotNull(UIManager.get("TextField.font"));
        
        // Test that custom colors are applied
        assertNotNull(UIManager.get("Panel.background"));
        assertNotNull(UIManager.get("Button.background"));
    }

    @Test
    public void testThemeExtendsCorrectBaseClass() {
        CoffeYellow theme = new CoffeYellow();
        assertTrue(theme instanceof com.formdev.flatlaf.FlatLightLaf, 
                  "CoffeYellow should extend FlatLightLaf");
    }
}
