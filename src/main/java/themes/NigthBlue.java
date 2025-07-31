package themes;

import com.formdev.flatlaf.FlatDarculaLaf;

public class NigthBlue extends FlatDarculaLaf {
	public static final String NAME = "NigthBlue";

	public static boolean setup() {
		return setup( new NigthBlue() );
	}

	public static void installLafInfo() {
		installLafInfo( NAME, NigthBlue.class );
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getDescription() {
		return "NigthBlue Theme";
	}
}
