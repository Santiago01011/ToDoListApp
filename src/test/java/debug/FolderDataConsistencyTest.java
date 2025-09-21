package debug;

import model.Folder;
import model.TaskHandlerV2;
import COMMON.UserProperties;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class FolderDataConsistencyTest {

    @Test
    public void testFolderNameVsIdConsistency() throws Exception {
        String userId = "consistency-test-" + UUID.randomUUID().toString();
        UserProperties.setProperty("userUUID", userId);
        
        // Clean up any existing data
        String tasksPath = UserProperties.getUserDataFilePath(userId, "tasks.json");
        File f = new File(tasksPath);
        if (f.exists()) f.delete();

        TaskHandlerV2 handler = new TaskHandlerV2(userId);
        
        // Create the folders that exist in the real app
        Folder kukaFolder = new Folder.Builder("0198b44f-e927-726d-90a0-10580c15685a")
            .folderName("Kuka")
            .build();
        Folder defaultFolder = new Folder.Builder("019669ce-a4d3-7e55-8375-294f932646bf")
            .folderName("Default Folder")
            .build();
            
        handler.setFoldersList(List.of(kukaFolder, defaultFolder));

        System.out.println("=== FOLDER DATA CONSISTENCY TEST ===");
        
        // Get the folder names that the UI would see
        List<String> uiFolderNames = handler.getFolderNamesList();
        System.out.println("UI sees these folders: " + uiFolderNames);
        
        // Test resolution for each folder name the UI shows
        for (String folderName : uiFolderNames) {
            String resolvedId = handler.getFolderIdByName(folderName);
            System.out.println("Folder '" + folderName + "' resolves to ID: '" + resolvedId + "'");
            
            if (resolvedId == null) {
                System.err.println("ERROR: UI shows folder '" + folderName + "' but resolution returns null!");
            }
        }
        
        // Specifically test the problematic case
        System.out.println("\n=== SPECIFIC KUKA TEST ===");
        String kukaId = handler.getFolderIdByName("Kuka");
        System.out.println("Kuka folder ID resolution: " + kukaId);
        
        if (kukaId == null) {
            System.err.println("CRITICAL: Kuka folder shows in UI but resolution fails!");
        } else if (!kukaId.equals("0198b44f-e927-726d-90a0-10580c15685a")) {
            System.err.println("CRITICAL: Kuka folder resolves to wrong ID: " + kukaId);
        } else {
            System.out.println("SUCCESS: Kuka folder resolves correctly");
        }
    }
}