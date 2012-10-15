package com.modcrafting.ultrabans.gui.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
				if(target!=null){
					if(action.equalsIgnoreCase("edit")) return;
					if(action.startsWith("U")||action.equalsIgnoreCase("ping")){
						frame.connection.sendtoServer(action+" "+target);
						return;
					}
					if(!action.startsWith("Temp")){
						String reason = frame.showInput("Reason", "Would you like to add a reason?");
						frame.connection.sendtoServer(action+" "+target+" "+reason);
					}else{
						JPanel p = new JPanel();
						p.add(new JLabel("For how long?"));
						JTextField amt = new JTextField(16);
						amt.setEditable(true);
						p.add(amt);
						String[] modes = {"secs","mins","hours","days","weeks","months"};
						JComboBox mode = new JComboBox(modes);
						mode.setEditable(true);
						p.add(mode);
						JOptionPane.showMessageDialog(null, p, "Temporary Action", JOptionPane.QUESTION_MESSAGE);
						String at = ((String)amt.getText());
						String md = ((String) mode.getSelectedItem());
						String reason = frame.showInput("Reason", "Would you like to add a reason?");
						if(at!=null&&md!=null)frame.connection.sendtoServer(action+" "+target+" "+at+" "+md+" "+reason);
					}
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		
	}

}
