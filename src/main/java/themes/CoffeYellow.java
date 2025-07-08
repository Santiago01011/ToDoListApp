package themes;

import com.formdev.flatlaf.FlatLightLaf;

public class CoffeYellow extends FlatLightLaf {
    public static final String NAME = "CoffeYellow";

    public static boolean setup() {
        return setup( new CoffeYellow() );
    }

    public static void installLafInfo() {
        installLafInfo( NAME, CoffeYellow.class );
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "CoffeYellow Theme";
    }
}