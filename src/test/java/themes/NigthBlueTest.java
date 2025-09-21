package themes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import javax.swing.UIManager;
import java.io.InputStream;

/**
 * Tests for NigthBlue custom theme
 */
public class NigthBlueTest {

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
            NigthBlue theme = new NigthBlue();
            assertNotNull(theme);
        });
    }

    @Test
    public void testThemeName() {
        NigthBlue theme = new NigthBlue();
        assertEquals("NigthBlue", theme.getName());
        assertEquals("NigthBlue", NigthBlue.NAME);
    }

    @Test
    public void testThemeDescription() {
        NigthBlue theme = new NigthBlue();
        assertEquals("NigthBlue Theme", theme.getDescription());
    }

    @Test
    public void testThemeSetup() {
        boolean setupResult = NigthBlue.setup();
        assertTrue(setupResult, "Theme setup should succeed");
        
        // Verify the theme is actually set
        assertEquals("NigthBlue", UIManager.getLookAndFeel().getName());
    }

    @Test
    public void testThemeInstallLafInfo() {
        assertDoesNotThrow(() -> {
            NigthBlue.installLafInfo();
        });
    }

    @Test
    public void testThemeCanBeSetAsLookAndFeel() {
        assertDoesNotThrow(() -> {
            UIManager.setLookAndFeel(new NigthBlue());
        });
        
        assertEquals("NigthBlue", UIManager.getLookAndFeel().getName());
    }

    @Test
    public void testThemePropertiesFileExists() {
        // Test that the properties file can be loaded
        InputStream propertiesStream = NigthBlue.class.getResourceAsStream("/themes/NigthBlue.properties");
        assertNotNull(propertiesStream, "NigthBlue.properties file should exist in resources/themes/");
        
        assertDoesNotThrow(() -> {
            if (propertiesStream != null) {
                propertiesStream.close();
            }
        });
    }

    @Test
    public void testThemeUIProperties() {
        // Set up the theme
        NigthBlue.setup();
        
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
        NigthBlue theme = new NigthBlue();
        assertTrue(theme instanceof com.formdev.flatlaf.FlatDarculaLaf, 
                  "NigthBlue should extend FlatDarculaLaf");
    }
}
