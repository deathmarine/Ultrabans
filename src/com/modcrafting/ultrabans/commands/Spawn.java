package com.modcrafting.ultrabans.commands;

import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;
import com.modcrafting.ultrabans.db.SQLDatabases;
import com.nijikokun.bukkit.Permissions.Permissions;

public class Spawn implements CommandExecutor{
	public static final Logger log = Logger.getLogger("Minecraft");
	SQLDatabases db;
	UltraBan plugin;
	public boolean autoComplete;
	public Spawn(UltraBan ultraBan) {
		this.plugin = ultraBan;
	}
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
		if(!plugin.useSpawn) return true;
		boolean auth = false;
		Player player = null;
		String admin = "server";
		autoComplete = config.getBoolean("auto-complete", true);
		if (sender instanceof Player){
			player = (Player)sender;
			if (Permissions.Security.permission(player, "ultraban.spawn")){
				auth = true;
			}else{
			 if (player.isOp()) auth = true; //defaulting to Op if no permissions or node
			}
			admin = player.getName();
		}else{
			auth = true; //if sender is not a player - Console
		}
		if(auth){
			if (args.length < 1) return false;
			String p = args[0]; //type name correct or 
			if(autoComplete) p = expandName(p); 
			Player victim = plugin.getServer().getPlayer(p);
			String idoit = victim.getName();
			String fspawnMsgVictim = config.getString("messages.fspawnMsgVictim", "You have been sent to spawn!");
			fspawnMsgVictim = fspawnMsgVictim.replaceAll("%admin%", admin);
			fspawnMsgVictim = fspawnMsgVictim.replaceAll("%victim%", idoit);
			victim.sendMessage(formatMessage(fspawnMsgVictim));
			String fspawnMsgBroadcast = config.getString("messages.fspawnMsgBroadcast", "%victim% is now at spawn!");
			fspawnMsgBroadcast = fspawnMsgBroadcast.replaceAll("%admin%", admin);
			fspawnMsgBroadcast = fspawnMsgBroadcast.replaceAll("%victim%", idoit);
			sender.sendMessage(formatMessage(fspawnMsgBroadcast));
				//Further Research	
				World wtlp = victim.getWorld();
				Location tlp = wtlp.getSpawnLocation();
				victim.teleport(tlp);
		}
		
		return true;
	}
	public String formatMessage(String str){
		String funnyChar = new Character((char) 167).toString();
		str = str.replaceAll("&", funnyChar);
		return str;
	}
}
