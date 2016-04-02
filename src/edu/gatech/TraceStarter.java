package edu.gatech;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class TraceStarter {
	
	private static void createAndShowGUI()
	{
		//Create and set up the window.
        JFrame frame = new JFrame("TraceStarter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        frame.add(new MyPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
 
        //Display the window.
        frame.setVisible(true);
	}
	
	public static void main(String[] args)
	{
//		Trace tr = new Trace("res/Test1.java");
//		System.out.println(tr.getJSON());
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
	}
}

@SuppressWarnings("serial")
class MyPanel extends JPanel {
	public static final int WIDTH = 800;
	public static final int HEIGHT = 600;
	
	public MyPanel() {
        setBorder(BorderFactory.createLineBorder(Color.black));
    }

    public Dimension getPreferredSize() {
        return new Dimension(WIDTH, HEIGHT);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);       

        // Draw Text
        g.drawString("This is my custom Panel!",10,20);
    }  
}
