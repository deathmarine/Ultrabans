package com.modcrafting.ultrabans.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.crypto.BadPaddingException;

import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;

public class ConnectionHandler extends Thread{
	UltraBan plugin;
	InputStream in;
	OutputStream out;
	String line;
	public Socket sock;
	UBHandler h;
	public ConnectionHandler(Socket client, UltraBan instance) throws IOException {
		plugin=instance;
		sock = client;
		in = sock.getInputStream();
		out = sock.getOutputStream();
		h = new UBHandler(this,plugin.crypto);
		plugin.getServer().getLogger().addHandler(h);
		plugin.getLogger().info(sock.getInetAddress().getHostAddress()+" connected the Live.");
		start();
	}

	boolean alive=true;
	@Override
	public void run() {
		while(alive){
			try{
				byte[] block = new byte[256];
				in.read(block, 0, block.length);
				block = plugin.crypto.decrypt(block);
				line = new String(block);
		        if(line!=null){
		        	if(line.contains(".sendCommand."))execute(line.replaceAll(".sendCommand.", ""));
		        	if(line.contains(".getPlayers."))getPlayers();
		        	if(line.contains(".bannedPlayers."))getBanned();
		        }
			} catch (BadPaddingException e) {
				alive=false;
			} catch (IOException e){
				alive=false;
			} catch (Exception e){
				alive=false;
			}
		}
		plugin.getServer().getLogger().removeHandler(h);
		try {
			in.close();
			out.close();
			sock.close();
			this.interrupt();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	private void execute(String str){
		plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), str);
	}
	public void getPlayers() throws Exception{
		StringBuilder sb = new StringBuilder();
		for(Player p:plugin.getServer().getOnlinePlayers()){
			sb.append(p.getName());
			sb.append(" ");
		}
		if(alive)out.write(plugin.crypto.encrypt((".oplayers."+sb.toString()).getBytes()));
	}
	public void getBanned() throws Exception{
		StringBuilder sb = new StringBuilder();
		for(String p:plugin.bannedPlayers){
			sb.append(p);
			sb.append(" ");
		}
		if(alive)out.write(plugin.crypto.encrypt((".bplayers."+sb.toString()).getBytes()));
	}
	public void write(byte[] bytes) throws IOException{
		if(alive){
			out.write(bytes);
		}
	}
	public void flush() throws IOException{
		if(alive){
			out.flush();
		}
	}
	public void close() throws IOException{
		if(sock.isConnected()&&!sock.isOutputShutdown()){
			out.close();
		}
		
	}

}
