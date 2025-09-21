package service.journal;

import COMMON.UserProperties;
import model.journal.*;
import model.commands.*;
import java.time.Instant;
import java.util.*;

/**
 * Main service for journal operations.
 * Handles entry creation, session management, and local storage.
 * All operations are local-only to preserve privacy.
 */
public class JournalService {
    
    private final String userId;
    private final JournalStore journalStore;
    private final JournalTextProcessor textProcessor;
    private JournalSession currentSession;

    public JournalService(String userId) {
        this.userId = userId;
        this.journalStore = new JournalStore(userId);
        this.textProcessor = new JournalTextProcessor();
    }

    /**
     * Start a new journal session
     */
    public JournalSession startSession() {
        if (currentSession != null && currentSession.getStatus() == JournalSession.Status.ACTIVE) {
            throw new IllegalStateException("A session is already active");
        }
        
        Map<String, Object> settingsSnapshot = createSettingsSnapshot();
        
        currentSession = JournalSession.builder()
                .settingsSnapshot(settingsSnapshot)
                .build();
        
        journalStore.saveSession(currentSession);
        
        System.out.println("Journal session started: " + currentSession.getId());
        return currentSession;
    }

    /**
     * End the current session
     */
    public void endSession() {
        if (currentSession == null || currentSession.getStatus() != JournalSession.Status.ACTIVE) {
            throw new IllegalStateException("No active session to end");
        }
        
        currentSession = currentSession.toBuilder()
                .endedAt(Instant.now())
                .status(JournalSession.Status.ENDED)
                .build();
        
        journalStore.saveSession(currentSession);
        
        System.out.println("Journal session ended: " + currentSession.getId());
        currentSession = null;
    }

    /**
     * Add a text entry to the current session
     */
    public JournalEntry addTextEntry(String text) {
        if (currentSession == null || currentSession.getStatus() != JournalSession.Status.ACTIVE) {
            throw new IllegalStateException("No active session. Start a session first.");
        }
        
        JournalEntry entry = JournalEntry.builder()
                .sessionId(currentSession.getId())
                .rawText(text)
                .source(JournalEntry.Source.TEXT)
                .build();
        
        // Save entry
        journalStore.saveEntry(entry);
        
        // Update session with new entry
        List<UUID> updatedEntryIds = new ArrayList<>(currentSession.getEntryIds());
        updatedEntryIds.add(entry.getId());
        
        currentSession = currentSession.toBuilder()
                .entryIds(updatedEntryIds)
                .build();
        
        journalStore.saveSession(currentSession);
        
        // Process for proposals if auto-parse is enabled
        if (UserProperties.isJournalAutoParse()) {
            processEntryForProposals(entry);
        }
        
        System.out.println("Journal entry added: " + entry.getId());
        return entry;
    }

    /**
     * Process an entry to generate proposals
     */
    public List<ProposedAction> processEntryForProposals(JournalEntry entry) {
        if (!UserProperties.isJournalEnabled()) {
            return List.of();
        }
        
        List<ProposedAction> proposals = textProcessor.generateProposals(entry);
        
        // Save proposals
        for (ProposedAction proposal : proposals) {
            journalStore.saveProposedAction(proposal);
        }
        
        // Update session with new proposals
        if (currentSession != null) {
            List<UUID> updatedProposalIds = new ArrayList<>(currentSession.getProposedActionIds());
            proposals.forEach(p -> updatedProposalIds.add(p.getId()));
            
            currentSession = currentSession.toBuilder()
                    .proposedActionIds(updatedProposalIds)
                    .build();
            
            journalStore.saveSession(currentSession);
        }
        
        System.out.println("Generated " + proposals.size() + " proposals for entry: " + entry.getId());
        return proposals;
    }

    /**
     * Get recent journal entries
     */
    public List<JournalEntry> getRecentEntries(int limit) {
        return journalStore.getRecentEntries(limit);
    }

    /**
     * Get current active session
     */
    public JournalSession getCurrentSession() {
        return currentSession;
    }

    /**
     * Load a session by ID and set it as current (for resuming)
     */
    public void resumeSession(UUID sessionId) {
        Optional<JournalSession> session = journalStore.loadSession(sessionId);
        if (session.isEmpty()) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }
        
        if (session.get().getStatus() == JournalSession.Status.ENDED) {
            throw new IllegalStateException("Cannot resume ended session: " + sessionId);
        }
        
        currentSession = session.get().toBuilder()
                .status(JournalSession.Status.ACTIVE)
                .build();
        
        journalStore.saveSession(currentSession);
    }

    /**
     * Get entries for the current session
     */
    public List<JournalEntry> getCurrentSessionEntries() {
        if (currentSession == null) {
            return List.of();
        }
        
        return currentSession.getEntryIds().stream()
                .map(journalStore::loadEntry)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                .toList();
    }

    /**
     * Get pending proposals for the current session
     */
    public List<ProposedAction> getCurrentSessionProposals() {
        if (currentSession == null) {
            return List.of();
        }
        
        return currentSession.getProposedActionIds().stream()
                .map(journalStore::loadProposedAction)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(p -> p.getDecision() == ProposedAction.Decision.PENDING)
                .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                .toList();
    }

    private Map<String, Object> createSettingsSnapshot() {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("voiceEnabled", UserProperties.isJournalVoiceEnabled());
        snapshot.put("enrichmentMode", UserProperties.getJournalEnrichmentMode());
        snapshot.put("autoParse", UserProperties.isJournalAutoParse());
        return snapshot;
    }
}