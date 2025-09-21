package UI;

import java.awt.Font;

/**
 * Centralized UI constants for consistent styling across the application.
 * This class contains all font definitions, layout constants, and other UI-related constants.
 */
public class UIConstants {

    // Font constants
    public static final Font TITLE_FONT_LARGE = new Font("Dialog", Font.BOLD, 30);
    public static final Font TITLE_FONT_MEDIUM = new Font("Dialog", Font.BOLD, 25);
    public static final Font TITLE_FONT_SMALL = new Font("Dialog", Font.BOLD, 21);
    public static final Font FIELD_FONT = new Font("Dialog", Font.PLAIN, 15);
    public static final Font BUTTON_FONT = new Font("Dialog", Font.BOLD, 12);

    // Layout constants
    public static final int STANDARD_BUTTON_HEIGHT = 30;
    public static final int STANDARD_GAP = 10;
    public static final int WINDOW_WIDTH_DEFAULT = 400;
    public static final int WINDOW_HEIGHT_DEFAULT = 350;

    // Task dashboard dimensions
    public static final int DASHBOARD_WIDTH = 700;
    public static final int DASHBOARD_HEIGHT = 700;
    public static final int DASHBOARD_MIN_WIDTH = 600;
    public static final int DASHBOARD_MIN_HEIGHT = 400;

    // Component sizing
    public static final int FIELD_HEIGHT = 30;
    public static final int ICON_SIZE = 20;

    private UIConstants() {
        // Utility class, no instantiation
    }
}