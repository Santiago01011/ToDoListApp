package classes;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class HistoryFrame extends JFrame{
    private List<Task> tasksHistory;
    public HistoryFrame(int userId, AppFrame appFrame){
        setTitle("History");
        ImageIcon appIcon = new ImageIcon("src/assets/app_icon.png");
        setIconImage(appIcon.getImage());
        setSize(400, 750);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        //load all the tasks marked as done in the database
        tasksHistory = TaskDAO.loadTasksFromDatabase(userId, true);
        JPanel taskPanel = new JPanel();
        taskPanel.setLayout(new BoxLayout(taskPanel, BoxLayout.Y_AXIS));
        JScrollPane taskScrollPane = new JScrollPane(taskPanel);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                appFrame.saveChangesDB();
                appFrame.updateTaskList();
                appFrame.dispose();
                new AppFrame();
            }
        });
        
        for(Task task : tasksHistory){
            JLabel taskTitle = new JLabel(task.getTaskTitle());
            JCheckBox taskCheckBox = new JCheckBox("Restore", task.getIsDone());
            taskCheckBox.addActionListener(e -> {
                task.setIsDone(taskCheckBox.isSelected());
                TaskDAO.updateTaskInDatabase(task);
                taskPanel.remove(taskTitle);
                taskPanel.remove(taskCheckBox);
                taskPanel.revalidate();
                taskPanel.repaint();
            });
            taskPanel.add(taskTitle);
            taskPanel.add(taskCheckBox);
        }
        add(taskScrollPane, BorderLayout.CENTER);
        setVisible(true);
    }
    
}