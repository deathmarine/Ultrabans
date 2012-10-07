package com.modcrafting.ultrabans.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;

public class UBServer implements Runnable{
	ServerSocket server;
	Socket client;
	InputStream in;
	OutputStream out;
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
			in = client.getInputStream();
			out = client.getOutputStream();
			plugin.getServer().getLogger().addHandler(new UBHandler(out,plugin.crypto));
		} catch (IOException e) {
			System.out.println("Could not listen on port");
		}
		alive=true;
		while(alive){
			try{
				byte[] block = new byte[256];
				in.read(block, 0, block.length);
				block = plugin.crypto.decrypt(block);
				line = new String(block);
		        if(line!=null){
		        	if(line.contains(".sendCommand."))execute(line.replaceAll(".sendCommand.", "")+"\n");
		        	if(line.contains(".getPlayers."))getPlayers();
					System.out.println(line);
		        }
			} catch (IOException e) {
				System.out.println("Client Disconnected.");
			} catch (Exception e) {
				e.printStackTrace();
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
	public void getPlayers() throws Exception{
		StringBuilder sb = new StringBuilder();
		for(Player p:plugin.getServer().getOnlinePlayers()){
			sb.append(p.getName());
			sb.append(" ");
		}
		out.write(plugin.crypto.encrypt((".players."+sb.toString()).getBytes()));
	}
}
