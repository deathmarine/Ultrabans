package com.modcrafting.ultrabans.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.Ultrabans;

public class Ping implements CommandExecutor{
	Ultrabans plugin;
	public Ping(Ultrabans ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
		if(!sender.hasPermission(command.getPermission())){
			sender.sendMessage(ChatColor.RED+plugin.perms);
			return true;
		}
		String p2 = plugin.getServer().getClass().getPackage().getName();
        String version = p2.substring(p2.lastIndexOf('.') + 1);
		if(args.length>0){
			Player p = plugin.getServer().getPlayer(args[0]);
			if(p!=null){
				String ping = null;
				if(version.equals("v1_4_6")){
					ping = String.valueOf(((org.bukkit.craftbukkit.v1_4_6.entity.CraftPlayer) p).getHandle().ping);
				}else if(version.equals("v1_4_5")){
					ping = String.valueOf(((org.bukkit.craftbukkit.v1_4_5.entity.CraftPlayer) p).getHandle().ping);
				}else{
					ping = String.valueOf(((org.bukkit.craftbukkit.entity.CraftPlayer) p).getHandle().ping);
				}
				sender.sendMessage(ChatColor.GRAY+p.getName()+"'s ping is: "+ChatColor.GOLD+ping+"ms");
			}else{
				sender.sendMessage(ChatColor.RED+"Player not found.");
			}
			return true;
		}
		if(sender instanceof Player){
			String ping = null;
			if(version.equals("v1_4_6")){
				ping = String.valueOf(((org.bukkit.craftbukkit.v1_4_6.entity.CraftPlayer) sender).getHandle().ping);
			}else if(version.equals("v1_4_5")){
				ping = String.valueOf(((org.bukkit.craftbukkit.v1_4_5.entity.CraftPlayer) sender).getHandle().ping);
			}else{
				ping = String.valueOf(((org.bukkit.craftbukkit.entity.CraftPlayer) sender).getHandle().ping);
			}
			sender.sendMessage(ChatColor.GRAY+"Your ping is: "+ChatColor.GOLD+ping+"ms");
		}else{
			sender.sendMessage(ChatColor.GRAY+"Your ping is: "+ChatColor.GOLD+"0ms");
		}
		return true;
	}
}
