package com.modcrafting.ultrabans.gui.listeners;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.DefaultEditorKit;

public class RightClickEditor implements MouseListener{
		@Override
		public void mouseClicked(MouseEvent e) {
			if(e.getButton()==MouseEvent.BUTTON3){
				JPopupMenu popup = new JPopupMenu();
				JMenuItem menuItem = new JMenuItem(new DefaultEditorKit.CutAction());
		        menuItem.setText("Cut");
				popup.add(menuItem);
		        menuItem = new JMenuItem(new DefaultEditorKit.CopyAction());
		        menuItem.setText("Copy");
				popup.add(menuItem);
		        menuItem = new JMenuItem(new DefaultEditorKit.PasteAction());
		        menuItem.setText("Paste");
				popup.add(menuItem);
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
