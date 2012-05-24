package com.modcrafting.ultrabans.commands;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.modcrafting.ultrabans.UltraBan;

public class Jail implements CommandExecutor{

	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
	String permission = "ultraban.jail";
	public World world;
	public String jworld;
    public double x;
    public int y;
    public double z;
    public int yaw;
    public int pitch;
    public Location setlp;
    public Jail(UltraBan ultraBan) {
			this.plugin = ultraBan;
		}
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
		boolean auth = false;
		Player player = null;
		String admin = config.getString("defAdminName", "server");
		String reason = config.getString("defReason", "not sure");
		boolean broadcast = true;
		if (sender instanceof Player){
			player = (Player)sender;
			if(player.hasPermission(permission) || player.isOp()) auth = true;
			admin = player.getName();
		}else{
			auth = true;
		}
		if(auth){
			if (args.length < 1) return false;
			if(args[0].equalsIgnoreCase("setjail")){
				this.setlp = player.getLocation();
				plugin.jail.setJail(setlp, "jail");
				sender.sendMessage(ChatColor.GRAY + "Jail has been set!");
				return true;
			}
			if(args[0].equalsIgnoreCase("setrelease")){
				this.setlp = player.getLocation();
				plugin.jail.setJail(setlp, "release");
				sender.sendMessage(ChatColor.GRAY + "Release has been set!");
				return true;
			}
			String p = args[0];
			if(plugin.autoComplete) p = plugin.util.expandName(p);
			
			if(args.length > 1){
				if(args[1].equalsIgnoreCase("-s")){
					broadcast = false;
					reason = plugin.util.combineSplit(2, args, " ");
				}else{
					if(args[1].equalsIgnoreCase("-a")){
						admin = config.getString("defAdminName", "server");
						reason = plugin.util.combineSplit(2, args, " ");
					}else{
					reason = plugin.util.combineSplit(1, args, " ");
					}
				}
			}
			
			Player victim = plugin.getServer().getPlayer(p);
			if(victim == null){
				if(plugin.jailed.contains(p)){
					sender.sendMessage(ChatColor.GRAY + p + " is already in jail.");
					return true;
				}else{
					sender.sendMessage(ChatColor.GRAY + p + " was not found to be jailed.");
				}
				sender.sendMessage(ChatColor.GRAY + "Player Must be online to be jailed.");
				return true;
			}else{
				if(victim.getName() == admin){
					sender.sendMessage(ChatColor.RED + "You cannot jail yourself!");
					return true;
				}
				if(victim.hasPermission( "ultraban.override.jail")){
					sender.sendMessage(ChatColor.RED + "Your jail attempt has been denied! Player Notified!");
					victim.sendMessage(ChatColor.RED + "Player: " + admin + " Attempted to jail you!");
					return true;
				}
				if(plugin.jailed.contains(victim.getName().toLowerCase())){
					sender.sendMessage(ChatColor.GRAY + victim.getName() + " is already in jail.");
					return true;
				}else{
					sender.sendMessage(ChatColor.GRAY + victim.getName() + " was not found to be jailed.");
				}
				
				String adminMsgAll = config.getString("messages.jailMsgBroadcast", "%victim% was jailed by %admin%. Reason: %reason%");
				if(adminMsgAll.contains(plugin.regexAdmin)) adminMsgAll = adminMsgAll.replaceAll(plugin.regexAdmin, admin);
				if(adminMsgAll.contains(plugin.regexReason)) adminMsgAll = adminMsgAll.replaceAll(plugin.regexReason, reason);
				if(adminMsgAll.contains(plugin.regexVictim)) adminMsgAll = adminMsgAll.replaceAll(plugin.regexVictim, p);

				String jailMsgVictim = config.getString("messages.jailMsgVictim", "You have been jailed by %admin%. Reason: %reason%!");
				if(jailMsgVictim.contains(plugin.regexAdmin)) jailMsgVictim = jailMsgVictim.replaceAll(plugin.regexAdmin, admin);
				if(jailMsgVictim.contains(plugin.regexVictim)) jailMsgVictim = jailMsgVictim.replaceAll(plugin.regexVictim, p);
				
				if(broadcast & adminMsgAll != null){
					plugin.getServer().broadcastMessage(plugin.util.formatMessage(adminMsgAll));
				}else{
					if(jailMsgVictim != null) victim.sendMessage(plugin.util.formatMessage(jailMsgVictim));
					sender.sendMessage(ChatColor.ITALIC + "Silent: " + plugin.util.formatMessage(adminMsgAll));
				}
				plugin.db.addPlayer(p, reason, admin, 0, 6);
				plugin.jailed.add(p.toLowerCase());
				Location stlp = plugin.jail.getJail("jail");
				victim.teleport(stlp);
				return true;
			}
		}
		return false;
	}

}

        
