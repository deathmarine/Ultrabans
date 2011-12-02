package com.modcrafting.ultrabans.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.modcrafting.ultrabans.UltraBan;

public class Help implements CommandExecutor{
	UltraBan plugin;
	
	public Help(UltraBan ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		boolean auth = false;
		boolean server = false;
		Player player = null;
		if (sender instanceof Player){
			player = (Player)sender;
			if (plugin.setupPermissions()){
				if (plugin.permission.has(player, "ultraban.help")) auth = true;
			}else{
			 if (player.isOp()) auth = true; //defaulting to Op if no vault doesn't take or node
			}
		}else{
			auth = true;
			server = true;
		}
		if (server){
			sender.sendMessage("Required Info {} Optional () Silent -s");
			sender.sendMessage("/ban        {player} (-s) {reason}");
			sender.sendMessage("/permaban   {player} (-s) {reason}");		
			sender.sendMessage("/tempban    {player} (-s) {amt} {sec/min/hour/day} {Reason}");
			sender.sendMessage("/ipban      {player} (-s) {reason}");
			sender.sendMessage("/unban      {player}");
			sender.sendMessage("/checkban   {player}");
			sender.sendMessage("/kick       {player} (-s) {reason}");
			sender.sendMessage("/warn       {player} (-s) {reason}");
			sender.sendMessage("/fine       {player} (-s) {amt}");
			sender.sendMessage("/empty      {player}");
			sender.sendMessage("/forcespawn {player}");
			sender.sendMessage("/starve     {player}");
			sender.sendMessage("/rules      (help)");
			sender.sendMessage("/editban    (help)");
			sender.sendMessage("/lockdown   {on/off/status}");
			sender.sendMessage("/jail (set/pardon/{player}) {player}");
			sender.sendMessage("/uhelp /exportbans /importbans /ureload /uversion");
			return true;
		}
		if (auth) {
			if (plugin.setupPermissions()){
		sender.sendMessage("Ultrabans " + ChatColor.BLUE + "Required Info {}" + ChatColor.GREEN + " Optional ()" + ChatColor.RED + " Silent -s");
		if (plugin.permission.has(player, "ultraban.ban")) sender.sendMessage(ChatColor.GRAY + "/ban       {player} (-s) {reason}");
		if (plugin.permission.has(player, "ultraban.permaban"))sender.sendMessage(ChatColor.GRAY + "/permaban       {player} (-s) {reason}");		
		if (plugin.permission.has(player, "ultraban.tempban"))sender.sendMessage(ChatColor.GRAY + "/tempban  {player} (-s) {amt} {sec/min/hour/day} {Reason}");
		if (plugin.permission.has(player, "ultraban.ipban"))sender.sendMessage(ChatColor.GRAY + "/ipban     {player} (-s) {reason}");
		if (plugin.permission.has(player, "ultraban.unban"))sender.sendMessage(ChatColor.GRAY + "/unban    {player}");
		sender.sendMessage(ChatColor.GRAY + "/checkban {player}");
		if (plugin.permission.has(player, "ultraban.kick"))sender.sendMessage(ChatColor.GRAY + "/kick       {player} (-s) {reason}");
		if (plugin.permission.has(player, "ultraban.warn"))sender.sendMessage(ChatColor.GRAY + "/warn     {player} (-s) {reason}");
		if (plugin.permission.has(player, "ultraban.fine"))sender.sendMessage(ChatColor.GRAY + "/fine     {player} (-s) {amt}");
		if (plugin.permission.has(player, "ultraban.emtpy"))sender.sendMessage(ChatColor.GRAY + "/empty    {player}");
		if (plugin.permission.has(player, "ultraban.spawn"))sender.sendMessage(ChatColor.GRAY + "/forcespawn {player}");
		if (plugin.permission.has(player, "ultraban.starve"))sender.sendMessage(ChatColor.GRAY + "/starve     {player}");
		if (plugin.permission.has(player, "ultraban.rules"))sender.sendMessage(ChatColor.GRAY + "/rules     (help)");
		if (plugin.permission.has(player, "ultraban.editban"))sender.sendMessage(ChatColor.GRAY + "/editban  (help)");
		if (plugin.permission.has(player, "ultraban.jail"))sender.sendMessage(ChatColor.GRAY + "/jail (set/pardon/{player}) {player}");
		if (plugin.permission.has(player, "ultraban.lockdown"))sender.sendMessage(ChatColor.GRAY + "/lockdown   {on/off/status}");
		if (plugin.permission.has(player, "ultraban.admin")) sender.sendMessage(ChatColor.GRAY + "/uhelp /exportbans /ureload /uversion /importbans");
		return true;
			}
		}else{
		sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
		return true;
		}
		return false;
			
	}

}
