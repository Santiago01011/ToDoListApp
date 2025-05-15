package themes;

import com.formdev.flatlaf.FlatLightLaf;

public class CoffeYellow extends FlatLightLaf {
    public static boolean setup() {
        return setup( new CoffeYellow() );
    }

    @Override
    public String getName() {
        return "CoffeYellow";
    }
}