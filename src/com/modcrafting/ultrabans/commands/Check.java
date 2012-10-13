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
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.Ultrabans;
import com.modcrafting.ultrabans.tracker.Track;
import com.modcrafting.ultrabans.util.EditBan;

public class Check implements CommandExecutor{
	Ultrabans plugin;
	public Check(Ultrabans ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Track.track(command.getName());
		if(!sender.hasPermission(command.getPermission())){
			sender.sendMessage(ChatColor.RED+plugin.perms);
			return true;
		}
		if (args.length < 1) return false;
		String p = args[0];
		
		//Additional Checks for correct player name
		Player check = plugin.getServer().getPlayer(p);
		if(check != null){
			p = check.getName();
		}else{
			check = plugin.getServer().getOfflinePlayer(p).getPlayer();
			if(check != null){
				p = check.getName();
			}
		}
		
		List<EditBan> bans = plugin.db.listRecords(p, sender);
		if(bans.isEmpty()){
			String msg = plugin.getConfig().getString("Messages.CheckBan.None","No records found for %victim%.");
			if(msg.contains(plugin.regexAmt)) msg=msg.replaceAll(plugin.regexAmt, String.valueOf(bans.size()));
			if(msg.contains(plugin.regexVictim))msg=msg.replaceAll(plugin.regexVictim, p);
			msg=plugin.util.formatMessage(msg);
			sender.sendMessage(ChatColor.GREEN + msg);
			return true;
		}
		String msg = plugin.getConfig().getString("Messages.CheckBan.Header","Found %amt% records for %victim%.");
		if(msg.contains(plugin.regexAmt)) msg=msg.replaceAll(plugin.regexAmt, String.valueOf(bans.size()));
		if(msg.contains(plugin.regexVictim)) msg=msg.replaceAll(plugin.regexVictim, bans.get(0).name);
		msg=plugin.util.formatMessage(msg);
		sender.sendMessage(ChatColor.BLUE + msg);
		for(EditBan ban : bans){
			sender.sendMessage(ChatColor.RED + plugin.util.banType(ban.type) + ChatColor.GRAY + ban.id + ": " + ChatColor.GREEN + ban.reason + ChatColor.AQUA +" by " + ban.admin);
		}
		return true;
	}
	
}
