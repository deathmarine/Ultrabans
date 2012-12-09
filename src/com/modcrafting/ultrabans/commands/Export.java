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
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import com.modcrafting.ultrabans.Ultrabans;

public class Export implements CommandExecutor{
	Ultrabans plugin;
	public Export(Ultrabans ultraBan) {
		this.plugin = ultraBan;
	}
	@SuppressWarnings("deprecation")
	public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
		if(!sender.hasPermission(command.getPermission())){
			sender.sendMessage(ChatColor.RED+plugin.perms);
			return true;
		}
		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin,new Runnable(){
			@Override
			public void run() {
				try{
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
				}catch(IOException e){
					String msg = plugin.getConfig().getString("Messages.Export.Failed","Could not export ban lists.");
					msg=plugin.util.formatMessage(msg);
					sender.sendMessage(ChatColor.RED + msg);
					plugin.getLogger().severe(msg);
				}
				String msg = plugin.getConfig().getString("Messages.Export.Completed","Exported banlists.");
				msg=plugin.util.formatMessage(msg);
				sender.sendMessage(ChatColor.GREEN + msg);
				plugin.getLogger().severe(msg);
			}
			
		});
		
		return true;
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
