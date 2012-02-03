package com.modcrafting.ultrabans.commands;

import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;

public class History implements CommandExecutor{
	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	
	public History(UltraBan ultraBan) {
		this.plugin = ultraBan;
	
	}private String banType(int num){
		switch(num){
		case 0: return "Ban  ";
		case 1: return "IPBan";
		case 2: return "Warn ";
		case 3: return "Kick ";
		case 4: return "Fine ";
		case 5: return "Unban";
		case 6: return "Jail ";
		case 7: return "Mute ";
		case 9: return "PERMA";
		default: return "?";
		}
	}
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		boolean auth = false;
		Player player = null;
		if (sender instanceof Player){
			player = (Player)sender;
			if (plugin.setupPermissions()){
				if (plugin.permission.has(player, "ultraban.history")) auth = true;
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
		
		
		List<EditBan> bans = plugin.db.listRecent(p);
		if(bans.isEmpty()){
			sender.sendMessage(ChatColor.GREEN + "Error in command");
			return true;
		}
		sender.sendMessage(ChatColor.BLUE + "Ultrabans Listing " + ChatColor.GRAY + args[0] + ChatColor.BLUE + " records.");
		for(EditBan ban : bans){
			Long ltime = this.parseTimeSpec(Long.toString(ban.time), "hour");
			String time = Long.toString(ltime);
			sender.sendMessage(ChatColor.RED + ban.name + ": " + banType(ban.type) + ChatColor.GRAY + " by " + ban.admin + " on " + time + " for " + ban.reason);
		}
		return true;
		}
	}
	public long parseTimeSpec(String time, String unit) {
		long sec;
		try {
			sec = Integer.parseInt(time)*60;
		} catch (NumberFormatException ex) {
			return 0;
		}
		if (unit.startsWith("hour"))
			sec *= 60;
		else if (unit.startsWith("day"))
			sec *= (60*24);
		else if (unit.startsWith("week"))
			sec *= (7*60*24);
		else if (unit.startsWith("month"))
			sec *= (30*60*24);
		else if (unit.startsWith("min"))
			sec *= 1;
		else if (unit.startsWith("sec"))
			sec /= 60;
		return sec;
	}
}
