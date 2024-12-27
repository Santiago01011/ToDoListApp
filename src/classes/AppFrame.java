// package classes;

// import javax.swing.JButton;
// import javax.swing.JCheckBox;
// import javax.swing.JFrame;
// import javax.swing.JLabel;
// import javax.swing.JPanel;
// import javax.swing.JScrollPane;
// import javax.swing.JTextArea;
// import javax.swing.JTextField;
// import javax.swing.BorderFactory;
// import javax.swing.BoxLayout;
// import javax.swing.ImageIcon;

// import java.awt.BorderLayout;
// //import java.awt.Color;
// import java.awt.Dimension;
// import java.awt.FlowLayout;
// import java.awt.GridLayout;
// import java.awt.event.ActionEvent;
// import java.awt.event.ActionListener;
// import java.util.ArrayList;
// import java.util.List;

// public class AppFrame extends JFrame {
//     private List<Task> tasks;
//     private List<Task> tasksToAdd;
//     private List<Task> tasksToUpdate;
//     private List<Task> tasksToDelete;
//     private JPanel centerPanel;
//     private JTextArea taskDetailsArea;
//     private int currentUserId = 1;

//     // Constructor
//     public AppFrame() {

//         // Set the app frame icon
//         ImageIcon appIcon = new ImageIcon("src/assets/app_icon.png");
//         setIconImage(appIcon.getImage());

//         // Initialize the task list
//         tasks = new ArrayList<>();
//         tasksToAdd = new ArrayList<>();
//         tasksToUpdate = new ArrayList<>();
//         tasksToDelete = new ArrayList<>();

//         // Set the title of the frame
//         setTitle("To Do List App");

//         // Set the default close operation
//         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

//         // Set the layout manager to BorderLayout
//         setLayout(new BorderLayout());

//         // Initialize UI components
//         initNorthPanel();
//         initSouthPanel();
//         initCenterPanel();

//         //load tasks from the database
//         loadTasksFromDatabase();
//         updateTaskList();
        

//         // Add window listener to save changes on close
//         addWindowListener(new java.awt.event.WindowAdapter() {
//             @Override
//             public void windowClosing(java.awt.event.WindowEvent windowEvent) {
//                 saveChangesToDatabase();
//             }
//         });

//         // Set the size of the frame
//         setSize(400, 750);

//         // Set the frame to be visible
//         setVisible(true);

//         System.out.println("AppFrame created");
//     }

//     // Method to initialize the north panel
//     private void initNorthPanel() {
//         // Create buttons
//         JButton addButton = new JButton("Add Task");
//         //set button color rgb(124, 185, 43)
//         addButton.setBackground(new java.awt.Color(124, 185, 43));
//         addButton.setPreferredSize(new Dimension(100, 20));
//         JTextField taskField = new JTextField(20);
//         JTextField taskDescriptionField = new JTextField(30);

//         // Add action listener to the add button
//         addButton.addActionListener(new ActionListener() {
//             @Override
//             public void actionPerformed(ActionEvent e) {
//                 String taskTitle = taskField.getText();
//                 String description = taskDescriptionField.getText();
//                 if (!taskTitle.isEmpty()) {
//                     addTask(taskTitle, description);
//                     taskField.setText("");
//                     taskDescriptionField.setText("");
//                 }
//             }
//         });
//          // Add action listener to the text fields to trigger the add button on Enter key press
//         taskField.addActionListener(new ActionListener() {
//             @Override
//             public void actionPerformed(ActionEvent e) {
//                 addButton.doClick();
//             }
//         });
//         taskDescriptionField.addActionListener(new ActionListener() {
//             @Override
//             public void actionPerformed(ActionEvent e) {
//                 addButton.doClick();
//             }
//         });

//         // Create a panel with GridLayout for the NORTH region
//         JPanel northPanel = new JPanel(new GridLayout(2, 1));

//         // Create sub-panels with FlowLayout for field and add button
//         JPanel taskFieldPanel = new JPanel(new FlowLayout());
//         taskFieldPanel.add(taskField);
//         taskFieldPanel.add(addButton);
//         JPanel taskDescriptionFieldPanel = new JPanel(new FlowLayout());
//         taskDescriptionFieldPanel.add(taskDescriptionField);

//         // Add the sub-panels to the north panel
//         northPanel.add(taskFieldPanel);
//         northPanel.add(taskDescriptionFieldPanel);    

//         // Add the north panel to the NORTH region
//         add(northPanel, BorderLayout.NORTH);
//     }

//     // Method to initialize the south panel
//     private void initSouthPanel() {
//         // Create buttons
//         JButton updateButton = new JButton("Update Tasks");
//         JButton historyButton = new JButton("View History");

//         // Create a panel with GridLayout for the SOUTH region
//         JPanel southPanel = new JPanel(new GridLayout(1, 2));

//         // Add action listener to the update button to clear the tasks done
//         updateButton.addActionListener(new ActionListener() {
//             @Override
//             public void actionPerformed(ActionEvent e) {
//                 tasks.removeIf(task -> task.getIsDone());
//                 saveChangesToDatabase();
//                 updateTaskList();
//             }
//         });

//         // Add action listener to the history button to view the history
//         historyButton.addActionListener(new ActionListener() {
//             @Override
//             public void actionPerformed(ActionEvent e) {
//                 saveChangesToDatabase();
//                 new HistoryFrame(currentUserId, AppFrame.this);
//             }
//         });


//         // Create sub-panels with FlowLayout for each button
//         JPanel updatePanel = new JPanel(new FlowLayout());
//         updatePanel.add(updateButton);
//         updatePanel.add(historyButton);


//         // Add the sub-panels to the south panel
//         southPanel.add(updatePanel);

