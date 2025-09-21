package service;

import model.Folder;
import COMMON.JSONUtils;
import COMMON.UserProperties;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Local folder cache service with TTL to reduce server calls for folder metadata.
 * Caches folders locally and refreshes only when TTL expires or version changes.
 */
public class FolderCacheService {
    private static final long CACHE_TTL_MS = TimeUnit.HOURS.toMillis(1); // 1 hour TTL
    private final Map<String, Folder> folderCache = new ConcurrentHashMap<>();
    private final AtomicLong lastRefresh = new AtomicLong(0);
    private final String cachePath;
    private final String userId;
    private String folderVersion;
    
    public FolderCacheService(String userId) {
        this.userId = userId;
        this.cachePath = UserProperties.getUserDataFilePath(userId, "folder_cache.json");
        loadFromPersistence();
    }
    
    /**
     * Get folder name by folder ID, refreshing from server if cache is stale
     */
    public Optional<String> getFolderName(String folderId) {
        if (folderId == null) {
            return Optional.empty();
        }
        
        if (needsRefresh()) {
            refreshFromServer();
        }
        
        Folder folder = folderCache.get(folderId);
        return folder != null ? Optional.of(folder.getFolder_name()) : Optional.empty();
    }
    
    /**
     * Get all cached folders
     */
    public List<Folder> getCachedFolders() {
        if (needsRefresh()) {
            refreshFromServer();
        }
        return new ArrayList<>(folderCache.values());
    }
    
    /**
     * Update folders in cache (called from sync operations)
     */
    public void updateFolders(List<Folder> folders) {
        updateFolders(folders, null);
    }
    
    /**
     * Update folders with version tracking
     */
    public void updateFolders(List<Folder> folders, String version) {
        folderCache.clear();
        if (folders != null) {
            folders.forEach(f -> {
                if (f != null && f.getFolder_id() != null) {
                    folderCache.put(f.getFolder_id(), f);
                }
            });
        }
        
        this.folderVersion = version;
        lastRefresh.set(System.currentTimeMillis());
        persistToLocal();
        
        System.out.println("FolderCache: Updated " + folderCache.size() + " folders" + 
                          (version != null ? " (version: " + version + ")" : ""));
    }
    
    /**
     * Check if the current folder version matches cached version
     */
    public boolean hasVersion(String version) {
        return Objects.equals(this.folderVersion, version);
    }
    
    /**
     * Get current cached folder version
     */
    public String getFolderVersion() {
        return folderVersion;
    }
    
    /**
     * Force cache refresh from server
     */
    public void forceRefresh() {
        lastRefresh.set(0); // Force refresh on next access
    }
    
    /**
     * Check if cache needs refresh based on TTL
     */
    private boolean needsRefresh() {
        return System.currentTimeMillis() - lastRefresh.get() > CACHE_TTL_MS;
    }
    
    /**
     * Refresh folders from server
     */
    private void refreshFromServer() {
        try {
            // Skip DB refresh if userId is not a UUID (e.g., in tests using simple strings)
            java.util.UUID uid;
            try {
                uid = java.util.UUID.fromString(userId);
            } catch (IllegalArgumentException iae) {
                // Non-UUID userId, likely test/offline context; keep cache as-is
                return;
            }
            System.out.println("FolderCache: Refreshing from server...");
            List<Folder> folders = fetchFoldersFromDatabase(uid);
            if (folders != null) {
                updateFolders(folders);
            }
        } catch (Exception e) {
            System.err.println("FolderCache: Failed to refresh from server: " + e.getMessage());
        }
    }

