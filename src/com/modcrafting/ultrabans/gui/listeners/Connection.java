package com.modcrafting.ultrabans.gui.listeners;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.Thread.State;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JLabel;
import com.modcrafting.ultrabans.gui.Frame;

public class Connection {
	Socket sock;
	Frame frame;
	BufferedReader in;
	PrintWriter out;
	ClientWorker cw;
	public Connection(String ip, Frame f, int port){
		frame=f;
		try {
			InetAddress address = InetAddress.getByName(ip);
			sock=new Socket(address, port);
			cw =new ClientWorker(sock,frame.statsBar);
			cw.start();
		} catch (UnknownHostException e) {
			frame.showError("Unable to find Host");
		} catch (IOException e) {
			frame.showError("Unable to open connection");
		}
	}
	
	public void disconnect() {
		try {
			if(cw!=null)cw.alive=false;
			if(!cw.getState().equals(State.TERMINATED)||cw.isAlive()){
				cw.interrupt();
			}
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	class ClientWorker extends Thread implements Runnable {
		  private Socket client;
		  private JLabel textArea;
		  public String input;
		  boolean alive;
		  ClientWorker(Socket client, JLabel statsBar) {
		    this.client = client;
		    this.textArea = statsBar;
		  }
		  public void run(){
		    BufferedReader in = null;
		    PrintWriter out = null;
		    try{
		      in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		      out = new PrintWriter(client.getOutputStream(), true);
		    } catch (IOException e) {
		    	
		    }
		    alive=true;
		    while(alive){
		      try{
		    	  input = in.readLine();
		        if(input!=null){
		        	//incoming from server
		        	input=null;
		        }
		        out.println("");
		       }catch (IOException e) {
					frame.showError("Unable to open connection");
					alive=false;
		       }
		    }
		  }
		}
}
