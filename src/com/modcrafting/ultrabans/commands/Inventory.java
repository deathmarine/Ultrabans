package com.modcrafting.ultrabans.commands;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;

public class Inventory implements CommandExecutor{
	UltraBan plugin;
	public Inventory(UltraBan instance){
		plugin = instance;
	}
	//Attempting to condense more.
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lb, String[] args) {
		if(sender instanceof Player){
			if(sender.hasPermission("ultraban.inv")){
				if(args.length<1)return false;
				OfflinePlayer victim = plugin.getServer().getOfflinePlayer(args[0]);
				if(victim==null){
					sender.sendMessage(ChatColor.RED+"Unable to find player");
					return true;
				}
				((Player)sender).openInventory(victim.getPlayer().getInventory());
				return true;
			}else{
				sender.sendMessage(ChatColor.RED+"You do not have the nessecary permissions.");
				return true;
			}
		}
		return false;
	}

}
