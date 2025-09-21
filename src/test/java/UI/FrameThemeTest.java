package UI;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import javax.swing.UIManager;
import javax.swing.SwingUtilities;
import COMMON.common;

/**
 * Tests for Frame theme application functionality
 */
public class FrameThemeTest {

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
    public void testFrameCreationWithTheme() {
        assertDoesNotThrow(() -> {
            SwingUtilities.invokeAndWait(() -> {
                Frame testFrame = new Frame("Test Frame");
                assertNotNull(testFrame);
                testFrame.dispose();
            });
        });
    }

    @Test
    public void testFrameThemeRefresh() {
        assertDoesNotThrow(() -> {
            SwingUtilities.invokeAndWait(() -> {
                Frame testFrame = new Frame("Test Frame");
                
                // Test refresh with light theme
                common.useNightMode = false;
                testFrame.refreshTheme();
                assertEquals("CoffeYellow", UIManager.getLookAndFeel().getName());
                
                // Test refresh with dark theme
                common.useNightMode = true;
                testFrame.refreshTheme();
                assertEquals("NigthBlue", UIManager.getLookAndFeel().getName());
                
                testFrame.dispose();
            });
        });
    }

    @Test
    public void testFrameThemeInitialization() {
        assertDoesNotThrow(() -> {
            SwingUtilities.invokeAndWait(() -> {
                // Test light theme initialization
                common.useNightMode = false;
                Frame lightFrame = new Frame("Light Frame");
                assertEquals("CoffeYellow", UIManager.getLookAndFeel().getName());
                lightFrame.dispose();
                
                // Test dark theme initialization
                common.useNightMode = true;
                Frame darkFrame = new Frame("Dark Frame");
                assertEquals("NigthBlue", UIManager.getLookAndFeel().getName());
                darkFrame.dispose();
            });
        });
    }

    @Test
    public void testMultipleFrameThemeConsistency() {
        assertDoesNotThrow(() -> {
            SwingUtilities.invokeAndWait(() -> {
                common.useNightMode = false;
                
                Frame frame1 = new Frame("Frame 1");
                Frame frame2 = new Frame("Frame 2");
                
                // Both frames should have the same theme
                assertEquals(UIManager.getLookAndFeel().getName(), "CoffeYellow");
                
                // Change theme
                common.useNightMode = true;
                frame1.refreshTheme();
                frame2.refreshTheme();
                
                // Both frames should now have the dark theme
                assertEquals(UIManager.getLookAndFeel().getName(), "NigthBlue");
                
                frame1.dispose();
                frame2.dispose();
            });
        });
    }
}
