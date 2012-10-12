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
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		alive=true;
		while(alive){
			try{
				Socket client = server.accept();
				ConnectionHandler connectionHandler = new ConnectionHandler(client, plugin);
				threads.add(connectionHandler);
			} catch (IOException e) {
				plugin.getLogger().info("Could not listen on port");
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
