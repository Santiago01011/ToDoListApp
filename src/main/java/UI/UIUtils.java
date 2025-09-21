package UI;

import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.Cursor;
import java.awt.Font;

/**
 * Utility class for common UI operations and patterns.
 */
public class UIUtils {

    // Layout constants
    public static final int STANDARD_BUTTON_HEIGHT = 30;
    public static final int STANDARD_GAP = 10;

    // Font constants
    public static final Font TITLE_FONT_LARGE = new Font("Dialog", Font.BOLD, 30);
    public static final Font TITLE_FONT_MEDIUM = new Font("Dialog", Font.BOLD, 25);
    public static final Font TITLE_FONT_SMALL = new Font("Dialog", Font.BOLD, 21);
    public static final Font FIELD_FONT = new Font("Dialog", Font.PLAIN, 15);
    public static final Font BUTTON_FONT = new Font("Dialog", Font.BOLD, 12);

    /**
     * Adds focus listeners to a text field to implement placeholder text behavior.
     * When the field gains focus, if it contains the default text, it clears.
     * When the field loses focus, if it's empty, it restores the default text.
     *
     * @param textField The text field to add listeners to
     * @param defaultText The placeholder text to show when field is empty
     */
    public static void addPlaceholderText(JTextField textField, String defaultText) {
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

    /**
     * Styles an icon button with common properties: hand cursor, tooltip, and border removal.
     *
     * @param button The button to style
     * @param tooltip The tooltip text to display
     */
    public static void styleIconButton(JButton button, String tooltip) {
        button.setToolTipText(tooltip);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /**
     * Styles a regular button with common properties: hand cursor and tooltip.
     *
     * @param button The button to style
     * @param tooltip The tooltip text to display
     */
    public static void styleButton(JButton button, String tooltip) {
        button.setToolTipText(tooltip);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /**
     * Creates a standard font with the specified style and size.
     *
     * @param style The font style (Font.PLAIN, Font.BOLD, etc.)
     * @param size The font size
     * @return A new Font instance
     */
    public static Font getStandardFont(int style, float size) {
        return new Font("Dialog", style, (int) size);
    }

    /**
     * Creates a title font with bold style and the specified size.
     *
     * @param size The font size
     * @return A new bold Font instance
     */
    public static Font getTitleFont(float size) {
        return new Font("Dialog", Font.BOLD, (int) size);
    }

    private UIUtils() {
        // Utility class, no instantiation
    }
}