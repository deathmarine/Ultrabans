package com.modcrafting.ultrabans.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.modcrafting.ultrabans.Ultrabans;

public class Status implements CommandExecutor {
	Ultrabans plugin;
	public Status(Ultrabans instance){
		plugin = instance;
	}
	public boolean onCommand(CommandSender sender, Command command, String label, String[] arg3) {
		if(!sender.hasPermission(command.getPermission())){
			sender.sendMessage(ChatColor.RED+Ultrabans.DEFAULT_DENY_MESSAGE);
			return true;
		}
		sender.sendMessage(ChatColor.BLUE+"===Status===");
		sender.sendMessage(ChatColor.GRAY+"Currently Caching: "+ChatColor.AQUA+String.valueOf(plugin.bannedPlayers.size())+" Bans");
		sender.sendMessage(ChatColor.GRAY+"Currently Caching: "+ChatColor.AQUA+String.valueOf(plugin.bannedIPs.size())+" IPBans");
		sender.sendMessage(ChatColor.GRAY+"Currently Caching: "+ChatColor.AQUA+String.valueOf(plugin.jailed.size())+" Jailed");
		int counter=0;
		for(String a:plugin.bannedPlayers){
			counter=counter+a.getBytes().length;
		}
		for(String a:plugin.bannedIPs){
			counter=counter+a.getBytes().length;
		}
		for(String a:plugin.jailed){
			counter=counter+a.getBytes().length;
		}
		//Close enough.
		sender.sendMessage(ChatColor.GRAY+"Estimated Usage: "+ChatColor.AQUA+String.valueOf(counter)+" bytes.");
		return true;
	}
	
}
