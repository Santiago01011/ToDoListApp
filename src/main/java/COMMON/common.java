package COMMON;

import java.io.InputStream;
import java.util.Map;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Image;
import javax.swing.UIManager;
import java.awt.Color;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import model.TaskStatus;


public class common {

    private static final Map<String, ImageIcon> iconCache = new HashMap<>();

    public static boolean useNightMode = Boolean.valueOf((String) UserProperties.getProperty("darkTheme"));

    /*
     * Methods to get SVG icons from the assets folder
     * Icons are automatically colored to match the current theme's text color
     * The icons are stored in the assets folder in the resources directory
     * This should work in both development and production environments
     */
    public static ImageIcon getModeIcon(){
        String path = useNightMode ? "icons/day_mode.svg" : "icons/night_mode.svg";
        return loadIcon(path);
    }

    public static ImageIcon getAppIcon(){
        return loadIcon("icons/app_icon.svg");
    }

    public static ImageIcon getAddIcon(){
        return loadIcon("icons/add.svg");
    }

    public static ImageIcon getViewIcon(){
        return loadIcon("icons/view.svg");
    }

    public static ImageIcon getDeleteIcon(){
        return loadIcon("icons/delete.svg");
    }

    public static ImageIcon getBackIcon(){
        return loadIcon("icons/back.svg");
    }

    public static ImageIcon getEditIcon(){
        return loadIcon("icons/edit.svg");
    }

    public static ImageIcon getLogOutIcon(){
        return loadIcon("icons/logout.svg");
    }

    public static ImageIcon getUserConfigIcon(){
        return loadIcon("icons/userConfig.svg");
    }

    public static ImageIcon getSaveIcon(){
        return loadIcon("icons/save.svg");
    }

    public static ImageIcon getSyncIcon(){
        return loadIcon("icons/sync.svg");
    }

    public static ImageIcon getSettingsIcon() {
        return loadIcon("icons/settings.svg");
    }

    public static ImageIcon getFilterIcon() {
        return loadIcon("icons/filter.svg");
    }

    public static ImageIcon getRestoreIcon(){
        return loadIcon("icons/restore.svg");
    }

    // Added methods for user action icons
    public static ImageIcon getLogoutIcon() {
        return loadIcon("icons/logout.svg");
    }

    public static ImageIcon getEditUserIcon() {
        return loadIcon("icons/editUser.svg");
    }

    public static ImageIcon getDeleteUserIcon() {
        return loadIcon("icons/deleteUser.svg");
    }


    public static void toggleColorMode(){
        useNightMode = !useNightMode;
        // Clear icon cache so icons get reloaded with new theme colors
        iconCache.clear();
        // Persist the theme preference immediately
        UserProperties.setProperty("darkTheme", String.valueOf(useNightMode));
    }

    // Status color methods for task status badges
    public static String getStatusBackgroundColor(TaskStatus status) {
        switch (status) {
            case completed:
                return "@StatusCompleted";
            case in_progress:
                return "@StatusInProgress";
            case pending:
                return "@StatusPending";
            case incoming_due:
                return "@StatusIncomingDue";
            case overdue:
                return "@StatusOverdue";
            case newest:
                return "@StatusNewest";
            default:
                return "$Component.borderColor";
        }
    }

    private static ImageIcon loadIcon(String resourcePath){
        if (iconCache.containsKey(resourcePath)) {
            return iconCache.get(resourcePath);
        }

        try {
            ImageIcon icon;
            if (resourcePath.endsWith(".svg")) {
                // Handle SVG files using FlatLaf's built-in SVG support
                FlatSVGIcon svgIcon = new FlatSVGIcon(resourcePath, 20, 20);
                // Apply the current theme's label foreground color to the SVG icon
                Color labelColor = UIManager.getColor("Label.foreground");
                if (labelColor != null) {
                    svgIcon.setColorFilter(new FlatSVGIcon.ColorFilter() {
                        @Override
                        public Color filter(Color color) {
                            return labelColor;
                        }
                    });
                }
                icon = new ImageIcon(svgIcon.getImage());
            } else {
                // Handle PNG files as before
                try (InputStream is = common.class.getClassLoader().getResourceAsStream(resourcePath)) {
                    if (is == null) {
                        System.err.println("Resource not found on classpath: " + resourcePath);
                        return null;
                    }
                    Image image = ImageIO.read(is);
                    image = image.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                    icon = new ImageIcon(image);
                }
            }

            iconCache.put(resourcePath, icon);
            return icon;
        } catch (Exception e) {
            System.err.println("Error loading icon: " + resourcePath + " - " + e.getMessage());
            return null;
        }
    }

}