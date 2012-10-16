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

import com.modcrafting.ultrabans.Ultrabans;
import com.modcrafting.ultrabans.tracker.Track;

public class Warn implements CommandExecutor{
	Ultrabans plugin;
	public Warn(Ultrabans ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Track.track(command.getName());
    	YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		String admin=plugin.admin;
		String reason=plugin.reason;
		boolean broadcast = true;
		Player player = null;
		if (sender instanceof Player){
			player = (Player)sender;
			admin = player.getName();
		}
		if(!sender.hasPermission(command.getPermission())){
			sender.sendMessage(ChatColor.RED+plugin.perms);
			return true;
		}
		if (args.length < 1) return false;
		String p = args[0];
		p = plugin.util.expandName(p);
		Player victim = plugin.getServer().getPlayer(p);
		if(args.length > 1){
			if(args[1].equalsIgnoreCase("-s")){
				broadcast = false;
				reason = plugin.util.combineSplit(2, args, " ");
			}else
				reason = plugin.util.combineSplit(1, args, " ");
		}
		if(victim != null){
			if(victim.getName().equalsIgnoreCase(admin)){
				String bcmsg = config.getString("Messages.Warn.Emo", "You cannot warn yourself!");
				bcmsg = plugin.util.formatMessage(bcmsg);
				sender.sendMessage(ChatColor.RED + bcmsg);
				return true;
			}
			if(victim.hasPermission("ultraban.override.warn")&&!admin.equalsIgnoreCase(plugin.admin)){
				String bcmsg = config.getString("Messages.Denied.Emo", "Your warning has been denied!");
				bcmsg = plugin.util.formatMessage(bcmsg);
				sender.sendMessage(ChatColor.RED+bcmsg);
				return true;
			}
			//Max Warning System
			if(config.getBoolean("MaxWarning.Enable", false)){
				Integer max = config.getInt("MaxWarning.Amt", 5);
				String idoit = victim.getName();
				if(plugin.db.maxWarns(idoit) != null && plugin.db.maxWarns(idoit).size() >= max){
					String cmd = config.getString("MaxWarning.Result", "ban");
					String r = config.getString("MaxWarning.Reason","Max Warns");
					boolean s = config.getBoolean("MaxWarning.Silent",true);
					StringBuilder sb = new StringBuilder();
					if(cmd.equalsIgnoreCase("ban") || cmd.equalsIgnoreCase("kick") || cmd.equalsIgnoreCase("ipban") || cmd.equalsIgnoreCase("jail") || cmd.equalsIgnoreCase("permaban")){
						sb.append(cmd);
						sb.append(" ");
						sb.append(idoit);
						sb.append(" ");
						if(s){
							sb.append("-s");
							sb.append(" ");
						}
						sb.append(r);
						if(player != null){
							player.getPlayer().performCommand(sb.toString());
						}else{
							plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), sb.toString());
						}
					}else if(cmd.equalsIgnoreCase("tempban") || cmd.equalsIgnoreCase("tempipban") || cmd.equalsIgnoreCase("tempjail")){
						sb.append(cmd);
						sb.append(" ");
						sb.append(idoit);
						sb.append(" ");
						if(s){
							sb.append("-s");
							sb.append(" ");
						}
						sb.append(config.getString("MaxWarning.Temp.Amt", "5"));
						sb.append(" ");
						sb.append(config.getString("MaxWarning.Temp.Mode", "day"));
						sb.append(" ");
						
						sb.append(r);
						if(player != null){
							player.getPlayer().performCommand(sb.toString());
						}else{
							plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), sb.toString());
						}
					}else{
						String fakecmd = "ban" + " " + idoit + " " + "-s" + " " + r;
						if(player != null){
							player.getPlayer().performCommand(fakecmd);
						}else{
							plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), fakecmd);
						}
					}
					return true;
				}	
			}
			
			String bcmsg = config.getString("Messages.Warn.MsgToBroadcast", "%victim% was warned by %admin%. Reason: %reason%");
			bcmsg = bcmsg.replaceAll(plugin.regexAdmin, admin);
			bcmsg = bcmsg.replaceAll(plugin.regexReason, reason);
			bcmsg = bcmsg.replaceAll(plugin.regexVictim, victim.getName());
			bcmsg = plugin.util.formatMessage(bcmsg);
			if(broadcast){ 
				plugin.getServer().broadcastMessage(bcmsg);
			}else{
				String wMsgV = config.getString("Messages.Warn.MsgToVictim", "You have been warned by %admin%. Reason: %reason%");
				wMsgV = wMsgV.replaceAll(plugin.regexAdmin, admin);
				wMsgV = wMsgV.replaceAll(plugin.regexReason, reason);
				wMsgV = plugin.util.formatMessage(wMsgV);
				sender.sendMessage(ChatColor.ITALIC + "Silent: " + bcmsg);
				victim.sendMessage(wMsgV);
			}	
			plugin.db.addPlayer(victim.getName(), reason, admin, 0, 2);
			plugin.getLogger().info(bcmsg);
		}else{
			//Offline Warning
			victim = plugin.getServer().getOfflinePlayer(p).getPlayer();
			if(victim != null){
				if(victim.hasPermission("ultraban.override.warn")&&!admin.equalsIgnoreCase(plugin.admin)){
					sender.sendMessage(ChatColor.RED + "Your warning has been denied!");
					return true;
				}
			}
			String bcmsg = config.getString("Messages.Warn.MsgToBroadcast", "%victim% was warned by %admin%. Reason: %reason%");
			bcmsg = bcmsg.replaceAll(plugin.regexAdmin, admin);
			bcmsg = bcmsg.replaceAll(plugin.regexReason, reason);
			bcmsg = bcmsg.replaceAll(plugin.regexVictim, p);
			bcmsg = plugin.util.formatMessage(bcmsg);
			if(broadcast){ 
				plugin.getServer().broadcastMessage(bcmsg);
			}else{
				sender.sendMessage(ChatColor.ITALIC+"Silent: "+bcmsg);
			}
			plugin.db.addPlayer(p, reason, admin, 0, 2);
			plugin.getLogger().info(bcmsg);
		}
		return true;
	}
}
