package com.modcrafting.ultrabans.commands;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;

public class Reload implements CommandExecutor{
	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	String permission = "ultraban.reload";
	public Reload(UltraBan ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		boolean auth = false;
		Player player = null;
		String admin = "server";
		if (sender instanceof Player){
			player = (Player)sender;
			if(player.hasPermission(permission) || player.isOp()) auth = true;
			admin = player.getName();
		}else{
			auth = true; //if sender is not a player - Console
		}
		
		if (auth) {
			log.log(Level.INFO, "[UltraBan] Disabling Plugin.");
			plugin.getServer().getPluginManager().disablePlugin(plugin);
			plugin.log.log(Level.SEVERE, "[UltraBan] Attempting Restart.");
			plugin.getServer().getPluginManager().enablePlugin(plugin);
			log.log(Level.INFO, "[UltraBan] " + admin + " reloaded the plugin.");
			sender.sendMessage("§2[UltraBan] reloaded.");
			plugin.reloadConfig();
			return true;
		}else{
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}
	}
	


}