    private List<Folder> fetchFoldersFromDatabase(java.util.UUID uid) throws Exception {
        List<Folder> folders = new ArrayList<>();
        String sql = "SELECT * FROM todo.get_accessible_folders(?::uuid)";
        try (java.sql.Connection conn = DBH.NeonPool.getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, uid);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String id = null;
                    if (hasColumn(rs, "folder_id")) {
                        id = rs.getString("folder_id");
                    } else if (hasColumn(rs, "id")) {
                        id = rs.getString("id");
                    }

                    if (id == null) {
                        continue;
                    }

                    String name = null;
                    if (hasColumn(rs, "folder_name")) {
                        name = rs.getString("folder_name");
                    } else if (hasColumn(rs, "name")) {
                        name = rs.getString("name");
                    } else if (hasColumn(rs, "title")) {
                        name = rs.getString("title");
                    }

                    java.time.LocalDateTime createdAt = null;
                    if (hasColumn(rs, "created_at") && rs.getTimestamp("created_at") != null) {
                        createdAt = rs.getTimestamp("created_at").toLocalDateTime();
                    }

                    java.time.LocalDateTime deletedAt = null;
                    if (hasColumn(rs, "deleted_at") && rs.getTimestamp("deleted_at") != null) {
                        deletedAt = rs.getTimestamp("deleted_at").toLocalDateTime();
                    }

                    java.time.LocalDateTime lastSync = null;
                    if (hasColumn(rs, "last_sync") && rs.getTimestamp("last_sync") != null) {
                        lastSync = rs.getTimestamp("last_sync").toLocalDateTime();
                    }

                    Folder folder = new Folder.Builder(id)
                            .folderName(name)
                            .createdAt(createdAt)
                            .deletedAt(deletedAt)
                            .lastSync(lastSync)
                            .build();
                    folders.add(folder);
                }
            }
        }
        return folders;
    }

    private static boolean hasColumn(java.sql.ResultSet rs, String columnLabel) {
        try {
            java.sql.ResultSetMetaData rsmd = rs.getMetaData();
            int count = rsmd.getColumnCount();
            for (int i = 1; i <= count; i++) {
                if (columnLabel.equalsIgnoreCase(rsmd.getColumnLabel(i))) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }
    
    /**
     * Persist cache to local storage
     */
    private void persistToLocal() {
        try {
            Map<String, Object> cacheData = new HashMap<>();
            cacheData.put("folders", new ArrayList<>(folderCache.values()));
            cacheData.put("lastRefresh", lastRefresh.get());
            cacheData.put("folderVersion", folderVersion);
            
            JSONUtils.writeJsonFile(cacheData, cachePath);
        } catch (Exception e) {
            System.err.println("FolderCache: Failed to persist cache: " + e.getMessage());
        }
    }
    
    /**
     * Load cache from local storage
     */
    private void loadFromPersistence() {
        try {
            File cacheFile = new File(cachePath);
            if (cacheFile.exists()) {
                Map<String, Object> cacheData = JSONUtils.readJsonFile(cacheFile);
                
                // Load timestamp and version
                Object refreshObj = cacheData.get("lastRefresh");
                if (refreshObj instanceof Number) {
                    lastRefresh.set(((Number) refreshObj).longValue());
                }
                
                folderVersion = (String) cacheData.get("folderVersion");
                
                // Load folders
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> folderDataList = (List<Map<String, Object>>) cacheData.get("folders");
                if (folderDataList != null) {
                    for (Map<String, Object> folderData : folderDataList) {
                        try {
                            Folder folder = JSONUtils.getMapper().convertValue(folderData, Folder.class);
                            if (folder != null && folder.getFolder_id() != null) {
                                folderCache.put(folder.getFolder_id(), folder);
                            }
                        } catch (Exception e) {
                            System.err.println("FolderCache: Failed to parse folder data: " + e.getMessage());
                        }
                    }
                }
                
                System.out.println("FolderCache: Loaded " + folderCache.size() + " folders from cache" +
                                  (folderVersion != null ? " (version: " + folderVersion + ")" : ""));
            }
        } catch (Exception e) {
            System.err.println("FolderCache: Failed to load from persistence: " + e.getMessage());
        }
    }
    
    /**
     * Clear all cached data
     */
    public void clear() {
        folderCache.clear();
        folderVersion = null;
        lastRefresh.set(0);
        persistToLocal();
    }
}
