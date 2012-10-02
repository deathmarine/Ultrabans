package com.modcrafting.ultrabans.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;

public class Splash extends JWindow implements Runnable{
	private static final long serialVersionUID = 4530329299844232491L;
	public Splash(Frame f){
		super(f);
		URL u = this.getClass().getResource("/UltrabansLogo.gif");
		JLabel l = new JLabel(new ImageIcon(u));
        getContentPane().add(l, BorderLayout.CENTER);
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension labelSize = l.getPreferredSize();
        setLocation(screenSize.width/2 - (labelSize.width/2),screenSize.height/2 - (labelSize.height/2));
        setVisible(true);
	}
	public void close(){
		this.setVisible(false);
		this.dispose();
	}
	@Override
	public synchronized void run() {
		try {
			this.wait(1500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		close();
	}
}
