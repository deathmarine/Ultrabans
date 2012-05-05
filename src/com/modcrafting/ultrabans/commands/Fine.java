package com.modcrafting.ultrabans.commands;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import com.modcrafting.ultrabans.UltraBan;

public class Fine implements CommandExecutor{
	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	String permission = "ultraban.fine";
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
		boolean anon = false;
		Player player = null;
		String admin = config.getString("defAdminName", "server");
		if (sender instanceof Player){
			player = (Player)sender;
			if(player.hasPermission(permission) || player.isOp()) auth = true;
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
				amt = combineSplit(2, args, " ");
			}else{
				if(args[1].equalsIgnoreCase("-a")){
					anon = true;
					amt = combineSplit(2, args, " ");
				}else{
				amt = combineSplit(1, args, " ");
				}
			}
		}
		if (anon){
			admin = config.getString("defAdminName", "server");
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
			if(plugin.setupEconomy()){
				double bal = plugin.economy.getBalance(p);
				double amtd = Double.valueOf(amt.trim());
				int max = config.getInt("maxFineAmt", 0);
				if (max == 0){
					if(amtd > bal){
						plugin.economy.withdrawPlayer(victim.getName(), bal);	
					}else{
						plugin.economy.withdrawPlayer(victim.getName(), amtd);
					}
				}else{
					double maxd = Double.valueOf(Integer.toString(max).trim());
					if(maxd > amtd){
						sender.sendMessage(ChatColor.RED + "Max Allowable Fine: " + String.valueOf(max));
					}else{
						if(amtd > bal){
							plugin.economy.withdrawPlayer(victim.getName(), bal);	
						}else{
							plugin.economy.withdrawPlayer(victim.getName(), amtd);
						}
					}
					
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
	
	public String formatMessage(String str){
			String funnyChar = new Character((char) 167).toString();
			str = str.replaceAll("&", funnyChar);
			return str;
	}
}
