package com.modcrafting.ultrabans.commands;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;

public class CheckIP implements CommandExecutor{
	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	
	public CheckIP(UltraBan ultraBan) {
		this.plugin = ultraBan;
	
	}
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		boolean auth = false;
		Player player = null;
		if (sender instanceof Player){
			player = (Player)sender;
			if (plugin.setupPermissions()){
				if (plugin.permission.has(player, "ultraban.checkip")) auth = true;
			}else{
			 if (player.isOp()) auth = true; 
			 }
		}else{
			auth = true; //if sender is not a player - Console
		}
		if (!auth){
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}else{
		if (args.length < 1) return false;
		String p = args[0];
		String ip = plugin.getServer().getPlayer(p.toLowerCase()).getAddress().getAddress().getHostAddress();
		
		if(p.isEmpty()){
			sender.sendMessage(ChatColor.RED + "Unable to view ip for " + p + " !");
			return true;
		}
		if(ip.isEmpty()){
			sender.sendMessage(ChatColor.RED + "Unable to view ip for " + p + " !");
			return true;
		}
		sender.sendMessage(ChatColor.BLUE + "Player: " + p + " IP :" + ip + " !");
		return true;
		}
	}
}
