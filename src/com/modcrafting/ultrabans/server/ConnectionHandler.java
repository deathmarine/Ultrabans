package com.modcrafting.ultrabans.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.crypto.BadPaddingException;

import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;

public class ConnectionHandler implements Runnable {
	UltraBan plugin;
	InputStream in;
	OutputStream out;
	String line;
	public Socket sock;
	public ConnectionHandler(Socket client, UltraBan instance) throws IOException {
		plugin=instance;
		sock = client;
		in = sock.getInputStream();
		out = sock.getOutputStream();
		plugin.getLogger().info("Instantiated ConnectionHandler");
		plugin.getServer().getLogger().addHandler(new UBHandler(out,plugin.crypto));
		plugin.getLogger().info("Setup Logger Handler");
	}

	@Override
	public void run() {
		boolean alive=true;
		while(alive){
			try{
				byte[] block = new byte[256];
				in.read(block, 0, block.length);
				block = plugin.crypto.decrypt(block);
				line = new String(block);
		        if(line!=null){
		        	if(line.contains(".sendCommand."))execute(line.replaceAll(".sendCommand.", "")+"\n");
		        	if(line.contains(".getPlayers."))getPlayers();
		        }
			} catch (BadPaddingException e) {
				plugin.getLogger().info("Live : Found bad packet. Dismissed.");
				alive=false;
			} catch (IOException e){
				plugin.getLogger().info("Live : Unable to write.");
				alive=false;
			} catch (Exception e){
				e.printStackTrace();
				alive=false;
			}
		}		
		try {
			in.close();
			out.close();
			sock.close();
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
		out.write(plugin.crypto.encrypt((".players."+sb.toString()).getBytes()));
	}

}
