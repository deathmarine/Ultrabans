package com.modcrafting.ultrabans.commands;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;

public class Empty implements CommandExecutor{
	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	
	public Empty(UltraBan ultraBan) {
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
    	YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
    	if(!plugin.useEmpty) return true;
    	boolean auth = false;
		Player player = null;
		String admin = "server";
		if (sender instanceof Player){
			player = (Player)sender;
			if (plugin.setupPermissions()){
				if (plugin.permission.has(player, "ultraban.empty")) auth = true;
			}else{
			 if (player.isOp()) auth = true; //defaulting to Op if no vault doesn't take or node
			}
			admin = player.getName();
		}else{
			auth = true; //if sender is not a player - Console
		}
		if(auth){
			if (args.length < 1) return false;
			String p = args[0];
			if(autoComplete) p = expandName(p); 
			Player victim = plugin.getServer().getPlayer(p);
			String idoit = victim.getName();
			String emptyMsg = config.getString("messages.emptyMsgVictim", "%admin% has cleared your inventory!'");
			emptyMsg = emptyMsg.replaceAll("%admin%", admin);
			emptyMsg = emptyMsg.replaceAll("%victim%", idoit);
			sender.sendMessage(formatMessage(emptyMsg));
			String empyMsgAll = config.getString("messages.emptyMsgBroadcast", "%admin% has cleared the inventory of %victim%!");
			empyMsgAll = empyMsgAll.replaceAll("%admin%", admin);
			empyMsgAll = empyMsgAll.replaceAll("%victim%", idoit);
			victim.sendMessage(formatMessage(empyMsgAll));
			victim.getInventory().clear();
			return true;
			
		}else{
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
			}
		
	}
	public String formatMessage(String str){
		String funnyChar = new Character((char) 167).toString();
		str = str.replaceAll("&", funnyChar);
		return str;
	}

}
