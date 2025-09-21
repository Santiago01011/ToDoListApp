package service;

import model.TaskHandlerV2;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Batch persistence service that groups multiple write operations together
 * to reduce disk I/O and improve performance.
 */
public class BatchPersistenceService {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "BatchPersistence-" + System.currentTimeMillis());
        t.setDaemon(true);
        return t;
    });
    
    private final AtomicBoolean hasPendingWrites = new AtomicBoolean(false);
    private final long flushDelayMs;
    private TaskHandlerV2 taskHandler;
    
    public BatchPersistenceService(long flushDelayMs) {
        this.flushDelayMs = flushDelayMs;
    }
    
    /**
     * Set the task handler reference for persistence operations
     */
    public void setTaskHandler(TaskHandlerV2 taskHandler) {
        this.taskHandler = taskHandler;
    }
    
    /**
     * Schedule a persistence operation with batching delay
     */
    public void schedulePersistence() {
        if (hasPendingWrites.compareAndSet(false, true)) {
            scheduler.schedule(this::flush, flushDelayMs, TimeUnit.MILLISECONDS);
        }
        // If already scheduled, the existing schedule will handle the write
    }
    
    /**
     * Force immediate persistence (used during shutdown or critical operations)
     */
    public void flushImmediately() {
        hasPendingWrites.set(false); // Cancel any pending scheduled flush
        flush();
    }
    
    /**
     * Execute the actual persistence operation
     */
    private void flush() {
        if (taskHandler != null) {
            try {
                taskHandler.forcePersistence();
                hasPendingWrites.set(false);
                System.out.println("BatchPersistence: Successfully flushed tasks to disk");
            } catch (Exception e) {
                System.err.println("BatchPersistence: Failed to flush tasks: " + e.getMessage());
                hasPendingWrites.set(false);
            }
        }
    }
    
    /**
     * Check if there are pending writes waiting to be flushed
     */
    public boolean hasPendingWrites() {
        return hasPendingWrites.get();
    }
    
    /**
     * Shutdown the service and ensure all pending writes are completed
     */
    public void shutdown() {
        try {
            flushImmediately(); // Ensure all data is persisted
            scheduler.shutdown();
            
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                System.err.println("BatchPersistence: Force shutdown - some writes may be lost");
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            System.err.println("BatchPersistence: Interrupted during shutdown");
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
