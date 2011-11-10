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

public class Unban implements CommandExecutor{
	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	public Unban(UltraBan ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean autoComplete;
	public String expandName(String p) {
		int m = 0;
		String Result = "";
		for (int n = 0; n < plugin.getServer().getOnlinePlayers().length; n++) {
			String str = plugin.getServer().getOnlinePlayers()[n].getName();
			if (str.matches("(?i).*" + p + ".*")) {
				m++;
				Result = str;
				if(m==2) {
					return null;
				}
			}
			if (str.equalsIgnoreCase(p))
				return str;
		}
		if (m == 1)
			return Result;
		if (m > 1) {
			return null;
		}
		if (m < 1) {
			return p;
		}
		return p;
	}
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		boolean auth = false;
		Player player = null;
		String admin = "server";
		
		if (sender instanceof Player){
			player = (Player)sender;
			if (Permissions.Security.permission(player, "ultraban.unban")){
				auth = true;
			}else{
			 if (player.isOp()) auth = true; //defaulting to Op if no permissions or node
			}
			admin = player.getName();
		}else{
			auth = true; //if sender is not a player - Console
		}
		if (!auth){
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}

		// Has enough arguments?
		if (args.length < 1)return false;
		String p = args[0];

		if(plugin.bannedPlayers.remove(p.toLowerCase())){
			plugin.db.removeFromBanlist(p);
			plugin.db.addPlayer(p, "Unbanned", admin, 0, 5);
			if(plugin.tempBans.containsKey(p.toLowerCase()))
				plugin.tempBans.remove(p.toLowerCase());
			String ip = plugin.db.getAddress(p);
			if(plugin.bannedIPs.contains(ip)){
				plugin.bannedIPs.remove(ip);
				System.out.println("Also removed the IP ban!");
			}
			log.log(Level.INFO, "[UltraBan] " + admin + " unbanned player " + p + ".");
			plugin.getServer().broadcastMessage(ChatColor.BLUE + p + ChatColor.GRAY + " was unbanned by " + 
					ChatColor.DARK_GRAY + admin + "!");
			return true;
		}else{
			sender.sendMessage(ChatColor.BLUE + p +  ChatColor.GRAY + " is already unbanned!");
			return true;
		}
	}
}
