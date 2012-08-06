/* COPYRIGHT (c) 2012 Joshua McCurry
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License
 * and use of this software or its code is an agreement to this license.
 * A full copy of this license can be found at
 * http://creativecommons.org/licenses/by-nc-sa/3.0/. 
 */
package com.modcrafting.ultrabans.commands;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;

public class Export implements CommandExecutor{
	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	String permission = "ultraban.export";
	public Export(UltraBan ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(final CommandSender sender, Command command, String commandLabel, String[] args) {
		boolean auth = false;
		Player player = null;
		if (sender instanceof Player){
			player = (Player)sender;
			if(player.hasPermission(permission)) auth = true;
		}else{
			auth = true;
		}
		if (auth) {
			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin,new Runnable(){

				@Override
				public void run() {
					try
					{
						BufferedWriter banlist = new BufferedWriter(new FileWriter("banned-players.txt",true));
						for(String p : plugin.bannedPlayers){
							banlist.newLine();
							banlist.write(g(p));
						}
						banlist.close();
						BufferedWriter iplist = new BufferedWriter(new FileWriter("banned-ips.txt",true));
						for(String p : plugin.bannedIPs){
							iplist.newLine();
							iplist.write(g(p));
						}
						iplist.close();
					}
					catch(IOException e)          
					{
						plugin.log.log(Level.SEVERE,"UltraBan: Couldn't write to banned-players.txt");
					}
					sender.sendMessage("§2Exported banlist to banned-players.txt.");
					sender.sendMessage("§2Exported iplist to banned-ips.txt.");
				}
				
			});
			
			return true;
		}else{
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}
	}
	//Thanks jeb_
	public String g(String player) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
		Date now = new Date();
		now.setTime(System.currentTimeMillis());
	    StringBuilder localStringBuilder = new StringBuilder();

	    localStringBuilder.append(player);
	    localStringBuilder.append("|");
	    localStringBuilder.append(format.format(now));
	    localStringBuilder.append("|");
	    String admin = plugin.db.getAdmin(player);
	    if(admin==null||admin.equalsIgnoreCase("")) admin = "Ultrabans";
	    localStringBuilder.append(admin);
	    localStringBuilder.append("|");
	    localStringBuilder.append("Forever");
	    localStringBuilder.append("|");
	    String reason = plugin.db.getBanReason(player);
	    if(reason.equalsIgnoreCase("")) reason = "Exported from Ultrabans";
	    localStringBuilder.append(reason);

	    return localStringBuilder.toString();
	  }
}
