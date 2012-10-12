package com.modcrafting.ultrabans.gui.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import com.modcrafting.ultrabans.gui.Frame;

public class PopupListener implements ActionListener{
	Frame frame;
	String target;
	public PopupListener(Frame f, String p) {
		frame=f;
		target=p;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
        JMenuItem source = (JMenuItem)(e.getSource());
        String action = source.getText();
        if(frame.connection!=null){
			try {
				frame.connection.sendtoServer(action+" "+target);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		
	}

}
