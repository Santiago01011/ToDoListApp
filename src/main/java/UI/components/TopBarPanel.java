package UI.components;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.EnumMap;
import java.util.Arrays;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;

import COMMON.common;
import model.TaskStatus;
import net.miginfocom.swing.MigLayout;
import UI.UIConstants;
import UI.UIUtils;

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
    private Map<TaskStatus, JCheckBoxMenuItem> filterCheckBoxMenuItems;
    private Map<TaskStatus, JRadioButtonMenuItem> sortRadioButtonMenuItems;
    private ButtonGroup sortButtonGroup;
    private JPopupMenu userPopupMenu;
    private JButton toggleColorButton;
    private Set<TaskStatus> selectedCriterias = Collections.emptySet();

    public TopBarPanel(Listener listener) {
        this.listener = listener;
        filterCheckBoxMenuItems = new EnumMap<>(TaskStatus.class);
        sortRadioButtonMenuItems = new EnumMap<>(TaskStatus.class);
        sortButtonGroup = new ButtonGroup();
        setLayout(new MigLayout("insets 5 10 5 10, fillx", "[][][]push[][]", "[]"));
        setBackground(getBackground().darker());

        JLabel logoLabel = new JLabel(common.getAppIcon());
        add(logoLabel, "gapright 10");
        JLabel titleLabel = new JLabel("TaskFlow");
        titleLabel.setFont(UIConstants.TITLE_FONT_SMALL);
        add(titleLabel, "gapright 15, aligny center");
        
        folderFilterBox = new JComboBox<>();
        folderFilterBox.addActionListener(e -> {
            String sel = (String) folderFilterBox.getSelectedItem();
            String folder = "All Folders".equals(sel) ? null : sel;
            listener.onFolderFilterChanged(folder);
        });
        add(folderFilterBox, "width 150!");
        
        JButton filterButton = new JButton(common.getFilterIcon());
        UIUtils.styleIconButton(filterButton, "Filter Tasks");
        createFilterPopup();
        filterButton.addActionListener(e -> filterPopupMenu.show(filterButton, 0, filterButton.getHeight()));
        add(filterButton, "gapright 15");

        JButton userButton = new JButton(common.getUserConfigIcon());
        UIUtils.styleIconButton(userButton, "User Profile");
        createUserPopup();
        userButton.addActionListener(e -> userPopupMenu.show(userButton, 0, userButton.getHeight()));
        add(userButton);

        toggleColorButton = new JButton(common.getModeIcon());
        UIUtils.styleIconButton(toggleColorButton, "Toggle color mode");
        toggleColorButton.addActionListener(e -> listener.onToggleTheme());
        add(toggleColorButton, "aligny center, gapleft 10");
    }

    private void createFilterPopup() {
        filterPopupMenu = new JPopupMenu();
        JMenuItem clear = new JMenuItem("Clear All Filters");
        filterPopupMenu.add(clear);
        filterPopupMenu.addSeparator();

        JMenu statusFilterMenu = new JMenu("Filter by status");
        for (TaskStatus status : Arrays.asList(TaskStatus.pending, TaskStatus.completed, TaskStatus.in_progress)) {
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(TaskStatus.getStatusToString(status));
            if(status == TaskStatus.pending || status == TaskStatus.in_progress)
                item.setSelected(true);
            item.addActionListener(e -> handleFilterItemClicked());
            filterCheckBoxMenuItems.put(status, item);
            statusFilterMenu.add(item);
        }
        filterPopupMenu.add(statusFilterMenu);
        filterPopupMenu.addSeparator();

        JMenu dateSortMenu = new JMenu("Sort by date");
        for (TaskStatus status : Arrays.asList(TaskStatus.overdue, TaskStatus.incoming_due, TaskStatus.newest)) {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(TaskStatus.getStatusToString(status));
            item.addActionListener(e -> handleSortItemClicked());
            sortRadioButtonMenuItems.put(status, item);
            sortButtonGroup.add(item);
            dateSortMenu.add(item);
        }
        filterPopupMenu.add(dateSortMenu);
        clear.addActionListener(e -> {
            selectedCriterias = Collections.emptySet();
            for (JCheckBoxMenuItem item : filterCheckBoxMenuItems.values())
                item.setSelected(true);
            sortButtonGroup.clearSelection();
            folderFilterBox.setSelectedIndex(0);
            listener.onClearFilters();
            });
    }

    private void handleFilterItemClicked() {
        selectedCriterias = new HashSet<>();
        for (Map.Entry<TaskStatus, JCheckBoxMenuItem> entry : filterCheckBoxMenuItems.entrySet())
            if (entry.getValue().isSelected()) selectedCriterias.add(entry.getKey());
        listener.onStatusFilterChanged(selectedCriterias);
    }

    private void handleSortItemClicked() {
        Set<TaskStatus> selected = new HashSet<>();
        for (Map.Entry<TaskStatus, JCheckBoxMenuItem> entry : filterCheckBoxMenuItems.entrySet())
            if (entry.getValue().isSelected()) selected.add(entry.getKey());
        for (Map.Entry<TaskStatus, JRadioButtonMenuItem> entry : sortRadioButtonMenuItems.entrySet()) {
            if (entry.getValue().isSelected()) {
                selected.add(entry.getKey());
                break;
            }
        }
        listener.onStatusFilterChanged(selected);
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
}