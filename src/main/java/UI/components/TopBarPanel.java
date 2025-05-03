package UI.components;

import java.awt.Cursor;
import java.awt.Font;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
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
    private JCheckBoxMenuItem filterPendingItem;
    private JCheckBoxMenuItem filterCompletedItem;
    private JCheckBoxMenuItem filterInProgressItem;
    private JPopupMenu userPopupMenu;
    private JButton toggleColorButton;

    public TopBarPanel(Listener listener) {
        this.listener = listener;
        setLayout(new MigLayout("insets 5 10 5 10, fillx", "[][][]push[][]", "[]"));
        setBackground(common.getPanelColor().darker());

        // Logo and title
        JLabel logoLabel = new JLabel(common.getAppIcon());
        add(logoLabel, "gapright 10");
        JLabel titleLabel = new JLabel("TaskFlow");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 21f));
        add(titleLabel, "gapright 15, aligny center");

        // Folder filter
        folderFilterBox = new JComboBox<>();
        folderFilterBox.addActionListener(e -> {
            String sel = (String) folderFilterBox.getSelectedItem();
            String folder = "All Folders".equals(sel) ? null : sel;
            listener.onFolderFilterChanged(folder);
        });
        add(folderFilterBox, "width 150!");

        // Status filter button
        JButton filterButton = new JButton(common.getFilterIcon());
        styleIcon(filterButton, "Filter Tasks");
        createFilterPopup();
        filterButton.addActionListener(e -> filterPopupMenu.show(filterButton, 0, filterButton.getHeight()));
        add(filterButton, "gapright 15");

        // User menu button
        JButton userButton = new JButton(common.getUserConfigIcon());
        styleIcon(userButton, "User Profile");
        createUserPopup();
        userButton.addActionListener(e -> userPopupMenu.show(userButton, 0, userButton.getHeight()));
        add(userButton);

        // Theme toggle
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
        JMenuItem clear = new JMenuItem("Clear Status Filters");
        clear.addActionListener(e -> listener.onClearFilters());
        filterPopupMenu.add(clear);
        filterPopupMenu.addSeparator();
        filterPendingItem = new JCheckBoxMenuItem("Pending"); addStatusItem(filterPendingItem, TaskStatus.pending);
        filterCompletedItem = new JCheckBoxMenuItem("Completed"); addStatusItem(filterCompletedItem, TaskStatus.completed);
        filterInProgressItem = new JCheckBoxMenuItem("In Progress"); addStatusItem(filterInProgressItem, TaskStatus.in_progress);
        filterPopupMenu.add(filterPendingItem);
        filterPopupMenu.add(filterCompletedItem);
        filterPopupMenu.add(filterInProgressItem);
    }

    private void addStatusItem(JCheckBoxMenuItem item, TaskStatus status) {
        item.addActionListener(e -> {
            Set<TaskStatus> s = new HashSet<>();
            if (filterPendingItem.isSelected()) s.add(TaskStatus.pending);
            if (filterCompletedItem.isSelected()) s.add(TaskStatus.completed);
            if (filterInProgressItem.isSelected()) s.add(TaskStatus.in_progress);
            listener.onStatusFilterChanged(s);
        });
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
        filterPendingItem.setSelected(statuses.contains(TaskStatus.pending));
        filterCompletedItem.setSelected(statuses.contains(TaskStatus.completed));
        filterInProgressItem.setSelected(statuses.contains(TaskStatus.in_progress));
    }
}