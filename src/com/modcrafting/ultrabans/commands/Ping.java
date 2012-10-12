package com.modcrafting.ultrabans.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.Ultrabans;
import com.modcrafting.ultrabans.tracker.Track;

public class Ping implements CommandExecutor{
	Ultrabans plugin;
	public Ping(Ultrabans ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
		Track.track(command.getName());
		if(!sender.hasPermission(command.getPermission())){
			sender.sendMessage(ChatColor.RED+plugin.perms);
			return true;
		}
		if(args.length>0){
			Player p = plugin.getServer().getPlayer(args[0]);
			if(p!=null){
				String ping = String.valueOf(((CraftPlayer)p).getHandle().ping);
				sender.sendMessage(ChatColor.GRAY+p.getName()+"'s ping is: "+ChatColor.GOLD+ping+"ms");
			}else{
				sender.sendMessage(ChatColor.RED+"Player not found.");
			}
			return true;
		}
		if(sender instanceof Player){
			String ping = String.valueOf(((CraftPlayer)((Player) sender)).getHandle().ping);
			sender.sendMessage(ChatColor.GRAY+"Your ping is: "+ChatColor.GOLD+ping+"ms");
		}else{
			sender.sendMessage(ChatColor.GRAY+"Your ping is: "+ChatColor.GOLD+"0ms");
		}
		return true;
	}
}
