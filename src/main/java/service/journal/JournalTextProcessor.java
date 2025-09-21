package service.journal;

import model.journal.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Local text processor for journal entries.
 * Uses rule-based patterns to extract intents and generate task proposals.
 * All processing is done locally to preserve privacy.
 */
public class JournalTextProcessor {
    
    // Intent patterns for task creation
    private static final Pattern CREATE_TASK_PATTERN = Pattern.compile(
        "(?i)(?:need to|should|must|have to|todo|task|create)\\s+(.+?)(?:\\s+(?:by|due|deadline)\\s+(.+?))?(?:\\.|$)",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern COMPLETE_TASK_PATTERN = Pattern.compile(
        "(?i)(?:finished|completed|done with|finished with)\\s+(.+?)(?:\\.|$)",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern RESCHEDULE_PATTERN = Pattern.compile(
        "(?i)(?:move|reschedule|postpone)\\s+(.+?)\\s+(?:to|for)\\s+(.+?)(?:\\.|$)",
        Pattern.CASE_INSENSITIVE
    );
    
    // Date patterns (simple regex for common formats)
    private static final Pattern DATE_PATTERN = Pattern.compile(
        "(?i)(?:today|tomorrow|yesterday|monday|tuesday|wednesday|thursday|friday|saturday|sunday|" +
        "\\d{1,2}[/-]\\d{1,2}(?:[/-]\\d{2,4})?|" +
        "\\d{1,2}\\s+(?:january|february|march|april|may|june|july|august|september|october|november|december)|" +
        "next\\s+\\w+|this\\s+\\w+)"
    );
    
    // Time patterns
    private static final Pattern TIME_PATTERN = Pattern.compile(
        "(?i)(?:\\d{1,2}(?::\\d{2})?\\s*(?:am|pm)|\\d{1,2}\\s*(?:am|pm))"
    );

    /**
     * Generate proposed actions from a journal entry
     */
    public List<ProposedAction> generateProposals(JournalEntry entry) {
        List<ProposedAction> proposals = new ArrayList<>();
        String text = entry.getRawText();
        
        if (text == null || text.trim().isEmpty()) {
            return proposals;
        }
        
        // Normalize text
        String normalizedText = normalizeText(text);
        
        // Extract task creation intents
        proposals.addAll(extractCreateTaskProposals(entry, normalizedText));
        
        // Extract completion intents
        proposals.addAll(extractCompleteTaskProposals(entry, normalizedText));
        
        // Extract reschedule intents
        proposals.addAll(extractRescheduleProposals(entry, normalizedText));
        
        return proposals;
    }

    private String normalizeText(String text) {
        // Basic normalization
        return text.trim()
                .replaceAll("\\s+", " ")  // Collapse multiple spaces
                .replaceAll("[\\u201C\\u201D\\u2018\\u2019`]", "\"")  // Normalize quotes
                .replaceAll("\\u2026", "...");  // Normalize ellipsis
    }

    private List<ProposedAction> extractCreateTaskProposals(JournalEntry entry, String text) {
        List<ProposedAction> proposals = new ArrayList<>();
        
        Matcher matcher = CREATE_TASK_PATTERN.matcher(text);
        
        while (matcher.find()) {
            String taskTitle = matcher.group(1).trim();
            String dueDateText = matcher.group(2);
            
            if (taskTitle.isEmpty() || taskTitle.length() < 3) {
                continue; // Skip very short or empty tasks
            }
            
            // Clean up task title
            taskTitle = cleanTaskTitle(taskTitle);
            
            // Parse due date if present
            LocalDateTime dueDate = null;
            if (dueDateText != null && !dueDateText.trim().isEmpty()) {
                dueDate = parseDateTime(dueDateText.trim());
            }
            
            // Create proposal payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("title", taskTitle);
            payload.put("description", "Created from journal entry");
            payload.put("journalId", entry.getId().toString());
            
            if (dueDate != null) {
                payload.put("dueDate", dueDate.toString());
            }
            
            // Calculate confidence based on pattern match strength
            double confidence = calculateCreateTaskConfidence(taskTitle, dueDateText);
            
            String rationale = String.format("Task creation detected: '%s'%s", 
                taskTitle, 
                dueDate != null ? " with due date: " + dueDateText : "");
            
            ProposedAction proposal = ProposedAction.builder()
                    .entryId(entry.getId())
                    .type(ProposedAction.Type.CREATE_TASK)
                    .payload(payload)
                    .rationale(rationale)
                    .confidence(confidence)
                    .build();
            
            proposals.add(proposal);
        }
        
        return proposals;
    }

    private List<ProposedAction> extractCompleteTaskProposals(JournalEntry entry, String text) {
        List<ProposedAction> proposals = new ArrayList<>();
        
        Matcher matcher = COMPLETE_TASK_PATTERN.matcher(text);
        
        while (matcher.find()) {
            String taskReference = matcher.group(1).trim();
            
            if (taskReference.isEmpty() || taskReference.length() < 3) {
                continue;
            }
            
            taskReference = cleanTaskTitle(taskReference);
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("taskReference", taskReference);
            payload.put("journalId", entry.getId().toString());
            
            double confidence = calculateCompleteTaskConfidence(taskReference);
            
            String rationale = String.format("Task completion detected: '%s'", taskReference);
            
            ProposedAction proposal = ProposedAction.builder()
                    .entryId(entry.getId())
                    .type(ProposedAction.Type.COMPLETE_TASK)
                    .payload(payload)
                    .rationale(rationale)
                    .confidence(confidence)
                    .build();
            
            proposals.add(proposal);
        }
        
        return proposals;
    }

    private List<ProposedAction> extractRescheduleProposals(JournalEntry entry, String text) {
        List<ProposedAction> proposals = new ArrayList<>();
        
        Matcher matcher = RESCHEDULE_PATTERN.matcher(text);
        
        while (matcher.find()) {
            String taskReference = matcher.group(1).trim();
            String newDateText = matcher.group(2).trim();
            
            if (taskReference.isEmpty() || newDateText.isEmpty()) {
                continue;
            }
            
            LocalDateTime newDate = parseDateTime(newDateText);
            if (newDate == null) {
                continue; // Skip if we can't parse the new date
            }
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("taskReference", cleanTaskTitle(taskReference));
            payload.put("newDueDate", newDate.toString());
            payload.put("journalId", entry.getId().toString());
            
            double confidence = calculateRescheduleConfidence(taskReference, newDateText);
            
            String rationale = String.format("Task reschedule detected: '%s' to '%s'", 
                taskReference, newDateText);
            
            ProposedAction proposal = ProposedAction.builder()
                    .entryId(entry.getId())
                    .type(ProposedAction.Type.RESCHEDULE_TASK)
                    .payload(payload)
                    .rationale(rationale)
                    .confidence(confidence)
                    .build();
            
            proposals.add(proposal);
        }
        
        return proposals;
    }

    private String cleanTaskTitle(String title) {
        // Remove common prefixes and suffixes
        title = title.replaceAll("(?i)^(to\\s+|a\\s+|the\\s+)", "");
        title = title.replaceAll("(?i)(\\s+today|\\s+tomorrow|\\s+asap)$", "");
        
        // Capitalize first letter
        if (!title.isEmpty()) {
            title = title.substring(0, 1).toUpperCase() + 
                   (title.length() > 1 ? title.substring(1) : "");
        }
        
        return title.trim();
    }

    private LocalDateTime parseDateTime(String dateTimeText) {
        if (dateTimeText == null || dateTimeText.trim().isEmpty()) {
            return null;
        }
        
        dateTimeText = dateTimeText.toLowerCase().trim();
        LocalDateTime now = LocalDateTime.now();
        
        // Handle relative dates
        switch (dateTimeText) {
            case "today":
                return now.toLocalDate().atTime(23, 59);
            case "tomorrow":
                return now.plusDays(1).toLocalDate().atTime(23, 59);
            case "yesterday":
                return now.minusDays(1).toLocalDate().atTime(23, 59);
        }
        
        // Handle day names
        if (dateTimeText.matches("(?i)(monday|tuesday|wednesday|thursday|friday|saturday|sunday)")) {
            return getNextWeekday(dateTimeText, now);
        }
        
        // Handle "next" patterns
        if (dateTimeText.startsWith("next ")) {
            String day = dateTimeText.substring(5);
            if (day.matches("(?i)(monday|tuesday|wednesday|thursday|friday|saturday|sunday)")) {
                return getNextWeekday(day, now.plusWeeks(1));
            }
        }
        
        // Try to parse common date formats
        String[] formats = {
            "M/d/yyyy", "M/d/yy", "M/d",
            "M-d-yyyy", "M-d-yy", "M-d",
            "d/M/yyyy", "d/M/yy", "d/M",
            "yyyy-M-d", "yyyy/M/d"
        };
        
        for (String format : formats) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                return LocalDateTime.parse(dateTimeText + " 23:59", 
                    DateTimeFormatter.ofPattern(format + " HH:mm"));
            } catch (DateTimeParseException e) {
                // Try next format
            }
        }
        
        return null;
    }

