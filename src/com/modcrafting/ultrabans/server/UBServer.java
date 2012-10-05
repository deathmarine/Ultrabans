package com.modcrafting.ultrabans.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;

public class UBServer implements Runnable{
	ServerSocket server;
	Socket client;
	BufferedReader in;
	PrintWriter out;
	String line;
	boolean alive;
	int port;
	UltraBan plugin;
	public UBServer(int p,UltraBan instance){
		port=p;
		plugin=instance;
	}
	@Override
	public void run(){
		try{
			server = new ServerSocket(port);
			client = server.accept(); 
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			out = new PrintWriter(client.getOutputStream(), true);
			plugin.getServer().getLogger().addHandler(new UBHandler(out));
		} catch (IOException e) {
			System.out.println("Could not listen on port");
		}
		alive=true;
		while(alive){ 
			try{
				client = server.accept();
				line = in.readLine();
		        if(line!=null){
		        	if(line.startsWith("a"))execute(line.substring(1)+"\n");
		        	if(line.startsWith("pl"))getPlayers();
		        }
			} catch (IOException e) {
				System.out.println("Client Disconnected.");
			}
		}		
	}
	public void disconnect(){
		alive=false;
		try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void execute(String str){
		plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), str);
	}
	public void getPlayers(){
		StringBuilder sb = new StringBuilder();
		for(Player p:plugin.getServer().getOnlinePlayers()){
			sb.append(p.getName());
			sb.append(" ");
		}
		out.println("pl"+sb.toString()+"\n");
	}
}
