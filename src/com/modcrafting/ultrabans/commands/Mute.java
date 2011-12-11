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
	public Mute(UltraBan ultraBan) {
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
		String admin = "server";
		if (sender instanceof Player){
			player = (Player)sender;
			 			//new permissions test before reconstruct
			if (plugin.setupPermissions()){
				if (plugin.permission.has(player, "ultraban.mute")) auth = true;
			}else{
			if (player.isOp()) auth = true; //defaulting to Op if no vault doesn't take or node
			}
			
			admin = player.getName();
		}else{
			auth = true; //if sender is not a player - Console
		}
		if (!auth){
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}
		
		if (args.length < 1) return false;
		
		if(args[0].equalsIgnoreCase("enable")){
			String p = expandName(args[1]); 
			p = expandName(p);
			Player victim = plugin.getServer().getPlayer(p);
			if(victim != null){
				plugin.muted.remove(p);
				String adminMsg = config.getString("messages.unmuteMsgVictim", "You have been unmuted.");
		 		victim.sendMessage(ChatColor.GRAY + adminMsg);
				String adminMsgs = config.getString("messages.unmuteMsg", "You have unmuted %victim%.");
				adminMsgs = adminMsgs.replaceAll("%victim%", p);
		 		sender.sendMessage(ChatColor.GRAY + adminMsgs);
				return true;
			}else{
				sender.sendMessage(ChatColor.GRAY + "Player must be online!");
				return true;
			}
	}
		 
		String p = expandName(args[0]); 
		Player victim = plugin.getServer().getPlayer(p);
		if(victim != null){
			plugin.muted.add(p);
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
