package com.modcrafting.ultrabans.commands;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.modcrafting.ultrabans.UltraBan;
import com.nijikokun.bukkit.Permissions.Permissions;

public class Fine implements CommandExecutor{
	public net.milkbowl.vault.economy.Economy economy = null;
	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	public Fine(UltraBan ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean autoComplete;
	public String expandName(String p) {
		int m = 0;
		String Result = "";
		for (int n = 0; n < plugin.getServer().getOnlinePlayers().length; n++) {
			String str = plugin.getServer().getOnlinePlayers()[n].getName();
			if (str.matches("(?i).*" + p + ".*")) {
				m++;
				Result = str;
				if(m==2) {
					return null;
				}
			}
			if (str.equalsIgnoreCase(p))
				return str;
		}
		if (m == 1)
			return Result;
		if (m > 1) {
			return null;
		}
		if (m < 1) {
			return p;
		}
		return p;
	}
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		boolean auth = false;
		Player player = null;
		String admin = "server";
		String perms = "ultraban.fine";
		if (sender instanceof Player){
			player = (Player)sender;
			 			//new permissions test before reconstruct
				if (Permissions.Security.permission(player, perms)){
					auth = true;
				}
			
			admin = player.getName();
		}else{
			auth = true; //if sender is not a player - Console
		}
		if (!auth){
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}
		
		if (args.length < 1) return false;
		String p = args[0];
		if(autoComplete) p = expandName(p); 
		Player victim = plugin.getServer().getPlayer(p);
		boolean broadcast = true;
		String amt = args[1]; //set string amount to argument	
			//Going loud
			if(args.length > 1){
				if(args[1].equalsIgnoreCase("-s")){
				broadcast = false;
				amt  = combineSplit(2, args, " ");
				}else{
				amt = combineSplit(1, args, " ");
				}
			}
			
		if(victim != null){
			if(!broadcast){ //If silent wake his ass up
				String fineMsg = config.getString("messages.fineMsgVictim", "You have been fined by %admin% in the amount of %amt%!");
				String idoit = victim.getName();
				fineMsg = fineMsg.replaceAll("%admin%", admin);
				fineMsg = fineMsg.replaceAll("%amt%", amt);
				fineMsg = fineMsg.replaceAll("%victim%", idoit);
				sender.sendMessage(formatMessage(":S:" + fineMsg));
				victim.sendMessage(formatMessage(fineMsg));
			}
			if(setupEconomy()){
				double bal = economy.getBalance(p);
				double amtd = Double.valueOf(amt.trim());
				if(amtd > bal){
					economy.withdrawPlayer(victim.getName(), bal);	
				}else{
					economy.withdrawPlayer(victim.getName(), amtd);
				}
			}
			log.log(Level.INFO, "[UltraBan] " + admin + " fined player " + p + " amount of " + amt + ".");
			plugin.db.addPlayer(p, amt, admin, 0, 4);
			if(broadcast){
				String idoit = victim.getName();
				String fineMsgAll = config.getString("messages.fineMsgBroadcast", "%victim% was fined by %admin% in the amount of %amt%!!");
				fineMsgAll = fineMsgAll.replaceAll("%admin%", admin);
				fineMsgAll = fineMsgAll.replaceAll("%amt%", amt);
				fineMsgAll = fineMsgAll.replaceAll("%victim%", idoit);
				plugin.getServer().broadcastMessage(formatMessage(fineMsgAll));
				return true;
			}
			return true;
		}else{
			sender.sendMessage(ChatColor.GRAY + "Player must be online!");
			return true;
		}		
	}
	public String combineSplit(int startIndex, String[] string, String seperator) {
		StringBuilder builder = new StringBuilder();

		for (int i = startIndex; i < string.length; i++) {
			builder.append(string[i]);
			builder.append(seperator);
		}

		builder.deleteCharAt(builder.length() - seperator.length()); // remove
		return builder.toString();
	}
	public boolean setupEconomy(){
		RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> economyProvider = plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
			if (economyProvider != null) {
				economy = economyProvider.getProvider();
			}
				return (economy != null);
		}
	public String formatMessage(String str){
			String funnyChar = new Character((char) 167).toString();
			str = str.replaceAll("&", funnyChar);
			return str;
	}
}
