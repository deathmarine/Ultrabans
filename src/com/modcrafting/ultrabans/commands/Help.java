package com.modcrafting.ultrabans.commands;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

import com.modcrafting.ultrabans.UltraBan;
import com.nijikokun.bukkit.Permissions.Permissions;

public class Help implements CommandExecutor{
	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	
	public Help(UltraBan ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		boolean auth = false;
		Player player = null;
		String admin = "server";
		if (sender instanceof Player){
			player = (Player)sender;
			if (Permissions.Security.permission(player, "ultraban.help")){
				auth = true;
			}else{
			 if (player.isOp()) auth = true;
			}
			admin = player.getName();
		}else{
			auth = true;
		}
		if (auth) {
		PluginDescriptionFile pdfFile = plugin.getDescription();
		sender.sendMessage(ChatColor.GRAY + pdfFile.getName() + " version " + pdfFile.getVersion() + " Help System");
		sender.sendMessage(ChatColor.BLUE + "Current User " + admin);
		sender.sendMessage(ChatColor.GRAY + "-----------------------------------------------");
		sender.sendMessage(ChatColor.BLUE + "Required Info {}" + ChatColor.GREEN + " Optional ()");
		sender.sendMessage(ChatColor.GRAY + "/ban      {player} (-s) {reason}");
		sender.sendMessage(ChatColor.GRAY + "/tempban  {player} (-s) {amt} {sec/min/hour/day} {Reason}");
		sender.sendMessage(ChatColor.GRAY + "/ipban    {player} (-s) {reason}");
		sender.sendMessage(ChatColor.GRAY + "/unban    {player}");
		sender.sendMessage(ChatColor.GRAY + "/checkban {player}");
		sender.sendMessage(ChatColor.GRAY + "/kick     {player} (-s) {reason}");
		sender.sendMessage(ChatColor.GRAY + "/warn     {player} (-s) {reason}");
		sender.sendMessage(ChatColor.GRAY + "/fine     {player} (-s) {amt}");
		sender.sendMessage(ChatColor.GRAY + "/empty    {player}");
		sender.sendMessage(ChatColor.GRAY + "/forcespawn {player}");
		sender.sendMessage(ChatColor.GRAY + "/starve     {player}");
		sender.sendMessage(ChatColor.GRAY + "/editban  (help)");
		sender.sendMessage(ChatColor.GRAY + "/exportbans");
		sender.sendMessage(ChatColor.GRAY + "/uhelp");
		sender.sendMessage(ChatColor.GRAY + "/ureload");
		sender.sendMessage(ChatColor.GRAY + "/uversion");
		return true;
		}else{
		sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
		return true;
		}
	}

}
