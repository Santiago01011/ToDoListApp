package UI.components;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import COMMON.common;
import net.miginfocom.swing.MigLayout;

public class BottomBarPanel extends JPanel {
    public interface Listener {
        void onNewTask();
        void onSync();
        void onHistory();
    }
    @SuppressWarnings("unused")
    private Listener listener;
    private JLabel lastSyncLabel;
    private JButton newTaskBtn;
    private JButton historyButton;

    public BottomBarPanel(Listener listener) {
        this.listener = listener;
        setLayout(new MigLayout("insets 5 10 5 10, fillx", "[]push[][][]", "[]"));
        setBackground(getBackground().darker());

        newTaskBtn = new JButton("New task", new javax.swing.ImageIcon(
                common.getAddIcon().getImage().getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH)));
        
        add(newTaskBtn, "gapleft 5");

        lastSyncLabel = new JLabel("Sync status unknown");
        lastSyncLabel.setFont(lastSyncLabel.getFont().deriveFont(java.awt.Font.PLAIN, 12f));
        add(lastSyncLabel, "gapright 15");

        JButton syncButton = new JButton(common.getSyncIcon());
        syncButton.setToolTipText("Synchronize Tasks");
        syncButton.setBorderPainted(false);
        syncButton.setContentAreaFilled(false);
        syncButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        syncButton.addActionListener(e -> listener.onSync());
        add(syncButton, "gapright 15");

        historyButton = new JButton("History");
        historyButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        add(historyButton, "gapright 5");
        
        historyButton.addActionListener(e -> {
            newTaskBtn.setEnabled(false);
            historyButton.setEnabled(false);
            listener.onHistory();
        });
        newTaskBtn.addActionListener(e -> {
            newTaskBtn.setEnabled(false);
            historyButton.setEnabled(false);
            listener.onNewTask();
        });
    }
    
    public void enableBottomBarButtons() {
        newTaskBtn.setEnabled(true);
        historyButton.setEnabled(true);
    }

    public void setLastSync(LocalDateTime t) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss");
        if (t != null) {
            lastSyncLabel.setText("Last Sync: " + t.format(df));
        } else {
            lastSyncLabel.setText("Never synced");
        }
    }
}