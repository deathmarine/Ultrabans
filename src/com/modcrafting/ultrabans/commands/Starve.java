/* COPYRIGHT (c) 2012 Joshua McCurry
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License
 * and use of this software or its code is an agreement to this license.
 * A full copy of this license can be found at
 * http://creativecommons.org/licenses/by-nc-sa/3.0/. 
 */
package com.modcrafting.ultrabans.commands;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;

public class Starve implements CommandExecutor{
	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	String permission = "ultraban.starve";
	public Starve(UltraBan ultraBan) {
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
		if(auth){
			if (args.length < 1) return false;
			String p = args[0];
			if(plugin.autoComplete)
				p = plugin.util.expandName(p); 
			Player victim = plugin.getServer().getPlayer(p);
			String idoit = null;
			if (victim != null){
				idoit = victim.getName();
				if(victim.getName() == admin){
					sender.sendMessage(ChatColor.RED + "You cannot starve yourself!");
					return true;
				}
				if(victim.hasPermission( "ultraban.override.starve")){
					sender.sendMessage(ChatColor.RED + "Your starve attempt has been denied! Player Notified!");
					victim.sendMessage(ChatColor.RED + "Player: " + player.getName() + " Attempted to starve you!");
					return true;
				}
			}else{
				sender.sendMessage(ChatColor.GRAY + "Player must be online!");
				return true;
			}
			String starveMsgVictim = config.getString("messages.starveMsgVictim");
			if(starveMsgVictim.contains(plugin.regexAdmin)) starveMsgVictim = starveMsgVictim.replaceAll(plugin.regexAdmin, admin);
			if(starveMsgVictim.contains(plugin.regexVictim)) starveMsgVictim = starveMsgVictim.replaceAll(plugin.regexVictim, idoit);
			if(starveMsgVictim != null) victim.sendMessage(plugin.util.formatMessage(starveMsgVictim));
			
			String starveMsgBroadcast = config.getString("messages.starveMsgBroadcast");
			if(starveMsgBroadcast.contains(plugin.regexAdmin)) starveMsgBroadcast = starveMsgBroadcast.replaceAll(plugin.regexAdmin, admin);
			if(starveMsgBroadcast.contains(plugin.regexVictim)) starveMsgBroadcast = starveMsgBroadcast.replaceAll(plugin.regexVictim, idoit);
			if(starveMsgBroadcast != null) sender.sendMessage(plugin.util.formatMessage(starveMsgBroadcast));
			victim.setFoodLevel(0);
		}
		
		return true;
	}
}
