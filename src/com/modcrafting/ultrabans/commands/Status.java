package com.modcrafting.ultrabans.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.modcrafting.ultrabans.UltraBan;

public class Status implements CommandExecutor {
	UltraBan plugin;
	public Status(UltraBan instance){
		plugin = instance;
	}
	public boolean onCommand(CommandSender sender, Command command, String label, String[] arg3) {
		if(!sender.hasPermission(command.getPermission())){
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}
		sender.sendMessage(ChatColor.BLUE+"===Status===");
		sender.sendMessage(ChatColor.GRAY+"Currently Caching: "+ChatColor.AQUA+String.valueOf(plugin.bannedPlayers.size())+" Bans");
		sender.sendMessage(ChatColor.GRAY+"Currently Caching: "+ChatColor.AQUA+String.valueOf(plugin.bannedIPs.size())+" IPBans");
		sender.sendMessage(ChatColor.GRAY+"Currently Caching: "+ChatColor.AQUA+String.valueOf(plugin.jailed.size())+" Jailed");
		sender.sendMessage(ChatColor.GRAY+"Estimated Usage: "+ChatColor.AQUA+String.valueOf(plugin.bannedIPs.toString().getBytes().length+plugin.bannedPlayers.toString().getBytes().length)+" bytes.");
		return true;
	}
}
