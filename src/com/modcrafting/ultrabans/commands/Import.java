/* COPYRIGHT (c) 2012 Joshua McCurry
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License
 * and use of this software or its code is an agreement to this license.
 * A full copy of this license can be found at
 * http://creativecommons.org/licenses/by-nc-sa/3.0/. 
 */
package com.modcrafting.ultrabans.commands;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;

public class Import implements CommandExecutor{
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
			sender.sendMessage(ChatColor.GRAY + "Be patient. Loading");
			sender.sendMessage(ChatColor.GRAY + "Depending on size of list may lag the server for a moment.");
			}
			plugin.getLogger().log(Level.INFO, "Be patient. Loading.");
			plugin.getLogger().log(Level.INFO, "Depending on size of list may lag the server for a moment.");
		try {
			BufferedReader banlist = new BufferedReader(new FileReader("banned-players.txt"));
			String p;
			//I think Mojang took my ideas and improved on them.
			while ((p = banlist.readLine()) != null){
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
				String[] arg = p.split("|");
				if(arg.length>0){
					Date date = format.parse(arg[1]);
					int type = 0;
					long temptime = 0;
					if(!arg[3].trim().contains("Forever")){
						temptime = format.parse(arg[3].trim()).getTime()/1000;
						type = 1;
					}
				    long sec = date.getTime()/1000;
					plugin.db.importPlayer(arg[0].trim(), arg[4].trim(), arg[2].trim(), temptime, sec, type);
						
				}else{
					if(!plugin.bannedPlayers.contains(p.toLowerCase())){
						plugin.db.addPlayer(p.toLowerCase(), "imported", "system", 0, 0);
						plugin.bannedPlayers.add(p.toLowerCase());
							
					}
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
			plugin.getLogger().log(Level.INFO,"System imported the banlist to the database.");
			
			return;
			
		} catch (IOException e) {
			plugin.getLogger().log(Level.SEVERE, "Could not import ban list.");
			sender.sendMessage(ChatColor.RED + "Could not import ban list.");
		} catch (ParseException e) {
			e.printStackTrace();
		}
			}
			});

		return true;
		}
		return false;
	}
}
