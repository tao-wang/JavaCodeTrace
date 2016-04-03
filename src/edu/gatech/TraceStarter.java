package edu.gatech;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class TraceStarter {
	
	private static void createAndShowGUI()
	{
		//Create and set up the window.
        JFrame frame = new JFrame("TraceStarter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        frame.add(new MyCanvas("res/Test1.java"));
        frame.pack();
        frame.setLocationRelativeTo(null);
 
        //Display the window.
        frame.setVisible(true);
	}
	
	public static void main(String[] args)
	{
//		Trace tr = new Trace("res/Test1.java");
//		Snapshot step16 = tr.getSnapshots().get(15);
//		System.out.println("code: " + step16.code);
//		System.out.println("pc: " + step16.pc);
//		System.out.println("step: " + step16.step);
//		for (Ref r : step16.state)
//		{
//			System.out.println(r.name);
//			System.out.println("\t" + r.type);
//			System.out.println("\t" + r.hashCode);
//			System.out.println("\t" + r.contents);
//		}
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
	}
}

@SuppressWarnings("serial")
class MyCanvas extends JPanel {
	public static final int WIDTH = 800;
	public static final int HEIGHT = 600;
	
	private int currentSnapshot;
	private ArrayList<Snapshot> snaps;
	
	ArrayList<Paintable> objects;
	
	public MyCanvas(String file) {
        setBorder(BorderFactory.createLineBorder(Color.black));
        currentSnapshot = 0;
        Trace tr = new Trace(file);
        snaps = tr.getSnapshots();
        
//        objects = new ArrayList<Paintable>();
//        MyTextBox box1 = new MyTextBox("Hello, World. I'm Tao!", 20, 30);
//        MyTextBox box2 = new MyTextBox("Foo bar baz qux", 400, 300);
//        MyArrow arrow1 = new MyArrow(box1, box2);
//        objects.add(box1);
//        objects.add(box2);
//        objects.add(arrow1);
    }
	
	public void addObjectsToPaint()
	{
		
	}
	
	public void nextSnapshot()
	{
		if (currentSnapshot < snaps.size())
			currentSnapshot++;
	}
	
	public void prevSnapshot()
	{
		if (currentSnapshot > 0)
			currentSnapshot--;
	}

    public Dimension getPreferredSize() {
        return new Dimension(WIDTH, HEIGHT);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);       

        // Draw Text
        for (Paintable p : objects)
        {
        	p.paint(g);
        }
    }  
}

class MyTextBox implements Paintable {
	
	public int x;
	public int y;
	public int width;
	public int height;
	public String text;
	
	public MyTextBox(String text, int x, int y)
	{
		this.text = text;
		this.x = x;
		this.y = y;
		this.width = 8 * text.length();
		this.height = 20;
	}
	
	public void paint(Graphics g)
	{
		g.drawRect(x, y, width, height);
		g.drawString(text, x+4, y+15);
	}
	
}

class MyArrow implements Paintable {

	private int startX;
	private int startY;
	private int endX;
	private int endY;
	
	public MyArrow(int x1, int y1, int x2, int y2)
	{
		startX = x1;
		startY = y1;
		endX = x2;
		endY = y2;
	}
	
	public MyArrow(MyTextBox one, MyTextBox two)
	{
		MyTextBox left = one;
		MyTextBox right = two;
		
		if (one.x > two.x)
		{
			left = two;
			right = one;
		}
		
		startX = left.x + left.width;
		startY = left.y + left.height/2;
		endX = right.x;
		endY = right.y + right.height/2;
	}
	
	@Override
	public void paint(Graphics g) {
		g.drawLine(startX, startY, endX, endY);
	}
	
}
