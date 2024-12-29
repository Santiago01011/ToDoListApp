package main.COMMON;

import java.awt.Color;


public class common {

    private static boolean useNightMode = true; // Flag to determine which palette to use

    // Day mode colors
    public static final Color PRIMARY_COLOR_DAY = Color.decode("#f6d76f");  //hex #f6d76f
    public static final Color SECONDARY_COLOR_DAY = Color.decode("#ffea00");  //hex #ffea00
    public static final Color TERTIARY_COLOR_DAY = Color.decode("#fff989");  //hex #fff989
    public static final Color TEXT_COLOR_DAY = Color.decode("#473b00");  //hex #473b00

    // Night mode colors
    public static final Color PRIMARY_COLOR_NIGHT = Color.decode("#1a144b");  //hex #1a144b
    public static final Color SECONDARY_COLOR_NIGHT = Color.decode("#221f1f");  //hex #221f1f
    public static final Color TERTIARY_COLOR_NIGHT = Color.decode("#130814");  //hex #130814
    public static final Color TEXT_COLOR_NIGHT = Color.decode("#fab500");  //hex #fab500

    // Methods to get the current colors based on the mode
    public static Color getPrimaryColor() {
        return useNightMode ? PRIMARY_COLOR_NIGHT : PRIMARY_COLOR_DAY;
    }

    public static Color getSecondaryColor() {
        return useNightMode ? SECONDARY_COLOR_NIGHT : PRIMARY_COLOR_DAY;
    }

    public static Color getTertiaryColor() {
        return useNightMode ? TERTIARY_COLOR_NIGHT : TERTIARY_COLOR_DAY;
    }

    public static Color getTextColor(){
        return useNightMode ? TEXT_COLOR_NIGHT : TEXT_COLOR_DAY;
    }

    public static String getModePath(){
        String path = useNightMode ? "src/main/assets/day_mode.png" : "src/main/assets/night_mode.png";
        return path;
    }

    public static String getDeletePath(){
        String path = useNightMode ? "src/main/assets/delete_night.png" : "src/main/assets/delete_day.png";
        return path;
    }

    public static String getViewPath(){
        String path = useNightMode ? "src/main/assets/view_night.png" : "src/main/assets/view_day.png";
        return path;
    }

    public static String getBackPath(){
        String path = useNightMode ? "src/main/assets/back_night.png" : "src/main/assets/back_day.png";
        return path;
    }
    
    // Method to toggle the color mode
    public static void toggleColorMode(){
        useNightMode = !useNightMode;
    }
}