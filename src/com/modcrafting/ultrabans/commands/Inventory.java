package com.modcrafting.ultrabans.commands;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.Ultrabans;

public class Inventory extends CommandHandler{
	public Inventory(Ultrabans instance) {
		super(instance);
	}
	
	public String command(CommandSender sender, Command command, String[] args) {
		if(sender instanceof Player){
			if (args.length < 1) 
				return lang.getString("InvOf.Arguments");
			OfflinePlayer victim = plugin.getServer().getOfflinePlayer(args[0]);
			if(victim==null||!victim.isOnline()){
				return ChatColor.translateAlternateColorCodes('&', lang.getString("InvOf.Failed"));
			}
			((Player) sender).openInventory(victim.getPlayer().getInventory());
			return null;
		}
		return ChatColor.translateAlternateColorCodes('&', lang.getString("InvOf.Console"));
	}
}
