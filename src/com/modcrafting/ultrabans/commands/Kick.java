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
		boolean anon = false;
		Player player = null;
		String admin = config.getString("defAdminName", "server");
		if (sender instanceof Player){
			player = (Player)sender;
			if(player.hasPermission(permission) || player.isOp()) auth = true;
			admin = player.getName();
		}else{
			auth = true; //if sender is not a player - Console
		}
		if (!auth){
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}
		// Has enough arguments?
		if (args.length < 1) return false;

		String p = args[0].toLowerCase();
		if(plugin.autoComplete)
			p = plugin.util.expandName(p);
		// Reason stuff
		String reason = config.getString("defReason", "not sure");
		boolean broadcast = true;
		if(args.length > 1){
			if(args[1].equalsIgnoreCase("-s")){
				broadcast = false;
				reason = plugin.util.combineSplit(2, args, " ");
			}else{
				if(args[1].equalsIgnoreCase("-a")){
					anon = true;
					reason = plugin.util.combineSplit(2, args, " ");
				}else{
				reason = plugin.util.combineSplit(1, args, " ");
				}
			}
		}
		if (anon){
			admin = config.getString("defAdminName", "server");
		}

		if(p.equals("*")){
			if (sender instanceof Player)
				if(player.hasPermission("ultrabans.kick.all") || player.isOp()) auth = true;
			log.log(Level.INFO, "[UltraBan] " + admin + " kicked Everyone Reason: " + reason);
			Player[] pl = plugin.getServer().getOnlinePlayers();
			for (int i=0; i<pl.length; i++){
				if (pl[i] != player || pl[i].hasPermission("ultraban.override.kick.all")){
				String adminMsg = config.getString("messages.kickAllMsg", "Everyone has been kicked by %admin%. Reason: %reason%");
				adminMsg = adminMsg.replaceAll("%admin%", admin);
				adminMsg = adminMsg.replaceAll("%reason%", reason);
				pl[i].kickPlayer(plugin.util.formatMessage(adminMsg));
				}
			}
			return true;
		}
		if(plugin.autoComplete)
			p = plugin.util.expandName(p);
		Player victim = plugin.getServer().getPlayer(p);
		if(victim == null){
			sender.sendMessage(ChatColor.GRAY + "Player must be online!");
			return true;
		}

		if(victim.getName() == admin){
			sender.sendMessage(ChatColor.RED + "You cannot emokick yourself!");
			return true;
		}
		if(victim.hasPermission( "ultraban.override.kick")){
			sender.sendMessage(ChatColor.RED + "Your kick has been denied! Player Notified!");
			victim.sendMessage(ChatColor.RED + "Player:" + admin + " Attempted to kick you!");
			return true;
		}
		plugin.db.addPlayer(victim.getName(), reason, admin, 0, 3);
		log.log(Level.INFO, "[UltraBan] " + admin + " kicked player " + victim.getName() + ". Reason: " + reason);
		String adminMsg = config.getString("messages.kickMsgVictim", "You have been kicked by %admin%. Reason: %reason%");
		adminMsg = adminMsg.replaceAll("%admin%", admin);
		adminMsg = adminMsg.replaceAll("%reason%", reason);
		victim.kickPlayer(plugin.util.formatMessage(adminMsg));
	
		if(broadcast){
			String kickMsgBroadcast = config.getString("messages.kickMsgBroadcast", "%victim% has been kicked by %admin%. Reason: %reason%");
			kickMsgBroadcast = kickMsgBroadcast.replaceAll("%admin%", admin);
			kickMsgBroadcast = kickMsgBroadcast.replaceAll("%victim%", victim.getName());
			kickMsgBroadcast = kickMsgBroadcast.replaceAll("%reason%", reason);
			plugin.getServer().broadcastMessage(plugin.util.formatMessage(kickMsgBroadcast));
		}else{
			String kickMsgBroadcast = config.getString("messages.kickMsgBroadcast", "%victim% has been kicked by %admin%. Reason: %reason%");
			kickMsgBroadcast = kickMsgBroadcast.replaceAll("%admin%", admin);
			kickMsgBroadcast = kickMsgBroadcast.replaceAll("%victim%", victim.getName());
			kickMsgBroadcast = kickMsgBroadcast.replaceAll("%reason%", reason);
			sender.sendMessage(plugin.util.formatMessage(":S:" + kickMsgBroadcast));
		}
		return true;
	}
}
