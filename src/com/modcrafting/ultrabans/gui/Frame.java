package com.modcrafting.ultrabans.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.Socket;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import com.modcrafting.ultrabans.gui.listeners.KeyFolder;
import com.modcrafting.ultrabans.gui.listeners.MouseListListener;
import com.modcrafting.ultrabans.gui.listeners.WinListener;
import com.modcrafting.ultrabans.live.Connection;
import com.modcrafting.ultrabans.security.RSAServerCrypto;

public class Frame{
	Splash splash;
	public JFrame frame;
	int frameX;
	int frameY;
	public JLabel statsBar;
	public JList playerlist;
	public JList actionlist;
	public JTextArea console;
	JTextField input;
	final UndoManager undo = new UndoManager();
	Connection connection;
	public RSAServerCrypto crypto;
	public Socket sock;
	public Frame(){
		frame = new JFrame("Ultrabans Live");
		new Thread(new Splash(frame)).start();
		buildFrame();
		createMenu();
		visiblity(true);
	}

	public void visiblity(boolean b){
		frame.setVisible(b);
	}
	private void buildFrame(){
		Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize=new Dimension((int)(screenSize.width/2),(int)(screenSize.height/2));
		frameX=(int)(frameSize.width/2);
		frameY=(int)(frameSize.height/2);
		frame.setBounds(frameX,frameY,frameSize.width,frameSize.height);
		JPanel lower = new JPanel();
		lower.setLayout(new BoxLayout(lower, BoxLayout.Y_AXIS));
		lower.setPreferredSize(new Dimension(frame.getWidth(), 64));
		inputText(lower);
		statusBar(lower);
		mainArea();
		frame.getContentPane().add(lower, BorderLayout.SOUTH);
		frame.addWindowListener(new WinListener(this));
	}
	private void createMenu(){
		JMenuBar mbar = new JMenuBar();
		JMenu menu = new JMenu("File");
		JMenuItem menuItem = new JMenuItem("Connect");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Allows you to connect to Ultrabans Live.");
		menuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(sock!=null&&!sock.isClosed()){
					showError("It appears you are already connected.");
					return;
				}else{
					String ip = showInput("Connect","Type the ip of the server you would like to connect to. \nExample: xxx.xxx.xx.xxx:port ");
					if(ip!=null){
						String[] array = ip.split(":");
						int port;
						try{
							port = Integer.parseInt(array[1]);							
						}catch(NumberFormatException nfe){
							port = 123456;
						}
						connection= new Connection(array[0],getFrameClass(),port);						
					}
				}
			}
		});
		menu.add(menuItem);menuItem = new JMenuItem("Select KeyFolder...");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Sets the crypto keys used for the program.");
		menuItem.addActionListener(new KeyFolder(this));
		menu.add(menuItem);
		menuItem = new JMenuItem("Disconnect");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Allows you to disconnect from Ultrabans Live.");
		menuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(sock!=null&&!sock.isClosed()){
					connection.disconnect();
				}else{
					showError("It appears you are not connected.");
					return;
				}
			}
		});
		menu.add(menuItem);
		menuItem = new JMenuItem("Exit");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Closes Ultrabans Live.");
		menuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				die();
			}
		});
		menu.add(menuItem);
        mbar.add(menu);

		menu = new JMenu("Edit");
		menu.setMnemonic(KeyEvent.VK_E); 
        menuItem = new JMenuItem("Undo");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
		menuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
	            try {
	                if (undo.canUndo()) {
	                    undo.undo();
	                }
	            } catch (CannotUndoException e) {
	            }
			}
		});
        menu.add(menuItem);
        menuItem = new JMenuItem("Redo");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
		menuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
	            try {
	                if (undo.canRedo()) {
	                    undo.redo();
	                }
	            } catch (CannotUndoException e) {
	            }
			}
		});
        menu.add(menuItem);
        menu.addSeparator();
        menuItem = new JMenuItem(new DefaultEditorKit.CutAction());
        menuItem.setText("Cut");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
        menu.add(menuItem);
        menuItem = new JMenuItem(new DefaultEditorKit.CopyAction());
        menuItem.setText("Copy");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        menu.add(menuItem);
        menuItem = new JMenuItem(new DefaultEditorKit.PasteAction());
        menuItem.setText("Paste");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        menu.add(menuItem);
        mbar.add(menu);
        frame.setJMenuBar(mbar);
	}
	private void statusBar(Container c){
		JPanel p = new JPanel();
		p.setPreferredSize(new Dimension(frame.getWidth(), 18));
		statsBar = new JLabel();
		statsBar.setBorder(new BevelBorder(BevelBorder.LOWERED));
		statsBar.setPreferredSize(new Dimension(frame.getWidth()/2, 20));
		statsBar.setHorizontalAlignment(SwingConstants.RIGHT);
		statsBar.setText("Disconnected");
		p.add(statsBar);
		c.add(p);
	}
	private void inputText(Container c) {
		JPanel p = new JPanel();
		p.setPreferredSize(new Dimension(frame.getWidth(), 32));
		input = new JTextField(16);
        JButton button = new JButton("Send");
		input.setBackground(Color.white);
		input.setForeground(Color.black);
		input.setEditable(true);
		input.setVisible(true);
		input.setBorder(new BevelBorder(BevelBorder.LOWERED));
		button.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
					if(connection!=null)
						try {
							connection.sendtoServer(input.getText());
						} catch (Exception e) {
							e.printStackTrace();
						}
					input.setText("");
			}
		});
	    frame.getRootPane().setDefaultButton(button);
		p.add(input);
        p.add(button);
		c.add(p);
		Document doc = input.getDocument();
		doc.addUndoableEditListener(new UndoableEditListener() {
		    public void undoableEditHappened(UndoableEditEvent evt) {
		        undo.addEdit(evt.getEdit());
		    }
		});
		
	}
	private void mainArea(){
		JPanel p = new JPanel();
		playerlist = new JList();
		playerlist.addMouseListener(new MouseListListener(playerlist,this));
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setBorder(BorderFactory.createTitledBorder("Online Players"));
		p.add(new JScrollPane(playerlist));
		
		JPanel p2 = new JPanel();
		actionlist = new JList();
		actionlist.addMouseListener(new MouseListListener(actionlist,this));
		p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
		p2.setBorder(BorderFactory.createTitledBorder("Action Players"));
		p2.add(new JScrollPane(actionlist));
		
		JSplitPane r = new JSplitPane(JSplitPane.VERTICAL_SPLIT,p,p2);
		r.setBorder(new BevelBorder(BevelBorder.LOWERED));
		r.setDividerSize(10);
        r.setOneTouchExpandable(true);
        r.setResizeWeight(0.5);
		
		JPanel p3 = new JPanel();
		console = new JTextArea();
		console.setEditable(false);
		p3.setLayout(new BoxLayout(p3, BoxLayout.Y_AXIS));
		p3.setBorder(BorderFactory.createTitledBorder("Console"));
		p3.add(new JScrollPane(console));
		
		JSplitPane sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,p3,r);
		sp.setBorder(new BevelBorder(BevelBorder.LOWERED));
		sp.setDividerSize(10);
        sp.setOneTouchExpandable(true);
        sp.setResizeWeight(0.5);
        sp.setDividerLocation(450);
		frame.getContentPane().add(sp,BorderLayout.CENTER);
	}
	public void showError(String message){
		JOptionPane.showMessageDialog(null, message, "Error", 1);
	}
	public String showInput(String title, String message){
		return JOptionPane.showInputDialog(null,message, title,1);
	}
	public void die(){
		if(connection!=null){
			connection.disconnect();
		}
		visiblity(false);
		frame.dispose();
		System.exit(0);		
	}
	public Frame getFrameClass(){
		return this;
	}
}
