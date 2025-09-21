package UI.components;

import java.awt.Color;
import java.awt.LayoutManager;
import javax.swing.JPanel;
import javax.swing.UIManager;
import net.miginfocom.swing.MigLayout;
/**
 * A reusable card component with rounded corners and customizable appearance.
 * This class serves as a base for all card-like panels in the application.
 * Uses FlatLaf UI colors by default to match the current application theme.
 */
public class CardPanel extends JPanel {
    private static final int DEFAULT_ARC = 10;
    private static final int DEFAULT_THICKNESS = 2;
    private static final String DEFAULT_LAYOUT = "fillx, insets 10 15 10 15";
    private static final String DEFAULT_COLUMNS = "[grow, fill]";
    private static final String DEFAULT_ROWS = "[]";
    
    private static Color getDefaultBackgroundColor() {
        Color bgColor = UIManager.getColor("Panel.background");
        return bgColor != null ? bgColor : new Color(245, 245, 245);
    }
    
    private static Color getDefaultBorderColor() {
        Color borderColor = UIManager.getColor("Component.borderColor");
        return borderColor != null ? borderColor : new Color(200, 200, 200);
    }
        
    /**
     * Creates a default card with default background color and rounded borders.
     */
    public CardPanel() {
        this(getDefaultBackgroundColor(), getDefaultBorderColor(), DEFAULT_ARC, DEFAULT_THICKNESS);
    }
    
    /**
     * Creates a card with custom background color and default rounded borders.
     * 
     * @param backgroundColor The background color for the card
     */
    public CardPanel(Color backgroundColor) {
        this(backgroundColor, getDefaultBorderColor(), DEFAULT_ARC, DEFAULT_THICKNESS);
    }
    
    /**
     * Creates a card with custom background color and custom corner radius.
     * 
     * @param backgroundColor The background color for the card
     * @param arc The corner radius for rounded corners
     */
    public CardPanel(Color backgroundColor, int arc) {
        this(backgroundColor, getDefaultBorderColor(), arc, DEFAULT_THICKNESS);
    }

    /**
     * Creates a card with custom background color and custom border color.
     * 
     * @param backgroundColor The background color for the card
     * @param outlineColor The color for the card's border
     */
    public CardPanel(Color backgroundColor, Color outlineColor) {
        this(backgroundColor, outlineColor, DEFAULT_ARC, DEFAULT_THICKNESS);
    }

    /**
     * Creates a card with custom background color, border color, and corner radius.
     * 
     * @param backgroundColor The background color for the card
     * @param outlineColor The color for the card's border
     * @param arc The corner radius for rounded corners
     */
    public CardPanel(Color backgroundColor, Color outlineColor, int arc) {
        this(backgroundColor, outlineColor, arc, DEFAULT_THICKNESS);
    }
    
    /**
     * Creates a fully customized card with specific background color, border color,
     * corner radius, and border thickness.
     * 
     * @param backgroundColor The background color for the card
     * @param outlineColor The color for the card's border
     * @param arc The corner radius for rounded corners
     * @param thickness The thickness of the border
     */
    public CardPanel(Color backgroundColor, Color outlineColor, int arc, int thickness) {
        setLayout(new MigLayout(DEFAULT_LAYOUT, DEFAULT_COLUMNS, DEFAULT_ROWS));
        setBackground(backgroundColor);
        putClientProperty("FlatLaf.style", "arc: " + arc);
        setBorder(new RoundedLineBorder(outlineColor, thickness, arc));
    }
    
    /**
     * Creates a card with a custom layout.
     * 
     * @param layoutConstraints The layout constraints for MigLayout
     * @param columnConstraints The column constraints for MigLayout
     * @param rowConstraints The row constraints for MigLayout
     */
    public CardPanel(String layoutConstraints, String columnConstraints, String rowConstraints) {
        this();
        setLayout(new MigLayout(layoutConstraints, columnConstraints, rowConstraints));
    }    
    /**
     * Creates a card with a custom layout manager.
     * 
     * @param layout The LayoutManager to use for this card
     */
    public CardPanel(LayoutManager layout) {
        super(layout);
        setBackground(getDefaultBackgroundColor());
        putClientProperty("FlatLaf.style", "arc: " + DEFAULT_ARC);
        setBorder(new RoundedLineBorder(getDefaultBorderColor(), DEFAULT_THICKNESS, DEFAULT_ARC));
    }    
    /**
     * A utility method for child classes to set theme-aware colors from an external source.
     * This allows themed cards without direct dependencies on theme classes.
     * 
     * @param backgroundColor The background color for the card
     * @param borderColor The border color for the card
     */
    public void setThemeColors(Color backgroundColor, Color borderColor) {
        setBackground(backgroundColor);
        setBorder(new RoundedLineBorder(borderColor, DEFAULT_THICKNESS, DEFAULT_ARC));
    }
    
    /**
     * A more comprehensive utility method for child classes to set theme-aware colors.
     * 
     * @param backgroundColor The background color for the card
     * @param borderColor The border color for the card
     * @param arc The corner radius for rounded corners
     * @param thickness The thickness of the border
     */
    public void setThemeColors(Color backgroundColor, Color borderColor, int arc, int thickness) {
        setBackground(backgroundColor);
        putClientProperty("FlatLaf.style", "arc: " + arc);
        setBorder(new RoundedLineBorder(borderColor, thickness, arc));
    }
}

