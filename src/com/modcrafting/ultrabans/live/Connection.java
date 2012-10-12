package com.modcrafting.ultrabans.live;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JLabel;
import javax.swing.text.BadLocationException;

import com.modcrafting.ultrabans.gui.Frame;

public class Connection {
	Socket sock;
	Frame frame;
	InputStream in;
	public OutputStream out;
	ClientWorker cw;
	boolean alive;
	Thread cwc;
	public Connection(String ip, Frame f, int port){
		frame=f;
		try {
			InetAddress address = InetAddress.getByName(ip);
			sock=new Socket(address, port);
			sock.setKeepAlive(true);
			f.sock=sock;
			cw =new ClientWorker(sock,frame.statsBar);
			cwc=new Thread(cw);
			cwc.start();
		} catch (UnknownHostException e) {
			frame.showError("Unable to find Host");
		} catch (IOException e) {
			frame.showError("Unable to open connection");
		}
	}
	
	public void disconnect() {
		try {
			if(cw!=null)alive=false;
			sock.close();
			frame.statsBar.setText("Disconnected  ");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	class ClientWorker implements Runnable {
		  private Socket client;
		  private JLabel textArea;
		  public String input;
		  ClientWorker(Socket client, JLabel statsBar) {
		    this.client = client;
		    this.textArea = statsBar;
		  }
		  public void run(){
		    try {
				in = client.getInputStream();
			    out = client.getOutputStream();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		    alive=true;
		    textArea.setText("Connected  ");
		    try {
		    	getoPlayers();
		    	getbPlayers();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		    while(alive){
		    	try{
					byte[] block = new byte[256];
					in.read(block, 0, block.length);
					block = frame.crypto.decrypt(block);
					input = new String(block);
					if(input!=null){
						if(input.contains(".console."))writeConsole(input.replaceAll(".console.", ""));
						if(input.contains(".oplayers."))updateOPlayers(input.replaceAll(".oplayers.", ""));
						if(input.contains(".bplayers."))updateBPlayers(input.replaceAll(".bplayers.", ""));
					}
		    	}catch (IOException e) {
		    		disconnect();
		    	}catch (Exception e) {
				e.printStackTrace();
				}
		    }
		  }
		}
	public void sendtoServer(String command) throws Exception{
		if(alive){
			out.write(frame.crypto.encrypt((".sendCommand."+command).getBytes()));
		}else{
			frame.showError("Client is not connected.");
		}
	}
	//Thanks Wolverness
	private Pattern pat = Pattern.compile("(^|(\\x1B\\[(\\d*)(;(\\d*))?m))(.*?)(?=(\\x1B\\[(\\d{0,2})(;(\\d{0,2}))?m)|($))", Pattern.DOTALL);
	private void writeConsole(String input){
		Matcher m = pat.matcher(input);
		while (m.find()) {
			final String content = m.group(11) != null ? m.group(6) + "\n" : m.group(6);
			frame.console.append(content);
		}
	}
	public void updateOPlayers(String input){
		frame.playerlist.setListData(input.split(" "));
	}
	public void updateBPlayers(String input){
		frame.actionlist.setListData(input.split(" "));
	}
	public void getoPlayers() throws Exception{
		out.write(frame.crypto.encrypt((".getPlayers.").getBytes()));
	}
	public void getbPlayers() throws Exception{
		out.write(frame.crypto.encrypt((".bannedPlayers.").getBytes()));
	}
}