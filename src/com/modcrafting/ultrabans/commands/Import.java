package com.modcrafting.ultrabans.commands;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;

public class Import implements CommandExecutor{

	public static final Logger log = Logger.getLogger("Minecraft");
	boolean server = false;
	UltraBan plugin;
	String permission = "ultraban.import";
	public Import(UltraBan ultraBan) {
	this.plugin = ultraBan;
	}
	public boolean onCommand(final CommandSender sender, Command command, String commandLabel, String[] args) {
    	boolean auth = false;
		Player player = null;
		if (sender instanceof Player){
			player = (Player)sender;
			if(player.hasPermission(permission) || player.isOp()) auth = true;
			player.getName();
		}else{
			auth = true;
			server = true;
		}
		if (!auth){
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}
		if(auth){
			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin,new Runnable(){

			@Override
			public void run() {
			if(!server){
			sender.sendMessage(ChatColor.GRAY + "[UltraBan] Be patient. Loading");
			sender.sendMessage(ChatColor.GRAY + "[UltraBan] Depending on size of list may lag the server for a moment.");
			}
			plugin.log.log(Level.INFO, "[UltraBan] Be patient. Loading.");
			plugin.log.log(Level.INFO, "[UltraBan] Depending on size of list may lag the server for a moment.");
		try {
			BufferedReader banlist = new BufferedReader(new FileReader("banned-players.txt"));
			String p;
			
			while ((p = banlist.readLine()) != null){
					if(!plugin.bannedPlayers.contains(p.toLowerCase())){
						plugin.db.addPlayer(p.toLowerCase(), "imported", "system", 0, 0);
						plugin.bannedPlayers.add(p.toLowerCase());
						
					}
				  }
			BufferedReader bannedIP = new BufferedReader(new FileReader("banned-ips.txt"));
			String ip;
			
			while ((ip = bannedIP.readLine()) != null){
					if(!plugin.bannedIPs.contains(ip))
						plugin.bannedIPs.add(ip);
					String cknullIP = plugin.db.getName(ip);
					if (cknullIP != null){
						plugin.db.addPlayer(plugin.db.getName(ip), "imported", "system", 0, 1);
					}else{
						plugin.db.setAddress("import", ip);
						
						plugin.db.addPlayer("import", "imported", "system", 0, 1);
					}
					Bukkit.banIP(ip);
				  }
			
			sender.sendMessage(ChatColor.GREEN + "Banlist imported.");
			plugin.log.log(Level.INFO,"[UltraBan] " + "system" + " imported the banlist to the database.");
			
			return;
			
		} catch (IOException e) {
			plugin.log.log(Level.SEVERE, "[Ultrabans] could not import ban list.");
			sender.sendMessage(ChatColor.RED + "Could not import ban list.");
		}
			}
			});

		return true;
		}
		return false;
	}
}
