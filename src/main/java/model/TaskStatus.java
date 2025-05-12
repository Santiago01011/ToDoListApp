package model;

public enum TaskStatus {
    pending,
    in_progress,
    completed,
    incoming_due,
    overdue,
    newest;

    public static String getStatusToString(TaskStatus status) {
        switch (status) {
            case pending:
                return "Pending";
            case in_progress:
                return "In Progress";
            case completed:
                return "Completed";
            case incoming_due:
                return "Incoming Due";
            case overdue:
                return "Overdue";
            case newest:
                return "Newest";
            default:
                return "Unknown";
        }
    }

    public static TaskStatus getStringToStatus(String status) {
        switch (status) {
            case "Pending":
                return pending;
            case "In Progress":
                return in_progress;
            case "Completed":
                return completed;
            case "Incoming Due":
                return incoming_due;
            case "Overdue":
                return overdue;
            case "Newest":
                return newest;
            default:
                return null;
        }
    }
}
