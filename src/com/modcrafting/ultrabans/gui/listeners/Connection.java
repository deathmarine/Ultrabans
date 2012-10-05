package com.modcrafting.ultrabans.gui.listeners;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JLabel;
import com.modcrafting.ultrabans.gui.Frame;

public class Connection {
	Socket sock;
	Frame frame;
	BufferedReader in;
	public PrintWriter out;
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
		    try{
		      in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		      out = new PrintWriter(client.getOutputStream(), true);
		    } catch (IOException e) {
		    	
		    }
		    alive=true;
		    textArea.setText("Connected");
		    out.println("pl");
			out.flush();
		    while(alive){
		      try{
		    	  input = in.readLine();
		        if(input!=null){
		        	if(input.startsWith("pl")){
		        		updatePlayers(input);
		        	}else{
				        frame.console.append(input+"\n");		        		
		        	}
		        }
		       }catch (IOException e) {
					disconnect();
		       }
		    }
		  }
		}
	public void sendtoServer(String command){
		if(alive){
			out.println(command);
			out.flush();
		}else{
			frame.showError("Client is not connected.");
		}
	}
	public void updatePlayers(String input){
		for(String s:input.substring(2).split(" ")){
			frame.playerlist.append(s+"\n");
		}
	}
}