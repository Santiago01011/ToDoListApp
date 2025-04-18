package COMMON;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Image;


public class common {

    // Variable to store the current mode based on the time
    //private static boolean useNightMode = (java.time.LocalTime.now().getHour() >= 22 || java.time.LocalTime.now().getHour() < 7) ? true : false;
    //public static boolean useNightMode = false;
    public static boolean useNightMode = Boolean.valueOf((String) UserProperties.getProperty("darkTheme"));

    // Day mode colors
    public static final Color PRIMARY_COLOR_DAY = Color.decode("#f6d76f");  //hex #f6d76f
    public static final Color PANEL_COLOR_DAY = Color.decode("#eed06d");  //hex #eed06d
    public static final Color SECONDARY_COLOR_DAY = Color.decode("#ffea00");  //hex #ffea00
    public static final Color TERTIARY_COLOR_DAY = Color.decode("#fff989");  //hex #fff989
    public static final Color TEXT_COLOR_DAY = Color.decode("#473b00");  //hex #473b00

    // Night mode colors
    public static final Color PRIMARY_COLOR_NIGHT = Color.decode("#1a144b");  //hex #1a144b
    public static final Color PANEL_COLOR_NIGHT = Color.decode("#0c005b");  //hex #0c005b
    public static final Color SECONDARY_COLOR_NIGHT = Color.decode("#221f1f");  //hex #221f1f
    public static final Color TERTIARY_COLOR_NIGHT = Color.decode("#130814");  //hex #130814
    public static final Color TEXT_COLOR_NIGHT = Color.decode("#fab500");  //hex #fab500

    // Methods to get the current colors based on the mode
    public static Color getPrimaryColor() {
        return useNightMode ? PRIMARY_COLOR_NIGHT : PRIMARY_COLOR_DAY;
    }

    public static Color getPanelColor() {
        return useNightMode ? PANEL_COLOR_NIGHT : PANEL_COLOR_DAY;
    }

    public static Color getSecondaryColor() {
        return useNightMode ? SECONDARY_COLOR_NIGHT : SECONDARY_COLOR_DAY;
    }

    public static Color getTertiaryColor() {
        return useNightMode ? TERTIARY_COLOR_NIGHT : TERTIARY_COLOR_DAY;
    }

    public static Color getTextColor(){
        return useNightMode ? TEXT_COLOR_NIGHT : TEXT_COLOR_DAY;
    }

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

    public static ImageIcon getRestoreIcon(){
        String path = useNightMode ? "assets/restore_night.png" : "assets/restore_day.png";
        return loadIcon(path);
    }

    // Method to toggle the color mode
    public static void toggleColorMode(){
        useNightMode = !useNightMode;
    }

    private static ImageIcon loadIcon(String resourcePath){
        try {
            InputStream is = common.class.getClassLoader().getResourceAsStream(resourcePath);

            if (is == null)
                is = new FileInputStream("src/main/resources/" + resourcePath);

            Image image = ImageIO.read(is);
            image = image.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            return new ImageIcon(image);
        }catch (Exception e){
            System.err.println("Error loading icon: " + resourcePath);
            //e.printStackTrace();
            return null;
        }
    }

}