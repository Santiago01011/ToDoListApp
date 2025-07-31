package themes;

/**
 * Simple test runner specifically for theme-related tests
 * Run this to validate all theme functionality
 */
public class ThemeTestRunner {
    
    public static void main(String[] args) {
        System.out.println("=== Running Theme Tests ===");
        System.out.println("Note: This is a simple test runner. Use 'mvn test' for full test execution.");
        System.out.println();
        
        try {
            // Test theme creation
            testThemeCreation();
            
            // Test theme properties
            testThemeProperties();
            
            System.out.println("✅ Basic theme validation completed successfully!");
            System.out.println("🔧 Run 'mvn test -Dtest=*Theme*Test' to execute full theme test suite");
            
        } catch (Exception e) {
            System.err.println("❌ Theme validation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testThemeCreation() {
        System.out.println("Testing theme creation...");
        
        // Test CoffeYellow
        CoffeYellow coffeeTheme = new CoffeYellow();
        if (coffeeTheme == null) {
            throw new RuntimeException("Failed to create CoffeYellow theme");
        }
        if (!"CoffeYellow".equals(coffeeTheme.getName())) {
            throw new RuntimeException("CoffeYellow theme has incorrect name: " + coffeeTheme.getName());
        }
        
        // Test NigthBlue
        NigthBlue nightTheme = new NigthBlue();
        if (nightTheme == null) {
            throw new RuntimeException("Failed to create NigthBlue theme");
        }
        if (!"NigthBlue".equals(nightTheme.getName())) {
            throw new RuntimeException("NigthBlue theme has incorrect name: " + nightTheme.getName());
        }
        
        System.out.println("  ✓ Theme creation successful");
    }
    
    private static void testThemeProperties() {
        System.out.println("Testing theme properties files...");
        
        // Test CoffeYellow properties
        if (CoffeYellow.class.getResourceAsStream("/themes/CoffeYellow.properties") == null) {
            throw new RuntimeException("CoffeYellow.properties file not found");
        }
        
        // Test NigthBlue properties
        if (NigthBlue.class.getResourceAsStream("/themes/NigthBlue.properties") == null) {
            throw new RuntimeException("NigthBlue.properties file not found");
        }
        
        System.out.println("  ✓ Theme properties files found");
    }
}