//         // Add the south panel to the SOUTH region
//         add(southPanel, BorderLayout.SOUTH);
//     }

//     // Method to initialize the center panel
//     private void initCenterPanel() {
//         centerPanel = new JPanel();
//         centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
//         JScrollPane scrollPane = new JScrollPane(centerPanel);
//         // Set the background color of the center panel
//         //centerPanel.setBackground(new Color(112,128,144));
//         //  Initialize the task details area
//         taskDetailsArea = new JTextArea(10, 20);
//         taskDetailsArea.setEditable(false);
//         JScrollPane taskDetailsScrollPane = new JScrollPane(taskDetailsArea);

//         // Create a main panel with BorderLayout to hold both scroll panes
//         JPanel mainPanel = new JPanel(new BorderLayout());
//         mainPanel.add(scrollPane, BorderLayout.CENTER);
//         mainPanel.add(taskDetailsScrollPane, BorderLayout.SOUTH);

//         // Add the main panel to the CENTER region of the frame
//         add(mainPanel, BorderLayout.CENTER);
//     }

//     // Method to view the task details
//     private void viewTaskDetails(Task task) {
//         taskDetailsArea.setText(task.viewTaskDesc());
//     }

//     // Method to load tasks from the database
//     private void loadTasksFromDatabase(){
//         tasks = TaskDAO.loadTasksFromDatabase(currentUserId, false);
//         //updateTaskList();
//     }

//     public void saveChangesDB(){
//         saveChangesToDatabase();
//     }
//     // Method to save changes to the database
//     private void saveChangesToDatabase(){
//         for (Task task : tasksToAdd){
//             TaskDAO.saveTaskToDatabase(task);
//         }
//         for (Task task : tasksToUpdate){
//             TaskDAO.updateTaskInDatabase(task);
//         }
//         for (Task task : tasksToDelete){
//             TaskDAO.deleteTaskFromDatabase(task);
//         }
//         tasksToAdd.clear();
//         tasksToUpdate.clear();
//         tasksToDelete.clear();
//     }

//     // Method to add a task
//     private void addTask(String taskTitle, String description) {
//         Task task = new Task(tasks.size() + 1, taskTitle, description, 1);
//         tasks.add(task);
//         tasksToAdd.add(task);
//         saveChangesToDatabase();
//         updateTaskList();
//     }

//     // Method to update the task list
//     public void updateTaskList() {
//         centerPanel.removeAll();
//         loadTasksFromDatabase();
//         for (Task task : tasks) {
//             JPanel taskPanel = createTaskPanel(task);
//             centerPanel.add(taskPanel);
//         }
//         centerPanel.revalidate();
//         centerPanel.repaint();
//     }
    
//     private JPanel createTaskPanel(Task task) {
//         JPanel taskPanel = new JPanel(new BorderLayout());
//         taskPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
//         taskPanel.add(createCheckboxPanel(task), BorderLayout.WEST);
//         taskPanel.add(createTitlePanel(task), BorderLayout.CENTER);
//         taskPanel.add(createActionPanel(task), BorderLayout.EAST);
//         return taskPanel;
//     }
    
//     private JPanel createCheckboxPanel(Task task) {
//         JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//         JCheckBox updateCheckBox = new JCheckBox("", task.getIsDone());
//         updateCheckBox.setToolTipText("Mark as Done");
//         updateCheckBox.addActionListener(new ActionListener() {
//             @Override
//             public void actionPerformed(ActionEvent e) {
//                 task.setIsDone(!task.getIsDone());
//                 if (!tasksToUpdate.contains(task)) {
//                     tasksToUpdate.add(task);
//                 }
//                 saveChangesToDatabase();
//                 updateTaskList();
//             }
//         });
//         checkboxPanel.add(updateCheckBox);
//         return checkboxPanel;
//     }
    
//     private JPanel createTitlePanel(Task task) {
//         JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
//         JLabel taskLabel = new JLabel(task.getTaskTitle());
//         titlePanel.add(taskLabel);
//         return titlePanel;
//     }
    
//     private JPanel createActionPanel(Task task) {
//         JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    
//         ImageIcon viewIcon = new ImageIcon("src/assets/view.png");
//         ImageIcon deleteIcon = new ImageIcon("src/assets/delete.png");
//         viewIcon = new ImageIcon(viewIcon.getImage().getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH));
//         deleteIcon = new ImageIcon(deleteIcon.getImage().getScaledInstance(20, 20, java.awt.Image.SCALE_SMOOTH));
    
//         JButton viewButton = new JButton(viewIcon);
//         viewButton.setPreferredSize(new Dimension(20, 20));
//         viewButton.setBorderPainted(false);
//         viewButton.setContentAreaFilled(false);
//         viewButton.setToolTipText("View Task Details");
//         viewButton.addActionListener(new ActionListener() {
//             @Override
//             public void actionPerformed(ActionEvent e) {
//                 viewTaskDetails(task);
//             }
//         });
    
//         JButton deleteButton = new JButton(deleteIcon);
//         deleteButton.setPreferredSize(new Dimension(20, 20));
//         deleteButton.setBorderPainted(false);
//         deleteButton.setContentAreaFilled(false);
//         deleteButton.setToolTipText("Delete Task");
//         deleteButton.addActionListener(new ActionListener() {
//             @Override
//             public void actionPerformed(ActionEvent e) {
//                 if (tasksToAdd.contains(task)) {
//                     tasksToAdd.remove(task);
//                 } else {
//                     tasksToDelete.add(task);
//                 }
//                 tasks.remove(task);
//                 saveChangesToDatabase();
//                 updateTaskList();
//             }
//         });
    
//         actionPanel.add(viewButton);
//         actionPanel.add(deleteButton);
//         return actionPanel;
//     }

// }