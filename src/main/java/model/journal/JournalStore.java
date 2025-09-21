package model.journal;

import COMMON.JSONUtils;
import COMMON.UserProperties;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Local-only storage service for journal data.
 * All journal data is stored in user-specific directories and never synced to cloud.
 */
public class JournalStore {
    
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private final String userId;
    private final Path journalBasePath;

    public JournalStore(String userId) {
        this.userId = userId;
        this.journalBasePath = Paths.get(UserProperties.getUserDataDirectory(userId), "journal");
        initializeDirectories();
    }

    /**
     * Initialize the journal directory structure for a user
     */
    private void initializeDirectories() {
        try {
            Files.createDirectories(journalBasePath);
            Files.createDirectories(journalBasePath.resolve("entries"));
            Files.createDirectories(journalBasePath.resolve("sessions"));
            Files.createDirectories(journalBasePath.resolve("proposed"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize journal directories for user: " + userId, e);
        }
    }

    /**
     * Save a journal entry to local storage
     */
    public void saveEntry(JournalEntry entry) {
        String monthDir = entry.getCreatedAt().atZone(java.time.ZoneOffset.UTC)
                .toLocalDate().format(MONTH_FORMATTER);
        Path monthPath = journalBasePath.resolve("entries").resolve(monthDir);
        
        try {
            Files.createDirectories(monthPath);
            String filename = "entry" + entry.getId().toString() + ".json";
            Path filePath = monthPath.resolve(filename);
            
            atomicWrite(filePath, JSONUtils.toJsonString(entry));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save journal entry: " + entry.getId(), e);
        }
    }

    /**
     * Load a journal entry by ID
     */
    public Optional<JournalEntry> loadEntry(UUID entryId) {
        // Search through month directories for the entry
        try {
            Path entriesPath = journalBasePath.resolve("entries");
            if (!Files.exists(entriesPath)) {
                return Optional.empty();
            }
            
            return Files.list(entriesPath)
                    .filter(Files::isDirectory)
                    .map(monthDir -> monthDir.resolve("entry" + entryId.toString() + ".json"))
                    .filter(Files::exists)
                    .findFirst()
                    .map(this::loadEntryFromFile);
        } catch (IOException e) {
            System.err.println("Error loading journal entry " + entryId + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Load all entries for a given month
     */
    public List<JournalEntry> loadEntriesForMonth(LocalDate month) {
        String monthDir = month.format(MONTH_FORMATTER);
        Path monthPath = journalBasePath.resolve("entries").resolve(monthDir);
        
        if (!Files.exists(monthPath)) {
            return List.of();
        }
        
        try {
            return Files.list(monthPath)
                    .filter(path -> path.getFileName().toString().startsWith("entry"))
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .map(this::loadEntryFromFile)
                    .filter(Objects::nonNull)
                    .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                    .toList();
        } catch (IOException e) {
            System.err.println("Error loading entries for month " + monthDir + ": " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Save a journal session to local storage
     */
    public void saveSession(JournalSession session) {
        String monthDir = session.getStartedAt().atZone(java.time.ZoneOffset.UTC)
                .toLocalDate().format(MONTH_FORMATTER);
        Path monthPath = journalBasePath.resolve("sessions").resolve(monthDir);
        
        try {
            Files.createDirectories(monthPath);
            String filename = "session" + session.getId().toString() + ".json";
            Path filePath = monthPath.resolve(filename);
            
            atomicWrite(filePath, JSONUtils.toJsonString(session));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save journal session: " + session.getId(), e);
        }
    }

    /**
     * Load a journal session by ID
     */
    public Optional<JournalSession> loadSession(UUID sessionId) {
        try {
            Path sessionsPath = journalBasePath.resolve("sessions");
            if (!Files.exists(sessionsPath)) {
                return Optional.empty();
            }
            
            return Files.list(sessionsPath)
                    .filter(Files::isDirectory)
                    .map(monthDir -> monthDir.resolve("session" + sessionId.toString() + ".json"))
                    .filter(Files::exists)
                    .findFirst()
                    .map(this::loadSessionFromFile);
        } catch (IOException e) {
            System.err.println("Error loading journal session " + sessionId + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Save a proposed action to local storage
     */
    public void saveProposedAction(ProposedAction action) {
        String monthDir = action.getCreatedAt().atZone(java.time.ZoneOffset.UTC)
                .toLocalDate().format(MONTH_FORMATTER);
        Path monthPath = journalBasePath.resolve("proposed").resolve(monthDir);
        
        try {
            Files.createDirectories(monthPath);
            String filename = "proposal" + action.getId().toString() + ".json";
            Path filePath = monthPath.resolve(filename);
            
            atomicWrite(filePath, JSONUtils.toJsonString(action));
        } catch (IOException e) {
            throw new RuntimeException("Failed to save proposed action: " + action.getId(), e);
        }
    }

    /**
     * Load a proposed action by ID
     */
    public Optional<ProposedAction> loadProposedAction(UUID actionId) {
        try {
            Path proposedPath = journalBasePath.resolve("proposed");
            if (!Files.exists(proposedPath)) {
                return Optional.empty();
            }
            
            return Files.list(proposedPath)
                    .filter(Files::isDirectory)
                    .map(monthDir -> monthDir.resolve("proposal" + actionId.toString() + ".json"))
                    .filter(Files::exists)
                    .findFirst()
                    .map(this::loadProposedActionFromFile);
        } catch (IOException e) {
            System.err.println("Error loading proposed action " + actionId + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Get recent entries (across months if needed)
     */
    public List<JournalEntry> getRecentEntries(int limit) {
        List<JournalEntry> recentEntries = new ArrayList<>();
        LocalDate currentMonth = LocalDate.now();
        
        // Search backwards through months until we have enough entries
        for (int monthsBack = 0; monthsBack < 12 && recentEntries.size() < limit; monthsBack++) {
            LocalDate searchMonth = currentMonth.minusMonths(monthsBack);
            List<JournalEntry> monthEntries = loadEntriesForMonth(searchMonth);
            
            // Add entries in reverse chronological order
            for (int i = monthEntries.size() - 1; i >= 0 && recentEntries.size() < limit; i--) {
                recentEntries.add(monthEntries.get(i));
            }
        }
        
        return recentEntries;
    }

    /**
     * Atomic file write operation with temp file and move
     */
    private void atomicWrite(Path targetPath, String content) throws IOException {
        Path tempPath = targetPath.resolveSibling(targetPath.getFileName() + ".tmp");
        
        try {
            Files.writeString(tempPath, content, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            // Clean up temp file if it exists
            try {
                Files.deleteIfExists(tempPath);
            } catch (IOException cleanupError) {
                // Ignore cleanup errors
            }
            throw e;
        }
    }

    private JournalEntry loadEntryFromFile(Path filePath) {
        try {
            String json = Files.readString(filePath);
            return JSONUtils.fromJsonString(json, JournalEntry.class);
        } catch (IOException e) {
            System.err.println("Error loading journal entry from " + filePath + ": " + e.getMessage());
            return null;
        }
    }

    private JournalSession loadSessionFromFile(Path filePath) {
        try {
            String json = Files.readString(filePath);
            return JSONUtils.fromJsonString(json, JournalSession.class);
        } catch (IOException e) {
            System.err.println("Error loading journal session from " + filePath + ": " + e.getMessage());
            return null;
        }
    }

    private ProposedAction loadProposedActionFromFile(Path filePath) {
        try {
            String json = Files.readString(filePath);
            return JSONUtils.fromJsonString(json, ProposedAction.class);
        } catch (IOException e) {
            System.err.println("Error loading proposed action from " + filePath + ": " + e.getMessage());
            return null;
        }
    }
}