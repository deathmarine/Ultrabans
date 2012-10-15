package com.modcrafting.ultrabans.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.modcrafting.ultrabans.Ultrabans;

public class UBServer implements Runnable{
	ServerSocket server;
	boolean alive;
	int port;
	Ultrabans plugin;
	List<ConnectionHandler> threads = new ArrayList<ConnectionHandler>();
	public UBServer(int p,Ultrabans instance){
		port=p;
		plugin=instance;
	}
	@Override
	public synchronized void run(){
		try {
			server = new ServerSocket(port);
			alive=true;
		} catch (IOException e1) {
			plugin.getLogger().info("Could not listen on port");
			alive=false;
		}
		while(alive){
			try{
				Socket client = server.accept();
				ConnectionHandler connectionHandler = new ConnectionHandler(client, plugin);
				threads.add(connectionHandler);
			} catch (Exception e) {
				plugin.getLogger().info("Could not listen on port");
				disconnect();
			}
		}
		
	}
	public void disconnect(){
		alive=false;
		try {
			disconnectAll();
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void disconnectAll(){
		for(ConnectionHandler running:threads){
			running.alive=false;
			running.updater.interrupt();
			running.interrupt();
		}
	}
}
