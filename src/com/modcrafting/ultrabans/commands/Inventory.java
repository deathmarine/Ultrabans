package com.modcrafting.ultrabans.commands;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.Ultrabans;
import com.modcrafting.ultrabans.tracker.Track;

public class Inventory implements CommandExecutor{
	Ultrabans plugin;
	public Inventory(Ultrabans instance){
		plugin = instance;
	}
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Track.track(command.getName());
		if(!sender.hasPermission(command.getPermission())){
			sender.sendMessage(ChatColor.RED+plugin.perms);
			return true;
		}
		if(sender instanceof Player){
			if(args.length<1)return false;
			OfflinePlayer victim = plugin.getServer().getOfflinePlayer(args[0]);
			if(victim==null||!victim.isOnline()){
				String msg = plugin.getConfig().getString("Messages.InvOf.Failed","Unable to find player.");
				msg=plugin.util.formatMessage(msg);
				sender.sendMessage(ChatColor.RED + msg);
				return true;
			}
			((Player)sender).openInventory(victim.getPlayer().getInventory());			
		}else{
			String msg = plugin.getConfig().getString("Messages.InvOf.Console","This command must be executed by a player.");
			msg=plugin.util.formatMessage(msg);
			sender.sendMessage(ChatColor.RED + msg);
		}
		return true;
	}

}
