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
import com.modcrafting.ultrabans.UltraBan;
import com.modcrafting.ultrabans.util.EditBan;

public class History implements CommandExecutor{
	UltraBan plugin;
	String permission = "ultraban.history";
	public History(UltraBan ultraBan) {
		this.plugin = ultraBan;	
	}
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!sender.hasPermission(command.getPermission())){
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}
		if (args.length < 1) return false;
		String p = args[0];
		List<EditBan> bans = plugin.db.listRecent(p);
		if(bans.size() < 1){
			sender.sendMessage(ChatColor.GREEN + "Error in command");
			return true;
		}
		sender.sendMessage(ChatColor.BLUE + "Ultrabans Listing " + ChatColor.GRAY + args[0] + ChatColor.BLUE + " records.");
		for(EditBan ban : bans){
			Date date = new Date();
			date.setTime(ban.time*1000);
			String dateStr = date.toString();
			sender.sendMessage(ChatColor.RED + plugin.util.banType(ban.type) + ": " + ban.name + ChatColor.GRAY + " by " + ban.admin + " on " + dateStr.substring(4, 19) + " for " + ban.reason);
		}
		return true;
	}
}