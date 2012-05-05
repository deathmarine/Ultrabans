package com.modcrafting.ultrabans.commands;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;

public class CheckIP implements CommandExecutor{
	public static final Logger log = Logger.getLogger("Minecraft");
	String permission = "ultraban.checkip";
	UltraBan plugin;
	
	public CheckIP(UltraBan ultraBan) {
		this.plugin = ultraBan;
	
	}
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		boolean auth = false;
		Player player = null;
		if (sender instanceof Player){
			player = (Player)sender;
			if(player.hasPermission(permission) || player.isOp()) auth = true;
		}else{
			auth = true; //if sender is not a player - Console
		}
		if (!auth){
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}else{
		if (args.length < 1) return false;
		String p = args[0];
		String ip = plugin.db.getAddress(p.toLowerCase());
		InetAddress InetP;
		if(ip == null){
			plugin.db.setAddress(player.getName().toLowerCase(), player.getAddress().getAddress().getHostAddress());
		}
		try {
			InetP = InetAddress.getByName(ip);
			sender.sendMessage(ChatColor.YELLOW + "IP Address: " + ip);
			sender.sendMessage(ChatColor.YELLOW + "Host Address: " + InetP.getHostAddress());
			sender.sendMessage(ChatColor.YELLOW + "Host Name: " + InetP.getHostName());
			sender.sendMessage(ChatColor.YELLOW + "Connection: " + InetP.getCanonicalHostName());
			
			try {
				boolean ping = InetP.isReachable(config.getInt("HostTimeOut", 1800));
				if (ping){
					sender.sendMessage(ChatColor.YELLOW + "Ping Test Passed.");
				}else{
					sender.sendMessage(ChatColor.RED + "Ping Test Failed.");
				}
			} catch (IOException e) {
				sender.sendMessage(ChatColor.RED + "Ping Test Failed.");
			}
		} catch (UnknownHostException e) {
			sender.sendMessage(ChatColor.RED + "Gathering Information Failed: Recommend Kick!");
		}
		
		return true;
		}
	}
}
