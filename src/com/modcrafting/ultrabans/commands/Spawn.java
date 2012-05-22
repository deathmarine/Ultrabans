package com.modcrafting.ultrabans.commands;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;
import com.modcrafting.ultrabans.db.SQLDatabases;

public class Spawn implements CommandExecutor{
	public static final Logger log = Logger.getLogger("Minecraft");
	SQLDatabases db;
	UltraBan plugin;
	public boolean autoComplete;
	String permission = "ultraban.spawn";
	public Spawn(UltraBan ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		boolean auth = false;
		Player player = null;
		String admin = config.getString("defAdminName", "server");
		autoComplete = config.getBoolean("auto-complete", true);
		if (sender instanceof Player){
			player = (Player)sender;
			if(player.hasPermission(permission) || player.isOp()) auth = true;
			admin = player.getName();
		}else{
			auth = true; //if sender is not a player - Console
		}
		if(auth){
			if (args.length < 1) return false;
			String p = args[0]; //type name correct or 
			if(autoComplete) p = plugin.util.expandName(p); 
			Player victim = plugin.getServer().getPlayer(p);
			String idoit = null;
			if (victim != null){
				idoit = victim.getName();
			}else{
				sender.sendMessage(ChatColor.GRAY + "Player must be online!");
				return true;
			}
			String fspawnMsgVictim = config.getString("messages.fspawnMsgVictim", "You have been sent to spawn!");
			fspawnMsgVictim = fspawnMsgVictim.replaceAll("%admin%", admin);
			fspawnMsgVictim = fspawnMsgVictim.replaceAll("%victim%", idoit);
			victim.sendMessage(plugin.util.formatMessage(fspawnMsgVictim));
			String fspawnMsgBroadcast = config.getString("messages.fspawnMsgBroadcast", "%victim% is now at spawn!");
			fspawnMsgBroadcast = fspawnMsgBroadcast.replaceAll("%admin%", admin);
			fspawnMsgBroadcast = fspawnMsgBroadcast.replaceAll("%victim%", idoit);
			sender.sendMessage(plugin.util.formatMessage(fspawnMsgBroadcast));
				//Further Research	
				World wtlp = victim.getWorld();
				Location tlp = wtlp.getSpawnLocation();
				victim.teleport(tlp);
		}
		
		return true;
	}
}
