package com.modcrafting.ultrabans.commands;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;

public class Empty implements CommandExecutor{
	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	String permission = "ultraban.empty";
	public Empty(UltraBan ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
    	YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
    	boolean auth = false;
		Player player = null;
		String admin = config.getString("defAdminName", "server");
		if (sender instanceof Player){
			player = (Player)sender;
			if(player.hasPermission(permission) || player.isOp()) auth = true;
			admin = player.getName();
		}else{
			auth = true; //if sender is not a player - Console
		}
		if(auth){
			if (args.length < 1) return false;
			String p = args[0];
			if(plugin.autoComplete) p = plugin.util.expandName(p); 
			String idoit = null;
			Player victim = plugin.getServer().getPlayer(p);
			if (victim != null){
				idoit = victim.getName();
			}else{
				sender.sendMessage(ChatColor.GRAY + "Player must be online!");
				return true;
			}
			String emptyMsg = config.getString("messages.emptyMsgVictim", "%admin% has cleared your inventory!'");
			emptyMsg = emptyMsg.replaceAll("%admin%", admin);
			emptyMsg = emptyMsg.replaceAll("%victim%", idoit);
			sender.sendMessage(plugin.util.formatMessage(emptyMsg));
			String empyMsgAll = config.getString("messages.emptyMsgBroadcast", "%admin% has cleared the inventory of %victim%!");
			empyMsgAll = empyMsgAll.replaceAll("%admin%", admin);
			empyMsgAll = empyMsgAll.replaceAll("%victim%", idoit);
			victim.sendMessage(plugin.util.formatMessage(empyMsgAll));
			victim.getInventory().clear();
			return true;
			
		}else{
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
			}
		
	}

}
