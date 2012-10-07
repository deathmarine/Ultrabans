package com.modcrafting.ultrabans.gui.listeners;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JList;

import com.modcrafting.ultrabans.gui.Frame;

public class MouseListListener implements MouseListener{
	JList list;
	Frame frame;
	public MouseListListener(JList l,Frame f){
		list=l;
		frame=f;
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getButton()==MouseEvent.BUTTON3){
			int index = list.locationToIndex(e.getPoint());
			list.setSelectedIndex(index);
			String playerName = (String) list.getSelectedValue();
			frame.console.append(playerName);
		}				
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}	

