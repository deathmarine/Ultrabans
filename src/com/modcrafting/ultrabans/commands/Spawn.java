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
import com.modcrafting.ultrabans.util.Formatting;

public class Spawn implements CommandExecutor{
	Ultrabans plugin;
	public Spawn(Ultrabans ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		if(!sender.hasPermission(command.getPermission())){
			sender.sendMessage(ChatColor.RED+Ultrabans.DEFAULT_DENY_MESSAGE);
			return true;
		}
		String admin = Ultrabans.DEFAULT_ADMIN;
		if(sender instanceof Player){
			admin = sender.getName();
		}
		if(args.length < 1) return false;
		String p = args[0];
		p = Formatting.expandName(p); 
		Player victim = plugin.getServer().getPlayer(p);
		String idoit = null;
		if(victim != null){
			idoit = victim.getName();
		}else{
			String smvic = config.getString("Messages.Spawn.Failed","%victim% is not online.");
			if(smvic.contains(Ultrabans.VICTIM))smvic=smvic.replaceAll(Ultrabans.VICTIM, p);
			smvic=Formatting.formatMessage(smvic);
			sender.sendMessage(ChatColor.GRAY + smvic);
			return true;
		}
		String smvic = config.getString("Messages.Spawn.MsgToVictim","You have been sent to spawn!");
		smvic=Formatting.formatMessage(smvic);
		victim.sendMessage(smvic);
		victim.teleport(victim.getWorld().getSpawnLocation());	
		
		String bcmsg = config.getString("Messages.Spawn.MsgToSender","%victim% is now at spawn!");
		if(bcmsg.contains(Ultrabans.ADMIN)) bcmsg = bcmsg.replaceAll(Ultrabans.ADMIN, admin);
		if(bcmsg.contains(Ultrabans.VICTIM)) bcmsg = bcmsg.replaceAll(Ultrabans.VICTIM, idoit);
		bcmsg=Formatting.formatMessage(bcmsg);
		sender.sendMessage(bcmsg);
		return true;
	}
}
