package com.modcrafting.ultrabans.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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

import com.modcrafting.ultrabans.gui.listeners.Connection;
import com.modcrafting.ultrabans.gui.listeners.WinListener;

public class Frame{
	Splash splash;
	JFrame frame;
	int frameX;
	int frameY;
	JLabel statsBar;
	final UndoManager undo = new UndoManager();
	Connection connection;
	public Frame(){
		frame = new JFrame("Ultrabans Live");
		new Thread(new Splash(frame)).start();
		buildFrame();
		createMenu();
		//Do stuff
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
				if(connection!=null){
					showError("It appears you are already connected.");
					return;
				}else{
					connection= new Connection();
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
		statsBar.setPreferredSize(new Dimension(frame.getWidth(), 20));
		statsBar.setHorizontalAlignment(SwingConstants.RIGHT);
		p.add(statsBar);
		c.add(p);
	}
	private void inputText(Container c) {
		JPanel p = new JPanel();
		p.setPreferredSize(new Dimension(frame.getWidth(), 32));
		JTextField input = new JTextField(16);
        JButton button = new JButton("Send");
		input.setBackground(Color.white);
		input.setForeground(Color.black);
		input.setEditable(true);
		input.setVisible(true);
		input.setBorder(new BevelBorder(BevelBorder.LOWERED));
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
}
