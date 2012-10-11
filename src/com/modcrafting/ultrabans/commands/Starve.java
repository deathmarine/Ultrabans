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

public class Starve implements CommandExecutor{
	Ultrabans plugin;
	public Starve(Ultrabans ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		String admin = plugin.admin;
		if (sender instanceof Player){
			admin = sender.getName();
		}
		if(!sender.hasPermission(command.getPermission())){
			sender.sendMessage(ChatColor.RED+plugin.perms);
			return true;
		}
		if (args.length < 1) return false;
		String p = args[0];
		p = plugin.util.expandName(p); 
		Player victim = plugin.getServer().getPlayer(p);
		String idoit = null;
		if (victim != null){
			idoit = victim.getName();
			if(victim.getName() == admin){
				String bcmsg = config.getString("Messages.Starve.Emo","You cannot starve yourself!");
				bcmsg = plugin.util.formatMessage(bcmsg);
				sender.sendMessage(bcmsg);
				return true;
			}
			if(victim.hasPermission( "ultraban.override.starve")&&!admin.equalsIgnoreCase(plugin.admin)){
				String bcmsg = config.getString("Messages.Starve.Denied","Your starve attempt has been denied!");
				bcmsg = plugin.util.formatMessage(bcmsg);
				sender.sendMessage(bcmsg);
				return true;
			}
		}else{
			String smvic = config.getString("Messages.Starve.Failed","%victim% is not Online.");
			smvic=plugin.util.formatMessage(smvic);
			sender.sendMessage(ChatColor.GRAY + smvic);
			return true;
		}
		String smvic = config.getString("Messages.Starve.MsgToVictim","You are now starving!");
		smvic=plugin.util.formatMessage(smvic);
		victim.sendMessage(smvic);
		victim.setFoodLevel(0);	
		
		String bcmsg = config.getString("Messages.Starve.MsgToSender","%victim% is now starving!");
		if(bcmsg.contains(plugin.regexAdmin)) bcmsg = bcmsg.replaceAll(plugin.regexAdmin, admin);
		if(bcmsg.contains(plugin.regexVictim)) bcmsg = bcmsg.replaceAll(plugin.regexVictim, idoit);
		bcmsg=plugin.util.formatMessage(bcmsg);
		sender.sendMessage(bcmsg);
		return true;
	}
}
