package com.modcrafting.ultrabans.commands;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;
import com.modcrafting.ultrabans.db.SQLDatabases;

public class Starve implements CommandExecutor{
	public static final Logger log = Logger.getLogger("Minecraft");
	SQLDatabases db;
	UltraBan plugin;
	String permission = "ultraban.starve";
	public Starve(UltraBan ultraBan) {
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
		boolean auth = false;
		Player player = null;
		String admin = config.getString("defAdminName", "server");
		if (sender instanceof Player){
			player = (Player)sender;
			if(player.hasPermission(permission) || player.isOp()) auth = true;
			admin = player.getName();
		}else{
			auth = true;
		}
		if(auth){
			if (args.length < 1) return false;
			String p = args[0];
			if(autoComplete)
				p = expandName(p); 
			Player victim = plugin.getServer().getPlayer(p);
			String idoit = null;
			if (victim != null){
				idoit = victim.getName();
			}else{
				sender.sendMessage(ChatColor.GRAY + "Player must be online!");
				return true;
			}
			String starveMsgVictim = config.getString("messages.starveMsgVictim", "You are now starving!");
			starveMsgVictim = starveMsgVictim.replaceAll("%admin%", admin);
			starveMsgVictim = starveMsgVictim.replaceAll("%victim%", idoit);
			victim.sendMessage(formatMessage(starveMsgVictim));
			String starveMsgBroadcast = config.getString("messages.starveMsgBroadcast", "%victim% is now starving!");
			starveMsgBroadcast = starveMsgBroadcast.replaceAll("%admin%", admin);
			starveMsgBroadcast = starveMsgBroadcast.replaceAll("%victim%", idoit);
			sender.sendMessage(formatMessage(starveMsgBroadcast));
			int st = 0;
			victim.setFoodLevel(st);
		}
		
		return true;
	}
	public String formatMessage(String str){
		String funnyChar = new Character((char) 167).toString();
		str = str.replaceAll("&", funnyChar);
		return str;
	}
}
