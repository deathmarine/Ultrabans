/* COPYRIGHT (c) 2012 Joshua McCurry
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License
 * and use of this software or its code is an agreement to this license.
 * A full copy of this license can be found at
 * http://creativecommons.org/licenses/by-nc-sa/3.0/. 
 */
package com.modcrafting.ultrabans.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.Ultrabans;
import com.modcrafting.ultrabans.tracker.Track;

public class Empty implements CommandExecutor{
	Ultrabans plugin;
	public Empty(Ultrabans ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Track.track(command.getName());
		if(!sender.hasPermission(command.getPermission())){
			sender.sendMessage(ChatColor.RED+plugin.perms);
			return true;
		}
    	YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		String admin = plugin.admin;
		if (sender instanceof Player){
			admin = sender.getName();
		}
		if (args.length < 1) return false;
		String p = args[0];
		p = plugin.util.expandName(p); 
		
		Player victim = plugin.getServer().getPlayer(p);
		if (victim == null){
			String smvic = config.getString("Messages.Empty.Online","%victim% must be online.");
			smvic=plugin.util.formatMessage(smvic);
			sender.sendMessage(ChatColor.GRAY + smvic);
			return true;
		}
		String idoit = victim.getName();
		String msgvic = config.getString("Messages.Empty.MsgToVictim","%admin% has cleared your inventory!");
		if(msgvic.contains(plugin.regexAdmin))msgvic = msgvic.replaceAll(plugin.regexAdmin, admin);
		msgvic=plugin.util.formatMessage(msgvic);
		victim.sendMessage(msgvic);
		
		String bcmsg = config.getString("Messages.Empty.MsgToSender","%admin% has cleared the inventory of %victim%!");
		if(bcmsg.contains(plugin.regexAdmin)) bcmsg = bcmsg.replaceAll(plugin.regexAdmin, admin);
		if(bcmsg.contains(plugin.regexVictim)) bcmsg = bcmsg.replaceAll(plugin.regexVictim, idoit);
		bcmsg=plugin.util.formatMessage(bcmsg);
		sender.sendMessage(bcmsg);
		
		victim.getInventory().clear();
		return true;		
	}

}
