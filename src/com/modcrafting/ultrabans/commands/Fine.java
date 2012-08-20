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

public class Fine implements CommandExecutor{
	UltraBan plugin;
	public Fine(UltraBan ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    	YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		boolean broadcast = true;
		String admin = config.getString("defAdminName", "server");
		if (sender instanceof Player){
			admin = sender.getName();
		}
		if(!sender.hasPermission(command.getPermission())){
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}
		if (args.length < 1) return false;
		String p = args[0];
		if(plugin.autoComplete) p = plugin.util.expandName(p); 
		
		
		//Enhanced Variables
		String amt = args[1];
		if(args.length > 1){
			if(args[1].equalsIgnoreCase("-s")){
				broadcast = false;
				amt = plugin.util.combineSplit(2, args, " ");
			}else{
				if(args[1].equalsIgnoreCase("-a")){
					admin = config.getString("defAdminName", "server");
					amt = plugin.util.combineSplit(2, args, " ");
				}else{
					amt = plugin.util.combineSplit(1, args, " ");
				}
			}
		}
		
		//Player Checks
		Player victim = plugin.getServer().getPlayer(p);
		if(victim == null){
			sender.sendMessage(ChatColor.GRAY + "Player must be online!");
			return true;
		}	
		if(victim.getName() == admin){
			sender.sendMessage(ChatColor.RED + "You cannot fine yourself!");
			return true;
		}
		if(victim.hasPermission( "ultraban.override.fine")){
			sender.sendMessage(ChatColor.RED + "Your fine has been denied! Player Notified!");
			victim.sendMessage(ChatColor.RED + "Player: " + admin + " Attempted to fine you!");
			return true;
		}
		
		String idoit = victim.getName();
		//Money
		if(plugin.setupEconomy()){
			double bal = plugin.economy.getBalance(idoit);
			double amtd = Double.valueOf(amt.trim());
			int max = config.getInt("maxFineAmt", 0);
			if(amtd < 0){
				sender.sendMessage(ChatColor.RED + "Error invalid amount!");
				return true;
			}
			if (max == 0){
				if(amtd > bal){
					plugin.economy.withdrawPlayer(idoit, bal);	
				}else{
					plugin.economy.withdrawPlayer(idoit, amtd);
				}
			}else{
				double maxd = Double.valueOf(Integer.toString(max).trim());
				if(maxd > amtd){
					sender.sendMessage(ChatColor.RED + "Max Allowable Fine: " + String.valueOf(max));
				}else{
					if(amtd > bal){
						plugin.economy.withdrawPlayer(idoit, bal);	
					}else{
						plugin.economy.withdrawPlayer(idoit, amtd);
					}
				}
				
			}
		}		
		if(!broadcast){ //If silent wake his ass up
			String fineMsg = config.getString("messages.fineMsgVictim");
			if(fineMsg.contains(plugin.regexAdmin)) fineMsg = fineMsg.replaceAll(plugin.regexAdmin, admin);
			if(fineMsg.contains(plugin.regexAmt)) fineMsg = fineMsg.replaceAll(plugin.regexAmt, amt);
			if(fineMsg.contains(plugin.regexVictim)) fineMsg = fineMsg.replaceAll(plugin.regexVictim, idoit);
			if(fineMsg != null){
				sender.sendMessage(ChatColor.ITALIC + "Silent: " + plugin.util.formatMessage(fineMsg));
				victim.sendMessage(plugin.util.formatMessage(fineMsg));					
			}
		}else{
			String fineMsgAll = config.getString("messages.fineMsgBroadcast", "%victim% was fined by %admin% in the amount of %amt%!!");
			if(fineMsgAll.contains(plugin.regexAdmin)) fineMsgAll = fineMsgAll.replaceAll(plugin.regexAdmin, admin);
			if(fineMsgAll.contains(plugin.regexAmt)) fineMsgAll = fineMsgAll.replaceAll(plugin.regexAmt, amt);
			if(fineMsgAll.contains(plugin.regexVictim)) fineMsgAll = fineMsgAll.replaceAll(plugin.regexVictim, idoit);
			if(fineMsgAll != null) plugin.getServer().broadcastMessage(plugin.util.formatMessage(fineMsgAll));
			return true;
		}
		plugin.getLogger().info(admin + " fined player " + idoit + " amount of " + amt + ".");
		plugin.db.addPlayer(idoit, amt, admin, 0, 4);
		return true;
		
	}
}
