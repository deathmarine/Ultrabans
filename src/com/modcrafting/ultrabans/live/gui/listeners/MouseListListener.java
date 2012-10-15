package com.modcrafting.ultrabans.live.gui.listeners;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.modcrafting.ultrabans.live.gui.Frame;

public class MouseListListener implements MouseListener{
	JList list;
	Frame frame;
	String[] actions;
	public MouseListListener(JList l,Frame f, String[] a){
		list=l;
		frame=f;
		actions=a;
	}
	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getButton()==MouseEvent.BUTTON3){
			int index = list.locationToIndex(e.getPoint());
			list.setSelectedIndex(index);
			String playerName = (String) list.getSelectedValue();
			if(playerName==null) return;
			JPopupMenu popup = new JPopupMenu();
			for(String ac:actions){
		        JMenuItem menuItem = new JMenuItem(ac);
		        menuItem.addActionListener(new PopupListener(frame,playerName));
		        popup.add(menuItem);
			}
            popup.show(e.getComponent(), e.getX(), e.getY());
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

