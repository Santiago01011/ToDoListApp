package UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SlidingPanel extends JPanel {
    private Timer timer;
    private int slideSpeed = 5;
    private boolean isVisible = false;

    public SlidingPanel(int width, int height) {
        setSize(width, height);
        setLayout(new BorderLayout());
        setBackground(Color.LIGHT_GRAY);
    }

    public void slideIn() {
        if (isVisible) return;
        isVisible = true;
        setLocation(-getWidth(), 0);
        timer = new Timer(1, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setLocation(getX() + slideSpeed, 0);
                if (getX() >= 0) { // Fully visible
                    setLocation(0, 0);
                    ((Timer) e.getSource()).stop();
                }
            }
        });
        timer.start();
    }
    
    public void slideOut() {
        if (!isVisible) return;
        isVisible = false;
        timer = new Timer(1, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setLocation(getX() - slideSpeed, 0);
                if (getX() + getWidth() <= 0) { // Fully hidden
                    setLocation(-getWidth(), 0);
                    ((Timer) e.getSource()).stop();
                }
            }
        });
        timer.start();
    }



public static void main(String[] args) {
    JFrame frame = new JFrame("Sliding Panel");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(300, 600);
    frame.setLocationRelativeTo(null);

    SlidingPanel panel = new SlidingPanel(200, 200);
    frame.add(panel);

    JButton slideInButton = new JButton("Slide In");
    slideInButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            panel.slideIn();
        }
    });

    JButton slideOutButton = new JButton("Slide Out");
    slideOutButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            panel.slideOut();
        }
    });

    JPanel buttonPanel = new JPanel();
    buttonPanel.add(slideInButton);
    buttonPanel.add(slideOutButton);

    frame.add(buttonPanel, BorderLayout.SOUTH);
    frame.setVisible(true);
}
}