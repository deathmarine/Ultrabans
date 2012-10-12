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
import com.modcrafting.ultrabans.Ultrabans;
import com.modcrafting.ultrabans.tracker.Track;

public class DupeIP implements CommandExecutor{
	Ultrabans plugin;
	public DupeIP(Ultrabans ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(final CommandSender sender, Command command, String label, final String[] args) {
		Track.track(command.getName());
		if(!sender.hasPermission(command.getPermission())){
			sender.sendMessage(ChatColor.RED+plugin.perms);
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
					String msg = plugin.getConfig().getString("Messages.DupeIP.Failed","Unable to view ip for %victim% !");
					if(msg.contains(plugin.regexVictim)) msg=msg.replaceAll(plugin.regexVictim, p);
					msg=plugin.util.formatMessage(msg);
					sender.sendMessage(ChatColor.RED + msg);
					return;
				}
				List<String> list = plugin.db.listPlayers(ip);
				String msg = plugin.getConfig().getString("Messages.DupeIP.Header","Scanning Current IP of %victim%: %ip% !");
				if(msg.contains(plugin.regexVictim)) msg=msg.replaceAll(plugin.regexVictim, p);
				if(msg.contains("%ip%")) msg=msg.replaceAll("%ip%", ip);
				msg=plugin.util.formatMessage(msg);
				sender.sendMessage(ChatColor.AQUA + msg);
				for(String name:list){
					if(!name.equalsIgnoreCase(p)){
						sender.sendMessage(ChatColor.GRAY + "Player: " + name + " duplicates player: " + p + "!");
					}
				}
				msg = plugin.getConfig().getString("Messages.DupeIP.Completed","Scanning Complete!");
				msg=plugin.util.formatMessage(msg);
				sender.sendMessage(ChatColor.GREEN + msg);
			}
		});
		return true;
	}
}
