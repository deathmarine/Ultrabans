/* COPYRIGHT (c) 2012 Joshua McCurry
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License
 * and use of this software or its code is an agreement to this license.
 * A full copy of this license can be found at
 * http://creativecommons.org/licenses/by-nc-sa/3.0/. 
 */
package com.modcrafting.ultrabans.commands;

import java.util.Date;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import com.modcrafting.ultrabans.Ultrabans;
import com.modcrafting.ultrabans.util.EditBan;
import com.modcrafting.ultrabans.util.Formatting;

public class History implements CommandExecutor{
	Ultrabans plugin;
	public History(Ultrabans ultraBan) {
		this.plugin = ultraBan;	
	}
	public boolean onCommand(final CommandSender sender, Command command, String label, final String[] args) {
		if(!sender.hasPermission(command.getPermission())){
			sender.sendMessage(ChatColor.RED+Ultrabans.DEFAULT_DENY_MESSAGE);
			return true;
		}
		if (args.length < 1) return false;
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){

			@Override
			public void run() {
				List<EditBan> bans = plugin.getUBDatabase().listRecent(args[0]);
				if(bans.size() < 1){
					String msg = plugin.getConfig().getString("Messages.History.Failed","Unable to find any bans.");
					msg=Formatting.formatMessage(msg);
					sender.sendMessage(ChatColor.RED + msg);
					return;
				}
				String msg = plugin.getConfig().getString("Messages.History.Header","Ultrabans Listing %amt% Records.");
				if(msg.contains(Ultrabans.AMOUNT)) msg=msg.replaceAll(Ultrabans.AMOUNT, args[0]);
				msg=Formatting.formatMessage(msg);
				sender.sendMessage(ChatColor.BLUE + msg);
				for(EditBan ban : bans){
					Date date = new Date();
					date.setTime(ban.time*1000);
					String dateStr = date.toString();
					sender.sendMessage(ChatColor.RED + Formatting.banType(ban.type) + ": " + ban.name + ChatColor.GRAY + " by " + ban.admin + " on " + dateStr.substring(4, 19) + " for " + ban.reason);
				}
				
			}
			
		});
		return true;
	}
}