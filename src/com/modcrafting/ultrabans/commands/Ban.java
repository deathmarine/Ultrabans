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
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.modcrafting.ultrabans.Ultrabans;
import com.modcrafting.ultrabans.util.Formatting;

public class Ban extends CommandHandler {
	public Ban(Ultrabans instance) {
		super(instance);
	}

	public String command(CommandSender sender, Command command, String[] args) {
		if (args.length < 1)
			return lang.getString("Ban.Arguments");
		boolean broadcast = true;
		String admin = Ultrabans.DEFAULT_ADMIN;
		String reason = Ultrabans.DEFAULT_REASON;
		if (sender instanceof Player)
			admin = sender.getName();
		String name = Formatting.expandName(args[0]);
		if(name.equalsIgnoreCase(admin))
			return lang.getString("Ban.Emo");
		if (args.length > 1) {
			if (args[1].equalsIgnoreCase("-s")){
				if(	sender.hasPermission(command.getPermission() + ".silent"))
					broadcast = false;
				reason = Formatting.combineSplit(2, args);	
			} else if (args[1].equalsIgnoreCase("-a")){
				if(sender.hasPermission(command.getPermission() + ".anon"))
					admin = Ultrabans.DEFAULT_ADMIN;
				reason = Formatting.combineSplit(2, args);	
			} else {
				reason = Formatting.combineSplit(1, args);
			}
		}
		if(plugin.bannedPlayers.containsKey(name.toLowerCase())){
			String failed = lang.getString("Ban.Failed");
			if(failed.contains(Ultrabans.VICTIM))
				failed = failed.replace(Ultrabans.VICTIM, name);
			return failed;
		}
		OfflinePlayer victim = plugin.getServer().getOfflinePlayer(name);
		if(victim != null){
			if(victim.isOnline()){
				if(victim.getPlayer().hasPermission("ultraban.override.ban") &&
						!admin.equalsIgnoreCase(Ultrabans.ADMIN))
					return lang.getString("Ban.Denied");
				String vicmsg = lang.getString("Ban.MsgToVictim");
				if(vicmsg.contains(Ultrabans.ADMIN))
					vicmsg = vicmsg.replace(Ultrabans.ADMIN, admin);
				if(vicmsg.contains(Ultrabans.REASON)) 
					vicmsg = vicmsg.replace(Ultrabans.REASON, reason);
				victim.getPlayer().kickPlayer(ChatColor.translateAlternateColorCodes('&', vicmsg));
			}
			name = victim.getName();
		}
		plugin.getAPI().banPlayer(name, reason, admin);
		String bcmsg = ChatColor.translateAlternateColorCodes('&', lang.getString("Ban.MsgToBroadcast"));
		if(bcmsg.contains(Ultrabans.ADMIN)) 
			bcmsg = bcmsg.replace(Ultrabans.ADMIN, admin);
		if(bcmsg.contains(Ultrabans.REASON)) 
			bcmsg = bcmsg.replace(Ultrabans.REASON, reason);
		if(bcmsg.contains(Ultrabans.VICTIM)) 
			bcmsg = bcmsg.replace(Ultrabans.VICTIM, name);
		if(config.getBoolean("CleanOnBan")) 
			Formatting.deletePlyrdat(name);
		if(config.getBoolean("ClearWarnOnBan",false)) 
			plugin.getAPI().clearWarn(name);
		if(broadcast){
			plugin.getServer().broadcastMessage(bcmsg);
		}else{
			sender.sendMessage(ChatColor.ITALIC + "Silent: " + bcmsg);
		}
		if(plugin.getLog())
			plugin.getLogger().info(ChatColor.stripColor(bcmsg));
		return null;
	}
}
