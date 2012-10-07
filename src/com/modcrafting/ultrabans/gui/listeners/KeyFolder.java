package com.modcrafting.ultrabans.gui.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFileChooser;
import com.modcrafting.ultrabans.gui.Frame;
import com.modcrafting.ultrabans.security.RSAServerCrypto;

public class KeyFolder implements ActionListener{
	Frame frame;
	JFileChooser fc;
	public KeyFolder(Frame f){
		frame=f;
		fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
        int returnVal = fc.showOpenDialog(frame.frame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            frame.crypto = new RSAServerCrypto(fc.getSelectedFile());
        }
	}

}