    private LocalDateTime getNextWeekday(String dayName, LocalDateTime fromDate) {
        int targetDay = getDayOfWeek(dayName.toLowerCase());
        if (targetDay == -1) return null;
        
        int currentDay = fromDate.getDayOfWeek().getValue();
        int daysToAdd = (targetDay - currentDay + 7) % 7;
        if (daysToAdd == 0) daysToAdd = 7; // Next occurrence, not today
        
        return fromDate.plusDays(daysToAdd).toLocalDate().atTime(23, 59);
    }

    private int getDayOfWeek(String dayName) {
        return switch (dayName) {
            case "monday" -> 1;
            case "tuesday" -> 2;
            case "wednesday" -> 3;
            case "thursday" -> 4;
            case "friday" -> 5;
            case "saturday" -> 6;
            case "sunday" -> 7;
            default -> -1;
        };
    }

    private double calculateCreateTaskConfidence(String taskTitle, String dueDateText) {
        double confidence = 0.6; // Base confidence
        
        // Boost confidence for certain keywords
        if (taskTitle.toLowerCase().matches(".*(urgent|important|asap|deadline).*")) {
            confidence += 0.2;
        }
        
        // Boost confidence if due date is present
        if (dueDateText != null && !dueDateText.trim().isEmpty()) {
            confidence += 0.1;
        }
        
        // Boost confidence for action verbs
        if (taskTitle.toLowerCase().matches(".*(buy|call|send|write|finish|complete|review).*")) {
            confidence += 0.1;
        }
        
        return Math.min(confidence, 0.95); // Cap at 95%
    }

    private double calculateCompleteTaskConfidence(String taskReference) {
        double confidence = 0.7; // Higher base confidence for completion
        
        // Boost for clear completion words
        if (taskReference.toLowerCase().matches(".*(project|task|work|assignment).*")) {
            confidence += 0.1;
        }
        
        return Math.min(confidence, 0.9);
    }

    private double calculateRescheduleConfidence(String taskReference, String newDateText) {
        double confidence = 0.6;
        
        // Boost if we can parse the date
        if (parseDateTime(newDateText) != null) {
            confidence += 0.2;
        }
        
        return Math.min(confidence, 0.85);
    }
}