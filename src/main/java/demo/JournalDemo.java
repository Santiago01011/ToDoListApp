package demo;

import COMMON.UserProperties;
import service.journal.JournalService;
import model.journal.*;
import java.util.List;

/**
 * Simple demo to showcase journal functionality.
 * This demonstrates the core journal features working locally.
 */
public class JournalDemo {
    
    public static void main(String[] args) {
        System.out.println("=== TrashTDL Journal Demo ===");
        
        // Set up demo user - ensure user properties are initialized
        String demoUserId = "demo-user-123";
        
        // Force initialization of user properties with journal defaults
        UserProperties.setProperty("userUUID", demoUserId);
        UserProperties.setProperty("journal.enabled", "true");
        UserProperties.setProperty("journal.autoParse", "true");
        UserProperties.setProperty("journal.voice.enabled", "false");
        UserProperties.setProperty("journal.enrichment.mode", "OFF");
        
        // Create journal service
        JournalService journalService = new JournalService(demoUserId);
        
        System.out.println("Journal settings:");
        System.out.println("- Enabled: " + UserProperties.isJournalEnabled());
        System.out.println("- Auto-parse: " + UserProperties.isJournalAutoParse());
        System.out.println("- Voice enabled: " + UserProperties.isJournalVoiceEnabled());
        System.out.println("- Enrichment mode: " + UserProperties.getJournalEnrichmentMode());
        
        // Debug the actual property values
        System.out.println("Debug raw properties:");
        System.out.println("- journal.enabled: " + UserProperties.getProperty("journal.enabled"));
        System.out.println("- journal.autoParse: " + UserProperties.getProperty("journal.autoParse"));
        System.out.println();
        
        try {
            // Start a journal session
            System.out.println("Starting journal session...");
            JournalSession session = journalService.startSession();
            System.out.println("Session ID: " + session.getId());
            System.out.println();
            
            // Add some journal entries
            System.out.println("Adding journal entries...");
            
            String[] journalTexts = {
                "Need to buy groceries tomorrow. Should include milk, bread, and eggs.",
                "Finished the presentation for Monday's meeting. It went really well.",
                "Should call mom this weekend to check how she's doing.",
                "Move the dentist appointment to next Tuesday.",
                "Need to review the quarterly budget by Friday."
            };
            
            for (String text : journalTexts) {
                System.out.println("Entry: \"" + text + "\"");
                JournalEntry entry = journalService.addTextEntry(text);
                System.out.println("Entry ID: " + entry.getId());
                System.out.println();
            }
            
            // Show session summary
            System.out.println("=== Session Summary ===");
            List<JournalEntry> sessionEntries = journalService.getCurrentSessionEntries();
            System.out.println("Total entries: " + sessionEntries.size());
            
            List<ProposedAction> proposals = journalService.getCurrentSessionProposals();
            System.out.println("Total proposals: " + proposals.size());
            System.out.println();
            
            // Show generated proposals
            if (!proposals.isEmpty()) {
                System.out.println("=== Generated Proposals ===");
                for (ProposedAction proposal : proposals) {
                    System.out.println("Type: " + proposal.getType());
                    System.out.println("Confidence: " + String.format("%.1f%%", proposal.getConfidence() * 100));
                    System.out.println("Rationale: " + proposal.getRationale());
                    System.out.println("Payload: " + proposal.getPayload());
                    System.out.println("---");
                }
            }
            
            // End session
            System.out.println("Ending journal session...");
            journalService.endSession();
            
            // Show recent entries
            System.out.println("\n=== Recent Entries ===");
            List<JournalEntry> recentEntries = journalService.getRecentEntries(5);
            for (JournalEntry entry : recentEntries) {
                System.out.println("- " + entry.getRawText().substring(0, Math.min(50, entry.getRawText().length())) + "...");
            }
            
            System.out.println("\n=== Demo Complete ===");
            System.out.println("Journal data is stored locally in: ~/.todoapp/users/" + demoUserId + "/journal/");
            
        } catch (Exception e) {
            System.err.println("Error in journal demo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}