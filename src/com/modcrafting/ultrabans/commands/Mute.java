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
			auth = true;
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
				sender.sendMessage(ChatColor.RED + "You cannot mute yourself!");
				return true;
			}
			if(victim.hasPermission( "ultraban.override.mute")){
				sender.sendMessage(ChatColor.RED + "Your mute has been denied! Player Notified!");
				victim.sendMessage(ChatColor.RED + "Player: " + player.getName() + " Attempted to mute you!");
				return true;
			}
			
			if (plugin.muted.contains(p.toLowerCase())){
				plugin.muted.remove(p.toLowerCase());
				
				String adminMsg = config.getString("messages.unmuteMsgVictim");
		 		if(adminMsg != null) victim.sendMessage(plugin.util.formatMessage(adminMsg));
		 		
				String adminMsgs = config.getString("messages.unmuteMsg");
				if(adminMsgs.contains(plugin.regexVictim)) adminMsgs = adminMsgs.replaceAll(plugin.regexVictim, p);
		 		if(adminMsgs != null) sender.sendMessage(plugin.util.formatMessage(adminMsgs));
				return true;
			}
			plugin.muted.add(p.toLowerCase());
			
			String adminMsg = config.getString("messages.muteChatMsg");
	 		if(adminMsg != null) victim.sendMessage(plugin.util.formatMessage(adminMsg));
	 		
			String adminMsgs = config.getString("messages.muteMsg");
			if(adminMsgs.contains(plugin.regexVictim)) adminMsgs = adminMsgs.replaceAll(plugin.regexVictim, p);
	 		sender.sendMessage(plugin.util.formatMessage(adminMsgs));

			plugin.db.addPlayer(p, "Muted", admin, 0, 7);
			
			log.log(Level.INFO, "[UltraBan] " + admin + " muted player " + p + ".");
			return true;
		}else{
			sender.sendMessage(ChatColor.RED + "Player must be online!");
			return true;
		}		
	}

}
