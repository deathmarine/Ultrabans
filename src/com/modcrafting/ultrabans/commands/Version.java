package com.modcrafting.ultrabans.commands;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

import com.modcrafting.ultrabans.UltraBan;

public class Version implements CommandExecutor{
	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	String permission = "ultraban.version";
	public Version(UltraBan ultraBan) {
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
		
		if(auth){
		PluginDescriptionFile pdfFile = plugin.getDescription();
		sender.sendMessage(ChatColor.BLUE + "Thank you " + admin + " for using:");
		sender.sendMessage(ChatColor.GRAY + pdfFile.getName() + " version " + pdfFile.getVersion() + " by " + pdfFile.getAuthors());
		return true;
		}else{
		sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
		}
		return false;
	}
	
}

