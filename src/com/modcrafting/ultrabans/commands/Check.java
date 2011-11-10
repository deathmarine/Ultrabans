package com.modcrafting.ultrabans.commands;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.modcrafting.ultrabans.UltraBan;

public class Check implements CommandExecutor{
	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	
	public Check(UltraBan ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		String p = args[0];
		if(plugin.bannedPlayers.contains(p.toLowerCase()))
			sender.sendMessage(ChatColor.GRAY + "Player " + p + " is banned.");
		else
			sender.sendMessage(ChatColor.BLUE + "Player " + p + " is not banned.");
		return true;
	}
}
