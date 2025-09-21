package model;

import java.util.Set;

public record FiltersCriteria(String folderName, Set<TaskStatus> statuses) {
    public static FiltersCriteria defaultCriteria() {
        return new FiltersCriteria(null, Set.of(TaskStatus.pending, TaskStatus.in_progress)); 
    }    
}
