package model;

public record FiltersCriteria(String folderName, Boolean filterByStatus, String status) {
    public static FiltersCriteria defaultCriteria() {
        return new FiltersCriteria(null, null, null); 
    }    
}
