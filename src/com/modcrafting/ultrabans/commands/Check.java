package com.modcrafting.ultrabans.commands;

import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;

public class Check implements CommandExecutor{
	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	String permission = "utlraban.check";
	
	public Check(UltraBan ultraBan) {
		this.plugin = ultraBan;
	
	}private String banType(int num){
		switch(num){
		case 0: return "B";
		case 1: return "IP";
		case 2: return "W";
		case 3: return "K";
		case 4: return "F";
		case 5: return "UN";
		case 6: return "J";
		case 7: return "M";
		case 9: return "PB";
		default: return "?";
		}
	}
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		boolean auth = false;
		Player player = null;
		if (sender instanceof Player){
			player = (Player)sender;
			if(player.hasPermission(permission) || player.isOp()) auth = true;
		}else{
			auth = true;
		}
		if (!auth){
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}else{
		if (args.length < 1) return false;
		String p = args[0];
		
		
		List<EditBan> bans = plugin.db.listRecords(p, sender);
		if(bans.isEmpty()){
			sender.sendMessage(ChatColor.GREEN + "No records");
			return true;
		}
		String ip = plugin.db.getAddress(p);
		sender.sendMessage(ChatColor.BLUE + "Found " + bans.size() + " records for user " + bans.get(0).name + " on IP " + ip + " :");
		for(EditBan ban : bans){
			sender.sendMessage(ChatColor.RED + banType(ban.type) + ChatColor.GRAY + ban.id + ": " + ChatColor.GREEN + ban.reason + ChatColor.AQUA +" by " + ban.admin);
		}
		return true;
		}
	}
}
