package com.modcrafting.ultrabans.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

import com.modcrafting.ultrabans.UltraBan;
import com.nijikokun.bukkit.Permissions.Permissions;

public class Help implements CommandExecutor{
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
		sender.sendMessage(ChatColor.BLUE + "Required Info {}" + ChatColor.GREEN + " Optional ()" + ChatColor.RED + " Silent -s");
		if (Permissions.Security.permission(player, "ultraban.ban")) sender.sendMessage(ChatColor.GRAY + "/ban       {player} (-s) {reason}");
		if (Permissions.Security.permission(player, "ultraban.permaban")) sender.sendMessage(ChatColor.GRAY + "/permaban       {player} (-s) {reason}");		
		if (Permissions.Security.permission(player, "ultraban.tempban"))sender.sendMessage(ChatColor.GRAY + "/tempban  {player} (-s) {amt} {sec/min/hour/day} {Reason}");
		if (Permissions.Security.permission(player, "ultraban.ipban"))sender.sendMessage(ChatColor.GRAY + "/ipban     {player} (-s) {reason}");
		if (Permissions.Security.permission(player, "ultraban.unban"))sender.sendMessage(ChatColor.GRAY + "/unban    {player}");
		sender.sendMessage(ChatColor.GRAY + "/checkban {player}");
		if (Permissions.Security.permission(player, "ultraban.kick"))sender.sendMessage(ChatColor.GRAY + "/kick       {player} (-s) {reason}");
		if (Permissions.Security.permission(player, "ultraban.warn"))sender.sendMessage(ChatColor.GRAY + "/warn     {player} (-s) {reason}");
		if (Permissions.Security.permission(player, "ultraban.fine"))sender.sendMessage(ChatColor.GRAY + "/fine     {player} (-s) {amt}");
		if (Permissions.Security.permission(player, "ultraban.emtpy"))sender.sendMessage(ChatColor.GRAY + "/empty    {player}");
		if (Permissions.Security.permission(player, "ultraban.spawn"))sender.sendMessage(ChatColor.GRAY + "/forcespawn {player}");
		if (Permissions.Security.permission(player, "ultraban.starve"))sender.sendMessage(ChatColor.GRAY + "/starve     {player}");
		if (Permissions.Security.permission(player, "ultraban.editban"))sender.sendMessage(ChatColor.GRAY + "/editban  (help)");
		if (Permissions.Security.permission(player, "ultraban.jail"))sender.sendMessage(ChatColor.GRAY + "/jail (set/pardon/{player}) {player}");
		if (Permissions.Security.permission(player, "ultraban.admin")) sender.sendMessage(ChatColor.GRAY + "/uhelp /exportbans /ureload /uversion /exportbans");
		return true;
		}else{
		sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
		return true;
		}
	}

}
