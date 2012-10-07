package com.modcrafting.ultrabans.live;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;

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
			frame.statsBar.setText("Disconnected");
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
		    textArea.setText("Connected");
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
	public void writeConsole(String input){		
		frame.console.append(input+"\n");
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