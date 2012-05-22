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

public class Lockdown implements CommandExecutor {
	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	String permission = "ultraban.lockdown";
	public Lockdown(UltraBan ultraBan) {
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
		if (!auth){
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}
		// Using the config considering the server may restart.
		boolean lock = config.getBoolean("lockdown", false);
		if (args.length < 1) return false;
		String toggle = args[0];
		if (toggle.equalsIgnoreCase("on")){ 
			if(!lock){ 
				lockdownOn();
				sender.sendMessage(ChatColor.GRAY + "Lockdown initiated.PlayerLogin disabled.");
				UltraBan.log.log(Level.INFO, admin + " initiated lockdown.");
			}
			if(lock) sender.sendMessage(ChatColor.GRAY + "Lockdown already initiated.PlayerLogin disabled.");
			return true;
		}
		if (toggle.equalsIgnoreCase("off")){
			if(lock){
				lockdownEnd();
				sender.sendMessage(ChatColor.GRAY + "Lockdown ended.PlayerLogin reenabled.");
				UltraBan.log.log(Level.INFO, admin + " disabled lockdown.");
			}
			if(!lock) sender.sendMessage(ChatColor.GRAY + "Lockdown already ended / never initiated.");
			return true;
		}
		if (toggle.equalsIgnoreCase("status")){
			if(lock) sender.sendMessage(ChatColor.GRAY + "Lockdown in progress.PlayerLogin disabled.");
			if(!lock) sender. sendMessage(ChatColor.GRAY + "Lockdown is not in progress.");
			return true;
		}
		
		return false;
	}
	public void lockdownOn() {
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
        config.set("lockdown", (boolean) true);
        plugin.saveConfig();

    }
	public void lockdownEnd() {
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
        config.set("lockdown", (boolean) false);
        plugin.saveConfig();

    }

}
