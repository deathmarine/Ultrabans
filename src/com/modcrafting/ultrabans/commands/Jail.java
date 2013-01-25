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
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.Ultrabans;
import com.modcrafting.ultrabans.util.Formatting;

public class Jail implements CommandExecutor{
	Ultrabans plugin;
    public Jail(Ultrabans ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(final CommandSender sender, Command command, String label, final String[] args) {
		if(!sender.hasPermission(command.getPermission())){
			sender.sendMessage(ChatColor.RED+Ultrabans.DEFAULT_DENY_MESSAGE);
			return true;
		}

		Bukkit.getScheduler().scheduleSyncDelayedTask(Ultrabans.getPlugin(),new Runnable(){
			@Override
			public void run() {
		    	YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
				boolean broadcast = true;
				Player player = null;
				String admin = Ultrabans.DEFAULT_ADMIN;
				String reason = Ultrabans.DEFAULT_REASON;
				if (sender instanceof Player){
					player = (Player)sender;
					admin = player.getName();
				}
				if (args.length < 1) return;
				if(args[0].equalsIgnoreCase("setjail")){
					if(player==null) return;
					plugin.jail.setJail(player.getLocation(), "jail");
					String msg = config.getString("Messages.Jail.SetJail","Jail has been set!");
					msg=Formatting.formatMessage(msg);
					sender.sendMessage(ChatColor.GRAY + msg);
					return;
				}
				if(args[0].equalsIgnoreCase("setrelease")){
					if(player==null) return;
					plugin.jail.setJail(player.getLocation(), "release");
					String msg = config.getString("Messages.Jail.SetRelease","Release has been set!");
					msg=Formatting.formatMessage(msg);
					sender.sendMessage(ChatColor.GRAY + msg);
					return;
				}
				String p = args[0];
				p = Formatting.expandName(p);
				if(args.length > 1){
					if(args[1].equalsIgnoreCase("-s")){
						broadcast = false;
						reason = Formatting.combineSplit(2, args, " ");
					}else{
						if(args[1].equalsIgnoreCase("-a")){
							admin = Ultrabans.DEFAULT_ADMIN;
							reason = Formatting.combineSplit(2, args, " ");
						}else{
						reason = Formatting.combineSplit(1, args, " ");
						}
					}
				}
				Player victim = plugin.getServer().getPlayer(p);
				if(victim == null){
					if(plugin.jailed.contains(p)){
						String msg = config.getString("Messages.Jail.Failed","%victim% is already in jail.");
						if(msg.contains(Ultrabans.VICTIM)) msg = msg.replaceAll(Ultrabans.VICTIM, p);
						msg=Formatting.formatMessage(msg);
						sender.sendMessage(ChatColor.GRAY+msg);
						return;
					}
					String msg = config.getString("Messages.Jail.Online","%victim% must be online to be jailed.");
					if(msg.contains(Ultrabans.VICTIM)) msg = msg.replaceAll(Ultrabans.VICTIM, p);
					msg=Formatting.formatMessage(msg);
					sender.sendMessage(ChatColor.GRAY+msg);
					return;
				}else{
					if(victim.getName().equalsIgnoreCase(admin)){
						String bcmsg = config.getString("Messages.Jail.Emo","You cannot jail yourself!");
						bcmsg = Formatting.formatMessage(bcmsg);
						sender.sendMessage(bcmsg);
						return;
					}
					if(victim.hasPermission( "ultraban.override.jail")&&!admin.equalsIgnoreCase(Ultrabans.DEFAULT_ADMIN)){
						String bcmsg = config.getString("Messages.Jail.Denied","Your jail attempt has been denied!");
						bcmsg = Formatting.formatMessage(bcmsg);
						sender.sendMessage(bcmsg);
						return;
					}
					if(plugin.jailed.contains(victim.getName().toLowerCase())){
						String msg = config.getString("Messages.Jail.Failed","%victim% is already in jail.");
						if(msg.contains(Ultrabans.VICTIM)) msg = msg.replaceAll(Ultrabans.VICTIM, victim.getName());
						msg=Formatting.formatMessage(msg);
						sender.sendMessage(ChatColor.GRAY+msg);
						return;
					}
					String msgvic = config.getString("Messages.Jail.MsgToVictim", "You have been jailed by %admin%. Reason: %reason%");
					if(msgvic.contains(Ultrabans.ADMIN)) msgvic = msgvic.replaceAll(Ultrabans.ADMIN, admin);
					if(msgvic.contains(Ultrabans.REASON)) msgvic = msgvic.replaceAll(Ultrabans.REASON, reason);
					msgvic=Formatting.formatMessage(msgvic);
					victim.sendMessage(msgvic);
					
					String bcmsg = config.getString("Messages.Jail.MsgToBroadcast","%victim% was jailed by %admin%. Reason: %reason%!");
					if(bcmsg.contains(Ultrabans.ADMIN)) bcmsg = bcmsg.replaceAll(Ultrabans.ADMIN, admin);
					if(bcmsg.contains(Ultrabans.REASON)) bcmsg = bcmsg.replaceAll(Ultrabans.REASON, reason);
					if(bcmsg.contains(Ultrabans.VICTIM)) bcmsg = bcmsg.replaceAll(Ultrabans.VICTIM, victim.getName());
					bcmsg=Formatting.formatMessage(bcmsg);

					if(broadcast){
						plugin.getServer().broadcastMessage(bcmsg);
					}else{
						sender.sendMessage(ChatColor.ITALIC + "Silent: " + bcmsg);
					}
					plugin.getUBDatabase().addPlayer(p, reason, admin, 0, 6);
					plugin.jailed.add(p.toLowerCase());
					Location stlp = plugin.jail.getJail("jail");
					victim.teleport(stlp);
				}
			}
		});
		return true;
	}

}

        
