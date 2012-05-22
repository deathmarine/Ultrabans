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

public class Mute implements CommandExecutor {
	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	String permission = "ultraban.mute";
	public Mute(UltraBan ultraBan) {
		this.plugin = ultraBan;
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
			auth = true; //if sender is not a player - Console
		}
		if (!auth){
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}
		
		if (args.length < 1) return false;
		
		String p = plugin.util.expandName(args[0]); 
		Player victim = plugin.getServer().getPlayer(p);
		if(victim != null){

			if(victim.getName() == admin){
				sender.sendMessage(ChatColor.RED + "You cannot emomute yourself!");
				return true;
			}
			if(victim.hasPermission( "ultraban.override.mute")){
				sender.sendMessage(ChatColor.RED + "Your mute has been denied! Player Notified!");
				victim.sendMessage(ChatColor.RED + "Player:" + player.getName() + " Attempted to mute you!");
				return true;
			}
			if (plugin.muted.contains(p.toLowerCase())){
				plugin.muted.remove(p.toLowerCase());
				String adminMsg = config.getString("messages.unmuteMsgVictim", "You have been unmuted.");
		 		victim.sendMessage(ChatColor.GRAY + adminMsg);
				String adminMsgs = config.getString("messages.unmuteMsg", "You have unmuted %victim%.");
				adminMsgs = adminMsgs.replaceAll("%victim%", p);
		 		sender.sendMessage(ChatColor.GRAY + adminMsgs);
				return true;
			}
			plugin.muted.add(p.toLowerCase());
			String adminMsg = config.getString("messages.muteChatMsg", "You have been muted.");
	 		victim.sendMessage(ChatColor.GRAY + adminMsg);
			String adminMsgs = config.getString("messages.muteMsg", "You have muted %victim%.");
			adminMsgs = adminMsgs.replaceAll("%victim%", p);
	 		sender.sendMessage(ChatColor.GRAY + adminMsgs);
			log.log(Level.INFO, "[UltraBan] " + admin + " muted player " + p + ".");
			plugin.db.addPlayer(p, "Muted", admin, 0, 7);
			return true;
		}else{
			sender.sendMessage(ChatColor.GRAY + "Player must be online!");
			return true;
		}		
	}

}
