package edu.gatech;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

public class TraceStarter {
	static final String NEW = "New";
	static final String OPEN = "Open";
	static final String SAVE = "Save";
	static final String SAVE_AS = "Save As...";
	static final String EXIT = "Exit";
	
	static final int TA_ROWS = 32;
	static final int TA_COLS = 40;
	
	static Trace trace;
	static JTextArea textArea;
	static MyCanvas canvas;
	static JButton prev;
	static JButton next;
	
	private static void createAndShowGUI()
	{
		//Create and set up the window.
        JFrame frame = new JFrame("TraceStarter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        Container contentPane = frame.getContentPane();
        contentPane.setLayout(new FlowLayout());
        
        trace = new Trace("res/Test3.java");
        //System.out.println(trace.getJSON());
        
        textArea = new JTextArea(TA_ROWS, TA_COLS);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setText(trace.rawCode);
        
        Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
        try {
			textArea.getHighlighter().addHighlight(textArea.getLineStartOffset(0), textArea.getLineEndOffset(0), painter);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
        
        canvas = new MyCanvas(trace);
        
        MyActionListener listener = new MyActionListener(textArea, canvas);
        prev = new JButton("Prev");
        prev.addActionListener(listener);
        next = new JButton("Next");
        next.addActionListener(listener);
        
        // Menu bar
        MyMenuActionListener al = new MyMenuActionListener(textArea, canvas); 
        
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        JMenuItem newFile = new JMenuItem(NEW, KeyEvent.VK_N);
        newFile.addActionListener(al);
        JMenuItem openFile = new JMenuItem(OPEN, KeyEvent.VK_O);
        openFile.addActionListener(al);
        JMenuItem saveFile = new JMenuItem(SAVE, KeyEvent.VK_O);
        saveFile.addActionListener(al);
        JMenuItem saveFileAs = new JMenuItem(SAVE_AS, KeyEvent.VK_O);
        saveFileAs.addActionListener(al);
        JMenuItem exit = new JMenuItem(EXIT, KeyEvent.VK_O);
        exit.addActionListener(al);
        fileMenu.add(newFile);
        fileMenu.add(openFile);
        fileMenu.add(saveFile);
        fileMenu.add(saveFileAs);
        fileMenu.add(exit);
        menuBar.add(fileMenu);
        
        frame.add(textArea);
        frame.add(canvas);
        frame.add(prev);
        frame.add(next);
        frame.setJMenuBar(menuBar);
        
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

class MyMenuActionListener implements ActionListener {

	private JTextArea textArea;
	private MyCanvas canvas;
	private String currentPath;
	private boolean hasOpened;
	private File currentFile;
	
	public MyMenuActionListener(JTextArea textArea, MyCanvas canvas)
	{
		this.textArea = textArea;
		this.canvas = canvas;
		currentPath = System.getProperty("user.dir");
		hasOpened = false;
		currentFile = null;
	}
	
	public void saveFileAs(File file)
	{
		try {
			PrintWriter out = new PrintWriter(file);
			out.println(textArea.getText());
			out.close();
			TraceStarter.trace = new Trace(file.getAbsolutePath());
			canvas.setTrace(TraceStarter.trace);
			TraceStarter.prev.setEnabled(true);
			TraceStarter.next.setEnabled(true);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// System.out.println(arg0.getActionCommand());
		String cmd = arg0.getActionCommand();
		
		if (cmd.equals(TraceStarter.NEW))
		{
			textArea.setText("");
			canvas.setTrace(null);
			TraceStarter.prev.setEnabled(false);
			TraceStarter.next.setEnabled(false);
			currentFile = null;
		}
		else if (cmd.equals(TraceStarter.OPEN))
		{
			JFileChooser fc = new JFileChooser(currentPath);
			int result = fc.showOpenDialog(TraceStarter.canvas);
			// System.out.printf("%d, %s\n", result, fc.getSelectedFile());
			if (result == JFileChooser.APPROVE_OPTION)
			{
				currentPath = fc.getSelectedFile().getAbsolutePath();
				TraceStarter.trace = new Trace(currentPath);
				canvas.setTrace(TraceStarter.trace);
				textArea.setText(TraceStarter.trace.rawCode);
				Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
		        try {
					textArea.getHighlighter().addHighlight(textArea.getLineStartOffset(0), textArea.getLineEndOffset(0), painter);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
		        hasOpened = true;
		        currentFile = fc.getSelectedFile();
		        TraceStarter.prev.setEnabled(true);
		        TraceStarter.next.setEnabled(true);
			}
		}
		else if (cmd.equals(TraceStarter.SAVE))
		{
			if (currentFile != null)
			{
				saveFileAs(currentFile);
			}
			else
			{
				JFileChooser fc = new JFileChooser(currentPath);
				int result = fc.showSaveDialog(TraceStarter.canvas);
				if (result == JFileChooser.APPROVE_OPTION)
				{
					currentFile = fc.getSelectedFile();
					saveFileAs(currentFile);
					hasOpened = true;
					currentPath = currentFile.getAbsolutePath();
				}
			}
		}
		else if (cmd.equals(TraceStarter.SAVE_AS))
		{
			JFileChooser fc = new JFileChooser(currentPath);
			int result = fc.showSaveDialog(TraceStarter.canvas);
			if (result == JFileChooser.APPROVE_OPTION)
			{
				currentFile = fc.getSelectedFile();
				saveFileAs(currentFile);
				hasOpened = true;
				currentPath = currentFile.getAbsolutePath();
			}
		}
		else if (cmd.equals(TraceStarter.EXIT))
		{
			System.exit(0);
		}
	}
	
}

class MyActionListener implements ActionListener {
	JTextArea textArea;
	MyCanvas canvas;
	Highlighter.HighlightPainter painter;
	
	public MyActionListener(JTextArea textArea, MyCanvas canvas)
	{
		this.textArea = textArea;
		this.canvas = canvas;
		this.painter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		String action = arg0.getActionCommand();
		if (action.equals("Next"))
		{
			canvas.nextSnapshot();
		}
		else
		{
			canvas.prevSnapshot();
		}
		canvas.repaint();
		
		textArea.getHighlighter().removeAllHighlights();
		int lineNumber = canvas.getCurrentSnapshot().pc - 1;
		String code = canvas.getCurrentSnapshot().code;
		try {
			int startIndex = textArea.getLineStartOffset(lineNumber);
			int endIndex = textArea.getLineEndOffset(lineNumber);
			// System.out.printf("line start: %d, end: %d\n", startIndex, endIndex);
			String line = textArea.getText(startIndex, endIndex-startIndex);
			
			if (code.length() < line.length())
			{
				startIndex += line.indexOf(code);
				endIndex = startIndex + code.length();
			}
			
			textArea.getHighlighter().addHighlight(startIndex, endIndex, painter);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
}

@SuppressWarnings("serial")
class MyCanvas extends JPanel {
	public static final int WIDTH = 500;
	public static final int HEIGHT = 482;
	public static final int BASE_Y = 90;
	
	public int currentSnapshot;
	public ArrayList<Snapshot> snaps;
	
	private int[] columnX = {20, 220};
	private int[] columnY = {BASE_Y, BASE_Y};
	
	private HashMap<Integer, Integer> hashCodeConnections;
	
	ArrayList<Paintable> objects;
	
	public MyCanvas(Trace trace) {
        setBorder(BorderFactory.createLineBorder(Color.black));
        setTrace(trace);
        
//        objects = new ArrayList<Paintable>();
//        MyTextBox box1 = new MyTextBox("Hello, World. I'm Tao!", 20, 30);
//        MyTextBox box2 = new MyTextBox("Foo bar baz qux", 400, 300);
//        MyArrow arrow1 = new MyArrow(box1, box2);
//        objects.add(box1);
//        objects.add(box2);
//        objects.add(arrow1);

    }
	
	public void setTrace(Trace trace)
	{
		currentSnapshot = 0;
		objects = new ArrayList<Paintable>();
		hashCodeConnections = new HashMap<Integer, Integer>();
		if (trace != null)
		{
			snaps = trace.getSnapshots();
			addObjectsToPaint();
		}
		else
			snaps = new ArrayList<Snapshot>();
	}
	
	public Snapshot getCurrentSnapshot()
	{
		if (snaps.size() == 0)
			return null;
		
		return snaps.get(currentSnapshot);
	}
	
	public void addObjectsToPaint()
	{
		reset();
		
		if (snaps.size() == 0)
			return;
		
		Snapshot state = snaps.get(currentSnapshot);
		for (Ref r : state.state)
		{
			String description = r.type + " " + r.name;
			if (r.type.equals("int") || r.type.equals("double") || r.type.equals("char") || r.type.equals("boolean"))
			{
				description += " = " + r.contents;
				addTextBox(description, 0);
			}
			else
			{
				addTextBox(description, 0);
				
				if (hashCodeConnections.containsKey(r.hashCode))
				{
					int from = objects.size() - 1;
					int to = hashCodeConnections.get(r.hashCode);
					addArrow(from, to);
				}
				else
				{
					addTextBox(r.contents, 1);
					hashCodeConnections.put(r.hashCode, objects.size()-1);
					addArrow(objects.size()-2, objects.size()-1);
				}
			}
		}
	}
	
	public void reset()
	{
		objects.clear();
		hashCodeConnections.clear();
		columnY[0] = BASE_Y;
		columnY[1] = BASE_Y;
	}
	
	public void addTextBox(String s, int col)
	{
		MyTextBox textBox = new MyTextBox(s, columnX[col], columnY[col]);
		objects.add(textBox);
		columnY[col] += 30;
	}
	
	public void addArrow(int from, int to)
	{
		//System.err.printf("from: %d, to: %d\n", from, to);
		MyTextBox left = (MyTextBox) objects.get(from);
		MyTextBox right = (MyTextBox) objects.get(to);
		objects.add(new MyArrow(left, right));
	}
	
	public void nextSnapshot()
	{
		if (currentSnapshot < snaps.size()-1)
			currentSnapshot++;
		addObjectsToPaint();
	}
	
	public void prevSnapshot()
	{
		if (currentSnapshot > 0)
			currentSnapshot--;
		addObjectsToPaint();
	}

    public Dimension getPreferredSize() {
        return new Dimension(WIDTH, HEIGHT);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (snaps.size() == 0) return;
        
        Snapshot state = snaps.get(currentSnapshot);
        g.drawString("code: " + state.code, 10, 30);
        g.drawString("line: " + state.pc, 10, 50);
        g.drawString("step: " + state.step, 10, 70);
        
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
		this.width = 10 * text.length();
		this.height = 20;
	}
	
	public void paint(Graphics g)
	{
		g.setColor(Color.WHITE);
		g.fillRect(x, y, width, height);
		g.setColor(Color.BLACK);
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
