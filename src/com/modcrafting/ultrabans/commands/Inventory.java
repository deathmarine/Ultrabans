package com.modcrafting.ultrabans.commands;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.Ultrabans;

public class Inventory implements CommandExecutor{
	Ultrabans plugin;
	public Inventory(Ultrabans instance){
		plugin = instance;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!sender.hasPermission(command.getPermission())){
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}
		if(sender instanceof Player){
			if(args.length<1)return false;
			OfflinePlayer victim = plugin.getServer().getOfflinePlayer(args[0]);
			if(victim==null||!victim.isOnline()){
				sender.sendMessage(ChatColor.RED+"Unable to find player");
				return true;
			}
			((Player)sender).openInventory(victim.getPlayer().getInventory());			
		}else{
			sender.sendMessage("This command must be executed by a player.");
		}
		return true;
	}

}
