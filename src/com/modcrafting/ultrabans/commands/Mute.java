/* COPYRIGHT (c) 2012 Joshua McCurry
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License
 * and use of this software or its code is an agreement to this license.
 * A full copy of this license can be found at
 * http://creativecommons.org/licenses/by-nc-sa/3.0/. 
 */
package com.modcrafting.ultrabans.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;

public class Mute implements CommandExecutor {
	UltraBan plugin;
	public Mute(UltraBan ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		Player player = null;
		String admin = config.getString("defAdminName", "server");
		if (sender instanceof Player){
			player = (Player)sender;
			admin = player.getName();
		}
		if(!sender.hasPermission(command.getPermission())){
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
			plugin.getLogger().info(admin + " muted player " + p + ".");
			return true;
		}else{
			sender.sendMessage(ChatColor.RED + "Player must be online!");
			return true;
		}		
	}

}
