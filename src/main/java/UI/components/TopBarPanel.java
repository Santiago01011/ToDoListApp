package UI.components;

import java.awt.Cursor;
import java.awt.Font;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.EnumMap;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;

import COMMON.common;
import model.TaskStatus;
import net.miginfocom.swing.MigLayout;

public class TopBarPanel extends JPanel {
    public interface Listener {
        void onFolderFilterChanged(String folder);
        void onStatusFilterChanged(Set<TaskStatus> statuses);
        void onClearFilters();
        void onSyncRequested();
        void onHistoryRequested();
        void onLogout();
        void onEditAccount();
        void onDeleteAccount();
        void onToggleTheme();
    }

    private Listener listener;
    private JComboBox<String> folderFilterBox;
    private JPopupMenu filterPopupMenu;
    private Map<TaskStatus, JCheckBoxMenuItem> statusCheckBoxMenuItems;
    private JPopupMenu userPopupMenu;
    private JButton toggleColorButton;

    public TopBarPanel(Listener listener) {
        this.listener = listener;
        statusCheckBoxMenuItems = new EnumMap<>(TaskStatus.class);
        setLayout(new MigLayout("insets 5 10 5 10, fillx", "[][][]push[][]", "[]"));
        setBackground(common.getPanelColor().darker());

        JLabel logoLabel = new JLabel(common.getAppIcon());
        add(logoLabel, "gapright 10");
        JLabel titleLabel = new JLabel("TaskFlow");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 21f));
        add(titleLabel, "gapright 15, aligny center");
        
        folderFilterBox = new JComboBox<>();
        folderFilterBox.addActionListener(e -> {
            String sel = (String) folderFilterBox.getSelectedItem();
            String folder = "All Folders".equals(sel) ? null : sel;
            listener.onFolderFilterChanged(folder);
        });
        add(folderFilterBox, "width 150!");
        
        JButton filterButton = new JButton(common.getFilterIcon());
        styleIcon(filterButton, "Filter Tasks");
        createFilterPopup();
        filterButton.addActionListener(e -> filterPopupMenu.show(filterButton, 0, filterButton.getHeight()));
        add(filterButton, "gapright 15");

        JButton userButton = new JButton(common.getUserConfigIcon());
        styleIcon(userButton, "User Profile");
        createUserPopup();
        userButton.addActionListener(e -> userPopupMenu.show(userButton, 0, userButton.getHeight()));
        add(userButton);

        toggleColorButton = new JButton(common.getModeIcon());
        styleIcon(toggleColorButton, "Toggle color mode");
        toggleColorButton.addActionListener(e -> listener.onToggleTheme());
        add(toggleColorButton, "aligny center, gapleft 10");
    }

    private void styleIcon(JButton btn, String tip) {
        btn.setToolTipText(tip);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void createFilterPopup() {
        filterPopupMenu = new JPopupMenu();
        JMenuItem clear = new JMenuItem("Clear All Filters");
        clear.addActionListener(e -> listener.onClearFilters());
        filterPopupMenu.add(clear);
        filterPopupMenu.addSeparator();

        JMenu statusFilterMenu = new JMenu("Filter by status");
        for (TaskStatus status : Arrays.asList(TaskStatus.pending, TaskStatus.completed, TaskStatus.in_progress)) {
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(getDisplayTextForStatus(status));
            item.addActionListener(e -> handleStatusItemClicked());
            statusCheckBoxMenuItems.put(status, item);
            statusFilterMenu.add(item);
        }
        filterPopupMenu.add(statusFilterMenu);
        filterPopupMenu.addSeparator();

        JMenu dateSortMenu = new JMenu("Sort by date");
        for (TaskStatus status : Arrays.asList(TaskStatus.overdue, TaskStatus.incoming_due, TaskStatus.newest)) {
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(getDisplayTextForStatus(status));
            item.addActionListener(e -> handleSortItemClicked());
            statusCheckBoxMenuItems.put(status, item);
            dateSortMenu.add(item);
        }
        filterPopupMenu.add(dateSortMenu);
    }

    
    private void handleStatusItemClicked() {
        Set<TaskStatus> selected = new HashSet<>();
        for (Map.Entry<TaskStatus, JCheckBoxMenuItem> entry : statusCheckBoxMenuItems.entrySet()) {
            if (entry.getValue().isSelected()) {
                selected.add(entry.getKey());
            }
        }
        listener.onStatusFilterChanged(selected);
    }

    private void handleSortItemClicked() {
        Set<TaskStatus> selected = new HashSet<>();
        for (Map.Entry<TaskStatus, JCheckBoxMenuItem> entry : statusCheckBoxMenuItems.entrySet()) {
            if (entry.getValue().isSelected()) {
                selected.add(entry.getKey());
            }
        }
        listener.onStatusFilterChanged(selected);
    }

    
    private String getDisplayTextForStatus(TaskStatus status) {
        switch (status) {
            case pending: return "Pending";
            case completed: return "Completed";
            case in_progress: return "In Progress";
            case overdue: return "Overdues";
            case incoming_due: return "Incomings";
            case newest: return "newests";
            default:
                String name = status.name().toLowerCase().replace('_', ' ');
                return Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }
    }

    private void createUserPopup() {
        userPopupMenu = new JPopupMenu();
        JMenuItem logout = new JMenuItem("Log out", common.getLogoutIcon());
        logout.addActionListener(e -> listener.onLogout()); userPopupMenu.add(logout);
        JMenuItem edit = new JMenuItem("Edit Account", common.getEditUserIcon());
        edit.addActionListener(e -> listener.onEditAccount()); userPopupMenu.add(edit);
        JMenuItem del = new JMenuItem("Delete Account", common.getDeleteUserIcon());
        del.addActionListener(e -> listener.onDeleteAccount()); userPopupMenu.add(del);
    }

    public void updateFolders(List<String> folders) {
        folderFilterBox.removeAllItems();
        folderFilterBox.addItem("All Folders");
        if (folders != null) folders.forEach(folderFilterBox::addItem);
        folderFilterBox.setSelectedIndex(0);
    }

    public void setSelectedStatuses(Set<TaskStatus> statuses) {
        if (statuses == null) statuses = Collections.emptySet();
        for (Map.Entry<TaskStatus, JCheckBoxMenuItem> entry : statusCheckBoxMenuItems.entrySet()) {
            entry.getValue().setSelected(statuses.contains(entry.getKey()));
        }
    }
}