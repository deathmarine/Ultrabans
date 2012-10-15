package com.modcrafting.ultrabans.live.gui.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;

import com.modcrafting.ultrabans.live.gui.Frame;
import com.modcrafting.ultrabans.live.security.RSAServerCrypto;

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
        	boolean pub=false;
        	boolean priv=false;
        	File dir = fc.getSelectedFile();
        	for(File file :dir.listFiles()){
        		if(file.getName().equalsIgnoreCase("public.key"))pub=true;
        		if(file.getName().equalsIgnoreCase("private.key"))priv=true;
        	}
            if(pub&&priv){
            	frame.crypto = new RSAServerCrypto(dir);
            }else{
            	frame.showError("Keys not found.");
            }
        }
	}

}
