package com.modcrafting.ultrabans.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.modcrafting.ultrabans.UltraBan;

public class UBServer implements Runnable{
	ServerSocket server;
	boolean alive;
	int port;
	UltraBan plugin;
	List<Thread> threads = new ArrayList<Thread>();
	public UBServer(int p,UltraBan instance){
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
				plugin.getLogger().info("Found the client");
				ConnectionHandler connectionHandler = new ConnectionHandler(client, plugin);
				Thread t = new Thread(connectionHandler);
				threads.add(t);
				t.start();
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
	public void disconnectAll(){
		for(Thread running:threads){
			running.interrupt();
		}
	}
}
