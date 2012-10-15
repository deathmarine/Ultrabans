package com.modcrafting.ultrabans.live.gui.listeners;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import com.modcrafting.ultrabans.live.gui.Frame;

public class WinListener implements WindowListener {
	Frame frame;
	public WinListener(Frame in) {
		frame=in;
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		frame.die();

	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		frame.die();

	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

}
