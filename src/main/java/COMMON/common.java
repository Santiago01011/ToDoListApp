package COMMON;

import java.awt.Color;
import java.io.InputStream;
import java.util.Map;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.Image;


public class common {

    private static final Map<String, ImageIcon> iconCache = new HashMap<>();

    public static boolean useNightMode = Boolean.valueOf((String) UserProperties.getProperty("darkTheme"));

    /*
     * Methods to get the path of the icons based on the mode
     * The icons are stored in the assets folder in the resources directory
     * This should work in both development and production environments
     * The method will return the path of the icon based on the current mode
     */
    public static ImageIcon getModeIcon(){
        String path = useNightMode ? "assets/day_mode.png" : "assets/night_mode.png";
        return loadIcon(path);
    }

    public static ImageIcon getAppIcon(){
        return loadIcon("assets/app_icon.png");
    }

    public static ImageIcon getAddIcon(){
        String path = useNightMode ? "assets/add_night.png" : "assets/add_day.png";
        return loadIcon(path);
    }

    public static ImageIcon getViewIcon(){
        String path = useNightMode ? "assets/view_night.png" : "assets/view_day.png";
        return loadIcon(path);
    }

    public static ImageIcon getDeleteIcon(){
        String path = useNightMode ? "assets/delete_night.png" : "assets/delete_day.png";
        return loadIcon(path);
    }

    public static ImageIcon getBackIcon(){
        String path = useNightMode ? "assets/back_night.png" : "assets/back_day.png";
        return loadIcon(path);
    }

    public static ImageIcon getEditIcon(){
        String path = useNightMode ? "assets/edit_night.png" : "assets/edit_day.png";
        return loadIcon(path);
    }

    public static ImageIcon getLogOutIcon(){
        String path = useNightMode ? "assets/logout_night.png" : "assets/logout_day.png";
        return loadIcon(path);
    }

    public static ImageIcon getUserConfigIcon(){
        String path = useNightMode ? "assets/userConfig_night.png" : "assets/userConfig_day.png";
        return loadIcon(path);
    }

    public static ImageIcon getSaveIcon(){
        String path = useNightMode ? "assets/save_night.png" : "assets/save_day.png";
        return loadIcon(path);
    }

    public static ImageIcon getSyncIcon(){
        String path = useNightMode ? "assets/sync_night.png" : "assets/sync_day.png";
        return loadIcon(path);
    }

    public static ImageIcon getSettingsIcon() {
        String path = useNightMode ? "assets/settings_night.png" : "assets/settings_day.png";
        return loadIcon(path);
    }

    public static ImageIcon getFilterIcon() {
        String path = useNightMode ? "assets/filter_night.png" : "assets/filter_day.png";
        return loadIcon(path);
    }

    public static ImageIcon getRestoreIcon(){
        String path = useNightMode ? "assets/restore_night.png" : "assets/restore_day.png";
        return loadIcon(path);
    }

    // Added methods for user action icons
    public static ImageIcon getLogoutIcon() {
        String path = useNightMode ? "assets/logout_night.png" : "assets/logout_day.png";
        return loadIcon(path);
    }

    public static ImageIcon getEditUserIcon() {
        String path = useNightMode ? "assets/editUser_night.png" : "assets/editUser_day.png";
        return loadIcon(path);
    }

    public static ImageIcon getDeleteUserIcon() {
        String path = useNightMode ? "assets/deleteUser_night.png" : "assets/deleteUser_day.png";
        return loadIcon(path);
    }


    public static void toggleColorMode(){
        useNightMode = !useNightMode;
    }

    private static ImageIcon loadIcon(String resourcePath){
        if (iconCache.containsKey(resourcePath)) {
            return iconCache.get(resourcePath);
        }
        try (InputStream is = common.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("Resource not found on classpath: " + resourcePath);
                return null;
            }
            Image image = ImageIO.read(is);
            image = image.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            ImageIcon icon = new ImageIcon(image);
            iconCache.put(resourcePath, icon);
            return icon;
        } catch (Exception e) {
            System.err.println("Error loading icon: " + resourcePath + " - " + e.getMessage());
            return null;
        }
    }

}