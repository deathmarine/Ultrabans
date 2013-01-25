/* COPYRIGHT (c) 2012 Joshua McCurry
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License
 * and use of this software or its code is an agreement to this license.
 * A full copy of this license can be found at
 * http://creativecommons.org/licenses/by-nc-sa/3.0/. 
 */
package com.modcrafting.ultrabans.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.Ultrabans;
import com.modcrafting.ultrabans.util.BanType;
import com.modcrafting.ultrabans.util.Formatting;

public class Warn implements CommandExecutor{
	Ultrabans plugin;
	public Warn(Ultrabans ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		String admin=Ultrabans.DEFAULT_ADMIN;
		String reason=Ultrabans.DEFAULT_REASON;
		boolean broadcast = true;
		Player player = null;
		if (sender instanceof Player){
			player = (Player)sender;
			admin = player.getName();
		}
		if(!sender.hasPermission(command.getPermission())){
			sender.sendMessage(ChatColor.RED+Ultrabans.DEFAULT_DENY_MESSAGE);
			return true;
		}
		if (args.length < 1) return false;
		String p = args[0];
		p = Formatting.expandName(p);
		Player victim = plugin.getServer().getPlayer(p);
		if(args.length > 1){
			if(args[1].equalsIgnoreCase("-s")){
				broadcast = false;
				reason = Formatting.combineSplit(2, args, " ");
			}else
				reason = Formatting.combineSplit(1, args, " ");
		}
		if(victim != null){
			if(victim.getName().equalsIgnoreCase(admin)){
				String bcmsg = config.getString("Messages.Warn.Emo", "You cannot warn yourself!");
				bcmsg = Formatting.formatMessage(bcmsg);
				sender.sendMessage(ChatColor.RED + bcmsg);
				return true;
			}
			if(victim.hasPermission("ultraban.override.warn")&&!admin.equalsIgnoreCase(Ultrabans.DEFAULT_ADMIN)){
				String bcmsg = config.getString("Messages.Denied.Emo", "Your warning has been denied!");
				bcmsg = Formatting.formatMessage(bcmsg);
				sender.sendMessage(ChatColor.RED+bcmsg);
				return true;
			}
			//Max Warning System
			if(config.getBoolean("MaxWarning.Enable", false)){
				Integer max = config.getInt("MaxWarning.Amt", 5);
				String idoit = victim.getName();
				if(plugin.getUBDatabase().maxWarns(idoit) != null && plugin.getUBDatabase().maxWarns(idoit).size() >= max){
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
			bcmsg = bcmsg.replaceAll(Ultrabans.ADMIN, admin);
			bcmsg = bcmsg.replaceAll(Ultrabans.REASON, reason);
			bcmsg = bcmsg.replaceAll(Ultrabans.VICTIM, victim.getName());
			bcmsg = Formatting.formatMessage(bcmsg);
			if(broadcast){ 
				plugin.getServer().broadcastMessage(bcmsg);
			}else{
				String wMsgV = config.getString("Messages.Warn.MsgToVictim", "You have been warned by %admin%. Reason: %reason%");
				wMsgV = wMsgV.replaceAll(Ultrabans.ADMIN, admin);
				wMsgV = wMsgV.replaceAll(Ultrabans.REASON, reason);
				wMsgV = Formatting.formatMessage(wMsgV);
				sender.sendMessage(ChatColor.ITALIC + "Silent: " + bcmsg);
				victim.sendMessage(wMsgV);
			}
			final String fname = victim.getName();
			final String freason = reason;
			final String fadmin = admin;
			Bukkit.getScheduler().runTaskAsynchronously(Ultrabans.getPlugin(),new Runnable(){
				@Override
				public void run() {
					Ultrabans.getPlugin().getUBDatabase().addPlayer(fname, freason, fadmin, 0, BanType.WARN.getId());
				}	
			});
			if(plugin.getLog())
				plugin.getLogger().info(bcmsg);
		}else{
			//Offline Warning
			victim = plugin.getServer().getOfflinePlayer(p).getPlayer();
			if(victim != null){
				if(victim.hasPermission("ultraban.override.warn")&&!admin.equalsIgnoreCase(Ultrabans.DEFAULT_ADMIN)){
					sender.sendMessage(ChatColor.RED + "Your warning has been denied!");
					return true;
				}
			}
			String bcmsg = config.getString("Messages.Warn.MsgToBroadcast", "%victim% was warned by %admin%. Reason: %reason%");
			bcmsg = bcmsg.replaceAll(Ultrabans.ADMIN, admin);
			bcmsg = bcmsg.replaceAll(Ultrabans.REASON, reason);
			bcmsg = bcmsg.replaceAll(Ultrabans.VICTIM, p);
			bcmsg = Formatting.formatMessage(bcmsg);
			if(broadcast){ 
				plugin.getServer().broadcastMessage(bcmsg);
			}else{
				sender.sendMessage(ChatColor.ITALIC+"Silent: "+bcmsg);
			}
			final String fname = p;
			final String freason = reason;
			final String fadmin = admin;
			Bukkit.getScheduler().runTaskAsynchronously(Ultrabans.getPlugin(),new Runnable(){
				@Override
				public void run() {
					Ultrabans.getPlugin().getUBDatabase().addPlayer(fname, freason, fadmin, 0, BanType.WARN.getId());
				}	
			});
			if(plugin.getLog())
				plugin.getLogger().info(bcmsg);
		}
		return true;
	}
}
