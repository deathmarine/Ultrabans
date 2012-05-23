package com.modcrafting.ultrabans.commands;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;

public class Kick implements CommandExecutor{
	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	String permission = "ultraban.kick";
	public Kick(UltraBan ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		boolean auth = false;
		boolean broadcast = true;
		Player player = null;
		String admin = config.getString("defAdminName", "server");
		String reason = config.getString("defReason", "not sure");
		if (sender instanceof Player){
			player = (Player)sender;
			if(player.hasPermission(permission) || player.isOp()) auth = true;
			admin = player.getName();
		}else{
			auth = true;
		}
		if (!auth){
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}
		if (args.length < 1) return false;

		String p = args[0];
		if(plugin.autoComplete)	p = plugin.util.expandName(p);
		
		if(args.length > 1){
			if(args[1].equalsIgnoreCase("-s")){
				broadcast = false;
				reason = plugin.util.combineSplit(2, args, " ");
			}else{
				if(args[1].equalsIgnoreCase("-a")){
					admin = config.getString("defAdminName", "server");
					reason = plugin.util.combineSplit(2, args, " ");
				}else{
				reason = plugin.util.combineSplit(1, args, " ");
				}
			}
		}

		if(p.equals("*")){
			if (sender instanceof Player)
				if(player.hasPermission("ultrabans.kick.all") || player.isOp()) auth = true;
			log.log(Level.INFO, "[UltraBan] " + admin + " kicked Everyone Reason: " + reason);
			Player[] pl = plugin.getServer().getOnlinePlayers();
			for (int i=0; i<pl.length; i++){
				if (pl[i] != player || !pl[i].hasPermission("ultraban.override.kick.all")){
				String adminMsg = config.getString("messages.kickAllMsg", "Everyone has been kicked by %admin%. Reason: %reason%");
				if(adminMsg.contains(plugin.regexAdmin)) adminMsg = adminMsg.replaceAll(plugin.regexAdmin, admin);
				if(adminMsg.contains(plugin.regexReason)) adminMsg = adminMsg.replaceAll(plugin.regexReason, reason);
				pl[i].kickPlayer(plugin.util.formatMessage(adminMsg));
				}
			}
			return true;
		}
		if(plugin.autoComplete) p = plugin.util.expandName(p);
		
		Player victim = plugin.getServer().getPlayer(p);
		if(victim == null){
			sender.sendMessage(ChatColor.GRAY + "Player must be online!");
			return true;
		}
		if(victim.getName() == admin){
			sender.sendMessage(ChatColor.RED + "You cannot kick yourself!");
			return true;
		}
		if(victim.hasPermission( "ultraban.override.kick")){
			sender.sendMessage(ChatColor.RED + "Your kick has been denied! Player Notified!");
			victim.sendMessage(ChatColor.RED + "Player:" + admin + " Attempted to kick you!");
			return true;
		}
		
		
		String kickMsgBroadcast = config.getString("messages.kickMsgBroadcast");
		if(kickMsgBroadcast.contains(plugin.regexAdmin)) kickMsgBroadcast = kickMsgBroadcast.replaceAll(plugin.regexAdmin, admin);
		if(kickMsgBroadcast.contains(plugin.regexVictim)) kickMsgBroadcast = kickMsgBroadcast.replaceAll(plugin.regexVictim, victim.getName());
		if(kickMsgBroadcast.contains(plugin.regexReason)) kickMsgBroadcast = kickMsgBroadcast.replaceAll(plugin.regexReason, reason);
		if(kickMsgBroadcast != null){
			if(broadcast){
				plugin.getServer().broadcastMessage(plugin.util.formatMessage(kickMsgBroadcast));
			}else{
				sender.sendMessage(plugin.util.formatMessage(ChatColor.ITALIC + kickMsgBroadcast));
			}
		}
		
		String adminMsg = config.getString("messages.kickMsgVictim", "You have been kicked by %admin%. Reason: %reason%");
		if(adminMsg.contains(plugin.regexAdmin)) adminMsg = adminMsg.replaceAll(plugin.regexAdmin, admin);
		if(adminMsg.contains(plugin.regexReason)) adminMsg = adminMsg.replaceAll(plugin.regexReason, reason);
		victim.kickPlayer(plugin.util.formatMessage(adminMsg));		
		
		plugin.db.addPlayer(victim.getName(), reason, admin, 0, 3);
		log.log(Level.INFO, "[UltraBan] " + admin + " kicked player " + victim.getName() + ". Reason: " + reason);
		return true;
	}
}
