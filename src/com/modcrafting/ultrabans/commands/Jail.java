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

public class Jail extends CommandHandler{
	public Jail(Ultrabans instance) {
		super(instance);
	}

	public String command(final CommandSender sender, final Command command, final String[] args) {
		if (args.length < 1) 
			return lang.getString("Jail.Arguments");
		if(args[0].equalsIgnoreCase("setjail")){
			if(!(sender instanceof Player))
				return lang.getString("Jail.SetFail");
			plugin.jail.setJail(((Player) sender).getLocation(), "jail");
			return lang.getString("Jail.SetJail");
		}
		if(args[0].equalsIgnoreCase("setrelease")){
			if(!(sender instanceof Player))
				return lang.getString("Jail.SetFail");
			plugin.jail.setJail(((Player) sender).getLocation(), "release");
			return lang.getString("Jail.SetRelease");
		}
		boolean broadcast = true;
		String admin = Ultrabans.DEFAULT_ADMIN;
		String reason = Ultrabans.DEFAULT_REASON;
		if (sender instanceof Player)
			admin = sender.getName();
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
		if(name.equalsIgnoreCase(admin))
			return lang.getString("Jail.Emo");
		if(plugin.jailed.containsKey(name.toLowerCase())){
			String msg = lang.getString("Jail.Failed");
			if(msg.contains(Ultrabans.VICTIM)) 
				msg = msg.replace(Ultrabans.VICTIM, name);
			return msg;
		}
		OfflinePlayer victim = plugin.getServer().getOfflinePlayer(name);
		if(victim !=null){
			name = victim.getName();
			if(victim.isOnline()){
				if(victim.getPlayer().hasPermission("ultraban.override.jail"))
					return lang.getString("Jail.Denied");
				String msgvic = lang.getString("Jail.MsgToVictim");
				if(msgvic.contains(Ultrabans.ADMIN)) 
					msgvic = msgvic.replace(Ultrabans.ADMIN, admin);
				if(msgvic.contains(Ultrabans.REASON)) 
					msgvic = msgvic.replace(Ultrabans.REASON, reason);
				victim.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', msgvic));
				victim.getPlayer().teleport(plugin.jail.getJail("jail"));
			} 
		}		
		String bcmsg = lang.getString("Jail.MsgToBroadcast");
		if(bcmsg.contains(Ultrabans.ADMIN)) 
			bcmsg = bcmsg.replace(Ultrabans.ADMIN, admin);
		if(bcmsg.contains(Ultrabans.REASON))
			bcmsg = bcmsg.replace(Ultrabans.REASON, reason);
		if(bcmsg.contains(Ultrabans.VICTIM)) 
			bcmsg = bcmsg.replace(Ultrabans.VICTIM, name);
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

        
