/* COPYRIGHT (c) 2012 Joshua McCurry
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License
 * and use of this software or its code is an agreement to this license.
 * A full copy of this license can be found at
 * http://creativecommons.org/licenses/by-nc-sa/3.0/. 
 */
package com.modcrafting.ultrabans.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.Ultrabans;

public class Pardon implements CommandExecutor{
	Ultrabans plugin;
    public Pardon(Ultrabans ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!sender.hasPermission(command.getPermission())){
			sender.sendMessage(ChatColor.RED+plugin.perms);
			return true;
		}
    	YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		Player player = null;
		String admin = plugin.admin;
		if (sender instanceof Player){
			player = (Player)sender;
			admin = player.getName();
		}
		if (args.length < 1) return false;
		String p = args[0];
		p = plugin.util.expandName(p);
		Player victim = plugin.getServer().getPlayer(p);
		if(victim == null){
			victim = plugin.getServer().getOfflinePlayer(p).getPlayer();
			if(victim == null){
				if(plugin.jailed.contains(p.toLowerCase())||plugin.tempJail.containsKey(p.toLowerCase())){

					if(plugin.jailed.contains(p.toLowerCase()))plugin.jailed.remove(p.toLowerCase());
					plugin.db.removeFromJaillist(p);
					plugin.db.addPlayer(p, "Released From Jail", admin, 0, 8);
					if(plugin.tempJail.containsKey(p.toLowerCase()))plugin.tempJail.remove(p.toLowerCase());
					String bcmsg = config.getString("Messages.Pardon.Msg","%victim% was released from jail by %admin%!");
					if(bcmsg.contains(plugin.regexAdmin)) bcmsg = bcmsg.replaceAll(plugin.regexAdmin, admin);
					if(bcmsg.contains(plugin.regexVictim)) bcmsg = bcmsg.replaceAll(plugin.regexVictim, p);
					bcmsg=plugin.util.formatMessage(bcmsg);
					sender.sendMessage(bcmsg);
				}else{
					//TODO Make config Fail.
					sender.sendMessage("Player not found.");
				}
				return true;
			}
		}
		plugin.db.removeFromJaillist(victim.getName());
		plugin.db.addPlayer(victim.getName(), "Released From Jail", admin, 0, 8);
		if(plugin.jailed.contains(victim.getName().toLowerCase())){
			plugin.jailed.remove(victim.getName().toLowerCase());
		}
		if(plugin.tempJail.containsKey(victim.getName().toLowerCase())){
			plugin.tempJail.remove(victim.getName().toLowerCase());
		}
		String bcmsg = config.getString("Messages.Pardon.Msg","%victim% was released from jail by %admin%!");
		if(bcmsg.contains(plugin.regexAdmin)) bcmsg = bcmsg.replaceAll(plugin.regexAdmin, admin);
		if(bcmsg.contains(plugin.regexVictim)) bcmsg = bcmsg.replaceAll(plugin.regexVictim, victim.getName());
		bcmsg=plugin.util.formatMessage(bcmsg);
		victim.sendMessage(bcmsg);
		sender.sendMessage(bcmsg);
		Location stlp = plugin.jail.getJail("release");
		if(stlp != null){
			victim.teleport(stlp);
		}else{
			victim.teleport(victim.getWorld().getSpawnLocation());
		}
		return true;
	}
}
