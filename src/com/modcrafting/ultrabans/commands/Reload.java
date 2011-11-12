package com.modcrafting.ultrabans.commands;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;
import com.nijikokun.bukkit.Permissions.Permissions;

public class Reload implements CommandExecutor{
	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	
	public Reload(UltraBan ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		boolean auth = false;
		Player player = null;
		String admin = "server";
		if (sender instanceof Player){
			player = (Player)sender;
			if (Permissions.Security.permission(player, "ultraban.reload")){
				auth = true;
			}else{
			 if (player.isOp()) auth = true; //defaulting to Op if no permissions or node
			}
			admin = player.getName();
		}else{
			auth = true; //if sender is not a player - Console
		}
		
		if (auth) {
			plugin.bannedPlayers.clear(); // Clear the HASHSET (RAM)
			plugin.tempBans.clear();
			plugin.bannedIPs.clear();
			log.log(Level.INFO, "[UltraBan] Unloading Ram, Reinitializing.");
			plugin.db.initialize(plugin);
			
			log.log(Level.INFO, "[UltraBan] " + admin + " reloaded the banlist.");
			sender.sendMessage("§2Reloaded banlist.");
			return true;
		}else{
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}
	}
	


}
