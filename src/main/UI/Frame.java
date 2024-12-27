package main.UI;

import main.COMMON.common;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class Frame extends JFrame{
    //constructor
    public Frame(String title){
        super(title);
        setSize(400, 750);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ImageIcon appIcon = new ImageIcon("src/main/assets/app_icon.png");
        setIconImage(appIcon.getImage());
        getContentPane().setBackground(common.PRIMARY_COLOR);
    }
}