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

public class Kick extends CommandHandler{
	public Kick(Ultrabans instance) {
		super(instance);
	}

	public String command(CommandSender sender, Command command, String[] args) {
		if (args.length < 1) 
			return lang.getString("Kick.Arguments");
		String admin = Ultrabans.DEFAULT_ADMIN;
		String reason = Ultrabans.DEFAULT_REASON;
		boolean broadcast = true;
		if (sender instanceof Player)
			admin = sender.getName();
		if((args[0].equals("*") || args[0].equals("all")) && sender.hasPermission("ultrabans.kick.all")){
			if(args.length > 1)
				reason = Formatting.combineSplit(1, args);
			String adminMsg = ChatColor.translateAlternateColorCodes('&', lang.getString("Kick.MsgToAll"));
			if(adminMsg.contains(Ultrabans.ADMIN))
				adminMsg = adminMsg.replace(Ultrabans.ADMIN, admin);
			if(adminMsg.contains(Ultrabans.REASON)) 
				adminMsg = adminMsg.replace(Ultrabans.REASON, reason);
			for (Player players:plugin.getServer().getOnlinePlayers()){
				if (!players.hasPermission("ultraban.override.kick.all")){
					players.kickPlayer(adminMsg);
				}
			}
			if(plugin.getLog())
				plugin.getLogger().info(ChatColor.stripColor(adminMsg));
			return adminMsg;
		}
		String name = Formatting.expandName(args[0]);
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
		Player victim = plugin.getServer().getPlayer(name);
		if(victim == null)
			return lang.getString("Kick.Online");
		if(victim.getName().equalsIgnoreCase(admin))
			return lang.getString("Kick.Emo");
		if(victim.hasPermission("ultraban.override.kick"))
			return lang.getString("Kick.Denied");
		plugin.getAPI().kickPlayer(name, reason, admin);
		String msgvic = ChatColor.translateAlternateColorCodes('&', lang.getString("Kick.MsgToVictim"));
		if(msgvic.contains(Ultrabans.ADMIN)) 
			msgvic = msgvic.replace(Ultrabans.ADMIN, admin);
		if(msgvic.contains(Ultrabans.REASON)) 
			msgvic = msgvic.replace(Ultrabans.REASON, reason);
		victim.kickPlayer(msgvic);
		String bcmsg = ChatColor.translateAlternateColorCodes('&', lang.getString("Kick.MsgToBroadcast"));
		if(bcmsg.contains(Ultrabans.ADMIN)) 
			bcmsg = bcmsg.replace(Ultrabans.ADMIN, admin);
		if(bcmsg.contains(Ultrabans.REASON)) 
			bcmsg = bcmsg.replace(Ultrabans.REASON, reason);
		if(bcmsg.contains(Ultrabans.VICTIM)) 
			bcmsg = bcmsg.replace(Ultrabans.VICTIM, victim.getName());
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
