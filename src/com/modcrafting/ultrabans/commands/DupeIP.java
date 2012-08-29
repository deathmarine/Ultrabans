/* COPYRIGHT (c) 2012 Joshua McCurry
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License
 * and use of this software or its code is an agreement to this license.
 * A full copy of this license can be found at
 * http://creativecommons.org/licenses/by-nc-sa/3.0/. 
 */
package com.modcrafting.ultrabans.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import com.modcrafting.ultrabans.UltraBan;

public class DupeIP implements CommandExecutor{
	UltraBan plugin;
	public DupeIP(UltraBan ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(final CommandSender sender, Command command, String label, final String[] args) {
		if(!sender.hasPermission(command.getPermission())){
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}
		if (args.length < 1) return false;
		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin,new Runnable(){
			@Override
			public void run() {
				String p = args[0];
				
				p = plugin.util.expandName(p); 
				String ip = plugin.db.getAddress(p);
				if(ip == null){
					sender.sendMessage(ChatColor.RED + "Unable to view ip for " + p + " !");
					return;
				}
				List<String> list = plugin.db.listPlayers(ip);
				sender.sendMessage(ChatColor.AQUA + "Scanning Current IP of " + p + ": " + ip + " !");
				for(String name:list){
					if(!name.equalsIgnoreCase(p)) sender.sendMessage(ChatColor.GRAY + "Player: " + name + " duplicates player: " + p + "!");
				}
				sender.sendMessage(ChatColor.GREEN + "Scanning Complete!");
			}
		});
		return true;
	}
}
