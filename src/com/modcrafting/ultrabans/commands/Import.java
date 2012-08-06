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
import java.util.logging.Level;
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
				if(!p.contains("#")&&p.length()>0){
					g(p);
				}
			}
			BufferedReader bannedIP = new BufferedReader(new FileReader("banned-ips.txt"));
			String ip;
			
			while ((ip = bannedIP.readLine()) != null){
				if(ip!=null&&!ip.contains("#")&&ip.length()>0){
				    String[] args = ip.split("\\|");
				    String name = args[0].trim();
				    
						if(!plugin.bannedIPs.contains(name))
							plugin.bannedIPs.add(name);
						String cknullIP = plugin.db.getName(name);
						if (cknullIP != null){
							plugin.db.addPlayer(plugin.db.getName(name), "imported", args[2].trim(), 0, 1);
						}else{
							plugin.db.setAddress("import", name);
							
							plugin.db.addPlayer("import", "imported", args[2].trim(), 0, 1);
						}
					  }
					
				}
			
			sender.sendMessage(ChatColor.GREEN + "Banlist imported.");
			plugin.getLogger().log(Level.INFO,"System imported the banlist to the database.");
			
			return;
			
		} catch (IOException e) {
			plugin.getLogger().log(Level.SEVERE, "Could not import ban list.");
			sender.sendMessage(ChatColor.RED + "Could not import ban list.");
		}
			}
			});

		return true;
		}
		return false;
	}

	public void g(String line) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
	    String[] args = line.split("\\|");
	    if(args.length<4) return;
	    String name = args[0].trim();
	    long temp = 0;
	    int type = 0;
	    try {
			//Date conversion = format.parse(args[1].trim());
		    if(!args[3].trim().equalsIgnoreCase("Forever")){
		    	temp=format.parse(args[3].trim()).getTime();
		    }
		} catch (ParseException e) {
			e.printStackTrace();
		}
	    String admin = args[2].trim();
	    String reason = args[4];
		plugin.db.addPlayer(name, reason, admin, temp, type);
	}
}
