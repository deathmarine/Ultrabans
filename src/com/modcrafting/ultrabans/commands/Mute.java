/* COPYRIGHT (c) 2013 Deathmarine (Joshua McCurry)
 * This file is part of Ultrabans.
 * Ultrabans is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Ultrabans is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Ultrabans.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.modcrafting.ultrabans.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.modcrafting.ultrabans.Ultrabans;
import com.modcrafting.ultrabans.util.Formatting;

public class Mute extends CommandHandler{
	public Mute(Ultrabans instance) {
		super(instance);
	}
	
	public String command(CommandSender sender, Command command, String[] args) {
		if (args.length < 1) 
			return lang.getString("Mute.Arguments");
		String admin = Ultrabans.DEFAULT_ADMIN;
		String reason = Ultrabans.DEFAULT_REASON;
		if (sender instanceof Player)
			admin = sender.getName();
		String name = Formatting.expandName(args[0]);
		if (args.length > 1) {
			reason = Formatting.combineSplit(1, args);
		}
		if(name.equalsIgnoreCase(admin))
			return lang.getString("Mute.Emo");
		Player victim = plugin.getServer().getPlayer(name);
		if(victim != null){
			if(victim.hasPermission("ultraban.override.mute"))
				return lang.getString("Messages.Mute.Denied");
			if (plugin.muted.contains(name.toLowerCase())){
				plugin.muted.remove(name.toLowerCase());
		 		victim.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.getString("Mute.UnmuteMsgToVictim")));
				String adminMsgs = lang.getString("Messages.Mute.UnmuteMsgToSender","You have unmuted %victim%.");
				if(adminMsgs.contains(Ultrabans.VICTIM)) 
					adminMsgs = adminMsgs.replace(Ultrabans.VICTIM, name);
				return adminMsgs;
			}
			plugin.getAPI().mutePlayer(name, reason, admin);
	 		victim.sendMessage(ChatColor.translateAlternateColorCodes('&', lang.getString("Mute.MuteMsgToVictim")));
	 		String adminMsgs = ChatColor.translateAlternateColorCodes('&', lang.getString("Mute.MuteMsgToSender"));
			if(adminMsgs.contains(Ultrabans.VICTIM)) 
				adminMsgs = adminMsgs.replace(Ultrabans.VICTIM, name);
			plugin.getLogger().info(ChatColor.stripColor(adminMsgs));
			return adminMsgs;
		}
		if (plugin.muted.contains(name.toLowerCase())){
			plugin.muted.remove(name.toLowerCase());
			String adminMsgs = lang.getString("Mute.UnmuteMsgToSender");
			if(adminMsgs.contains(Ultrabans.VICTIM)) 
				adminMsgs = adminMsgs.replace(Ultrabans.VICTIM, name);
	 		return adminMsgs;
		}				
		return lang.getString("Mute.Failed");	
	}

}
