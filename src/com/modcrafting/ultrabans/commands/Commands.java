package com.modcrafting.ultrabans.commands;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.util.config.Configuration;

import com.modcrafting.ultrabans.UltraBan;
import com.modcrafting.ultrabans.db.MySQLDatabase;
import com.modcrafting.ultrabans.util.Util;
import com.nijikokun.bukkit.Permissions.Permissions;

@SuppressWarnings("deprecation")
public class Commands extends UltraBan{
	MySQLDatabase db;
	Util util;
	public Configuration properties = new Configuration(new File("plugins/UltraBan/config.yml"));
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		String commandName = command.getName().toLowerCase();
		//String[] trimmedArgs = args;
		
		if(commandName.equals("ureload")){
			return ureload(sender);
		}
		if(commandName.equals("unban")){
			return unBanPlayer(sender, args);
		}
		if(commandName.equals("ban")){
			return banPlayer(sender, args);
		}
		if(commandName.equals("warn")){
			return warnPlayer(sender, args);
		}
		if(commandName.equals("kick")){
			return kickPlayer(sender, args);
		}
		if(commandName.equals("tempban")){
			return tempbanPlayer(sender, args);
		}
		if(commandName.equals("checkban")){
			return checkBan(sender, args);
		}
		if(commandName.equals("ipban")){
			return ipBan(sender, args);
		}
		if(commandName.equals("fine")){
			return fine(sender, args);
		}
		if(commandName.equals("exportbans")){
			return exportBans(sender);
		}
		if(commandName.equals("uhelp")){
			return uhelp(sender);
		}
		if(commandName.equals("uversion")){
			return uversion(sender);
		}
		if(commandName.equals("empty")){
			return empty(sender, args);
		}
		if(commandName.equals("starve")){
			return starve(sender, args);
		}
		if(commandName.equals("forcespawn")){
			return forcespawn(sender, args);
		}
		
		return false;
	}
	private boolean unBanPlayer(CommandSender sender, String[] args){
		boolean auth = false;
		Player player = null;
		String admin = "server";
		
		if (sender instanceof Player){
			player = (Player)sender;
			if (Permissions.Security.permission(player, "ultraban.unban")){
				auth = true;
			}else{
			 if (player.isOp()) auth = true; //defaulting to Op if no permissions or node
			}
			admin = player.getName();
		}else{
			auth = true; //if sender is not a player - Console
		}
		if (!auth){
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}

		// Has enough arguments?
		if (args.length < 1){
			player.sendMessage(ChatColor.RED + "Command Failed!!");
			return false;
		}

		String p = args[0];

		if(bannedPlayers.remove(p.toLowerCase())){
			db.removeFromBanlist(p);
			db.addPlayer(p, "Unbanned", admin, 0, 5);
			if(tempBans.containsKey(p.toLowerCase()))
				tempBans.remove(p.toLowerCase());
			String ip = db.getAddress(p);
			if(bannedIPs.contains(ip)){
				bannedIPs.remove(ip);
				System.out.println("Also removed the IP ban!");
			}
			log.log(Level.INFO, "[UltraBan] " + admin + " unbanned player " + p + ".");

			properties.load();
			String adminMsg = properties.getNode("messages").getString("unbanMsg");
			String globalMsg = properties.getNode("messages").getString("unbanMsgGlobal");
			adminMsg = adminMsg.replaceAll("%victim%", p);
			globalMsg = globalMsg.replaceAll("%victim%", p);
			globalMsg = globalMsg.replaceAll("%player%", admin);
			//Send a message to unbanner!
			sender.sendMessage(Util.formatMessage(adminMsg));
			//send a message to everyone!
			this.getServer().broadcastMessage(Util.formatMessage(globalMsg));
			return true;
		}else{
			//Unban failed
			properties.load();
			String adminMsg = properties.getNode("messages").getString("unbanMsgFailed");
			adminMsg = adminMsg.replaceAll("%victim%", p);
			sender.sendMessage(Util.formatMessage(adminMsg));
			return true;
		}
	}
	private boolean kickPlayer(CommandSender sender, String[] args){
		boolean auth = false;
		Player player = null;
		String admin = "server";
		if (sender instanceof Player){
			player = (Player)sender;
			if (Permissions.Security.permission(player, "ultraban.kick")){
				auth = true;
			}else{
			 if (player.isOp()) auth = true; //defaulting to Op if no permissions or node
			}
			admin = player.getName();
		}else{
			auth = true; //if sender is not a player - Console
		}
		if (!auth){
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}
		// Has enough arguments?
		if (args.length < 1) return false;

		String p = args[0].toLowerCase();
		// Reason stuff
		String reason = "not sure";
		boolean broadcast = true;
		if(args.length > 1){
			if(args[1].equalsIgnoreCase("-s")){
				broadcast = false;
				reason = Util.combineSplit(2, args, " ");
			}else
				reason = Util.combineSplit(1, args, " ");
		}

		if(p.equals("*")){
			if (sender instanceof Player)
				if (!Permissions.Security.permission(player, "ultraban.kick.all")) return true;

			properties.load();
			String adminMsg = properties.getNode("messages").getString("kickAllMsg");
			adminMsg = adminMsg.replaceAll("%player%", admin);
			adminMsg = adminMsg.replaceAll("%reason%", reason);
			log.log(Level.INFO, "[UltraBan] " + Util.formatMessage(adminMsg));

			// Kick everyone on server
			for (Player pl : this.getServer().getOnlinePlayers()) {
				pl.kickPlayer(Util.formatMessage(adminMsg));
				return true;
			}
		}
		if(autoComplete)
			p = util.expandName(p);
		Player victim = this.getServer().getPlayer(p);
		if(victim == null){
			properties.load();
			String adminMsg = properties.getNode("messages").getString("kickMsgFailed");
			adminMsg = adminMsg.replaceAll("%victim%", p);
			sender.sendMessage(Util.formatMessage(adminMsg));
			return true;
		}

		//Log in console
		log.log(Level.INFO, "[UltraBan] " + admin + " kicked player " + p + ". Reason: " + reason);

		//Send message to victim
		String adminMsg = properties.getNode("messages").getString("kickMsgVictim");
		adminMsg = adminMsg.replaceAll("%player%", admin);
		adminMsg = adminMsg.replaceAll("%reason%", reason);
		victim.kickPlayer(Util.formatMessage(adminMsg));
		db.addPlayer(p, reason, admin, 0, 3);

		if(broadcast){
			//Send message to all players
			String adminMsgAll = properties.getNode("messages").getString("kickMsgBroadcast");
			adminMsgAll = adminMsgAll.replaceAll("%player%", admin);
			adminMsgAll = adminMsgAll.replaceAll("%reason%", reason);
			adminMsgAll = adminMsgAll.replaceAll("%victim%", p);
			this.getServer().broadcastMessage(Util.formatMessage(adminMsgAll));
		}
		return true;
	}
	private boolean banPlayer(CommandSender sender, String[] args){
		boolean auth = false;
		Player player = null;
		String admin = "server";
		if (sender instanceof Player){
			player = (Player)sender;
			if (Permissions.Security.permission(player, "ultraban.ban")){
				auth = true;
			}else{
			 if (player.isOp()) auth = true; //defaulting to Op if no permissions or node
			}
			admin = player.getName();
		}else{
			auth = true; //if sender is not a player - Console
		}
		if (!auth){
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}
		// Has enough arguments?
		if (args.length < 1) return false;

		String p = args[0]; // Get the victim's name
		if(autoComplete)
			p = util.expandName(p); //If the admin has chosen to do so, autocomplete the name!
		Player victim = this.getServer().getPlayer(p); // What player is really the victim?
		// Reason stuff
		String reason = "not sure";
		boolean broadcast = true;
		if(args.length > 1){
			if(args[1].equalsIgnoreCase("-s")){
				broadcast = false;
				reason = Util.combineSplit(2, args, " ");
			}else
				reason = Util.combineSplit(1, args, " ");
		}

		if(bannedPlayers.contains(p.toLowerCase())){
			properties.load();
			String adminMsg = properties.getNode("messages").getString("banMsgFailed");
			adminMsg = adminMsg.replaceAll("%victim%", p);
			sender.sendMessage(Util.formatMessage(adminMsg));
			return true;
		}

		bannedPlayers.add(p.toLowerCase()); // Add name to HASHSET (RAM) Locally

		// Add player to database
		db.addPlayer(p, reason, admin, 0, 0);

		//Log in console
		log.log(Level.INFO, "[UltraBan] " + admin + " banned player " + p + ".");

		if(victim != null){ // If he is online, kick him with a nice message :)

			//Send message to victim
			String adminMsg = properties.getNode("messages").getString("banMsgVictim");
			adminMsg = adminMsg.replaceAll("%player%", admin);
			adminMsg = adminMsg.replaceAll("%reason%", reason);
			victim.kickPlayer(Util.formatMessage(adminMsg));
		}
		//Send message to all players
		if(broadcast){
			String adminMsgAll = properties.getNode("messages").getString("banMsgBroadcast");
			adminMsgAll = adminMsgAll.replaceAll("%player%", admin);
			adminMsgAll = adminMsgAll.replaceAll("%reason%", reason);
			adminMsgAll = adminMsgAll.replaceAll("%victim%", p);
			this.getServer().broadcastMessage(Util.formatMessage(adminMsgAll));
		}

		return true;
	}
	private boolean tempbanPlayer(CommandSender sender, String[] args){
		boolean auth = false;
		Player player = null;
		String admin = "server";
		if (sender instanceof Player){
			player = (Player)sender;
			if (Permissions.Security.permission(player, "ultraban.tempban")){
				auth = true;
			}else{
			 if (player.isOp()) auth = true; //defaulting to Op if no permissions or node
			}
			admin = player.getName();
		}else{
			auth = true; //if sender is not a player - Console
		}
		if (!auth){
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}
		if (args.length < 3) return false;

		String p = args[0]; // Get the victim's name
		if(autoComplete)
			p = util.expandName(p); //If the admin has chosen to do so, autocomplete the name!
		Player victim = this.getServer().getPlayer(p); // What player is really the victim?
		// Reason stuff
		String reason = "not sure";
		boolean broadcast = true;
		if(args.length > 3){
			if(args[3].equalsIgnoreCase("-s")){
				broadcast = false;
				reason = Util.combineSplit(4, args, " ");
			}else
				reason = Util.combineSplit(3, args, " ");
		}

		if(bannedPlayers.contains(p.toLowerCase())){
			properties.load();
			String adminMsg = properties.getNode("messages").getString("banMsgFailed");
			adminMsg = adminMsg.replaceAll("%victim%", p);
			sender.sendMessage(Util.formatMessage(adminMsg));
			return true;
		}

		long tempTime = Util.parseTimeSpec(args[1],args[2]); //parse the time and do other crap below
		if(tempTime == 0)
			return false;
		bannedPlayers.add(p.toLowerCase()); // Add name to RAM
		tempTime = System.currentTimeMillis()/1000+tempTime;
		tempBans.put(p.toLowerCase(), tempTime); //put him in the temporary bans

		// Add to database
		db.addPlayer(p, reason, admin, tempTime, 0);

		//Log in console
		log.log(Level.INFO, "[UltraBan] " + admin + " tempbanned player " + p + ".");

		if(victim != null){ // If he is online, boot'em

			//Send message to victim
			String adminMsg = properties.getNode("messages").getString("tempbanMsgVictim");
			adminMsg = adminMsg.replaceAll("%player%", admin);
			adminMsg = adminMsg.replaceAll("%reason%", reason);
			victim.kickPlayer(Util.formatMessage(adminMsg));
		}
		if(broadcast){
			//Send message to all players
			String adminMsgAll = properties.getNode("messages").getString("tempbanMsgBroadcast");
			adminMsgAll = adminMsgAll.replaceAll("%player%", admin);
			adminMsgAll = adminMsgAll.replaceAll("%reason%", reason);
			adminMsgAll = adminMsgAll.replaceAll("%victim%", p);
			this.getServer().broadcastMessage(Util.formatMessage(adminMsgAll));
		}
		return true;
	}
	private boolean checkBan(CommandSender sender, String[] args){
		String p = args[0];
		if(bannedPlayers.contains(p.toLowerCase()))
			sender.sendMessage(ChatColor.GRAY + "Player " + p + " is banned.");
		else
			sender.sendMessage(ChatColor.BLUE + "Player " + p + " is not banned.");
		return true;
	}
	private boolean ipBan(CommandSender sender, String[] args){
		boolean auth = false;
		Player player = null;
		String admin = "server";
		if (sender instanceof Player){
			player = (Player)sender;
			if (Permissions.Security.permission(player, "ultraban.ipban")){
				auth = true;
			}else{
			 if (player.isOp()) auth = true; //defaulting to Op if no permissions or node
			}
			admin = player.getName();
		}else{
			auth = true; //if sender is not a player - Console
		}
		if (!auth){
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}
		// Has enough arguments?
		if (args.length < 1) return false;

		String p = args[0]; // Get the victim's name
		if(autoComplete)
			p = util.expandName(p); //If the admin has chosen to do so, autocomplete the name!
		Player victim = this.getServer().getPlayer(p); // What player is really the victim?
		if(victim == null){
			sender.sendMessage("Couldn't find player.");
			return true;
		}
		// Reason stuff
		String reason = "not sure";
		boolean broadcast = true;
		if(args.length > 1){
			if(args[1].equalsIgnoreCase("-s")){
				broadcast = false;
				reason = Util.combineSplit(2, args, " ");
			}else
				reason = Util.combineSplit(1, args, " ");
		}

		if(bannedPlayers.contains(p.toLowerCase())){
			properties.load();
			String adminMsg = properties.getNode("messages").getString("banMsgFailed");
			adminMsg = adminMsg.replaceAll("%victim%", p);
			sender.sendMessage(Util.formatMessage(adminMsg));
			return true;
		}

		bannedPlayers.add(p.toLowerCase()); // Add name to RAM
		bannedIPs.add(victim.getAddress().getAddress().getHostAddress()); // Add ip to RAM

		// Add player to database
		db.addPlayer(p, reason, admin, 0, 1);

		//Log in console
		log.log(Level.INFO, "[UltraBan] " + admin + " banned player " + p + ".");

		//Send message to victim
		String adminMsg = properties.getNode("messages").getString("banMsgVictim");
		adminMsg = adminMsg.replaceAll("%player%", admin);
		adminMsg = adminMsg.replaceAll("%reason%", reason);
		victim.kickPlayer(Util.formatMessage(adminMsg));

		if(broadcast){
			//Send message to all players
			String adminMsgAll = properties.getNode("messages").getString("banMsgBroadcast");
			adminMsgAll = adminMsgAll.replaceAll("%player%", admin);
			adminMsgAll = adminMsgAll.replaceAll("%reason%", reason);
			adminMsgAll = adminMsgAll.replaceAll("%victim%", p);
			this.getServer().broadcastMessage(Util.formatMessage(adminMsgAll));
		}

		return true;
	}	
	private boolean warnPlayer(CommandSender sender, String[] args){
		boolean auth = false;
		Player player = null;
		String admin = "server";
		if (sender instanceof Player){
			player = (Player)sender;
			if (Permissions.Security.permission(player, "ultraban.warn")){
				auth = true;
			}else{
			 if (player.isOp()) auth = true; //defaulting to Op if no permissions or node
			}
			admin = player.getName();
		}else{
			auth = true; //if sender is not a player - Console
		}
		if (!auth){
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}
		// Has enough arguments?
		if (args.length < 1) return false;

		String p = args[0]; // Get the victim's name from the command
		if(autoComplete)
			p = util.expandName(p); //If the admin has chosen to do so, autocomplete the name!
		Player victim = this.getServer().getPlayer(p); // What player is really the victim?
		// Reason stuff
		String reason = "not sure";
		
		boolean broadcast = true;
		if(args.length > 1){
			if(args[1].equalsIgnoreCase("-s")){
				broadcast = false;
				reason = Util.combineSplit(2, args, " ");
			}else
				reason = Util.combineSplit(1, args, " ");
			
		}
		// Add player to database
		db.addPlayer(p, reason, admin, 0, 2);

		//Log in console
		log.log(Level.INFO, "[UltraBan] " + admin + " warned player " + p + ".");


		//Send message to all players
		if(broadcast){ // Doesn't get kicked, just embarrassed.
			this.getServer().broadcastMessage(ChatColor.RED + "Player " + p + " recieved a warning from " + admin + ":");
			this.getServer().broadcastMessage(ChatColor.GRAY + "  " + reason);
			return true;
		}else{
			if(victim != null){
			victim.sendMessage(ChatColor.RED + "You have recieved a warning from " + admin + ":");
			victim.sendMessage(ChatColor.GRAY + "  " + reason);
			//broadcast = false SILENT DOUBLE WHAMMY
			return true;
			}
		}
		

		return true;
	}
	private boolean fine(CommandSender sender, String[] args){
		boolean auth = false;
		Player player = null;
		String admin = "server";
		String perms = "ultraban.fine";
		if (sender instanceof Player){
			player = (Player)sender;
			admin = player.getName();
			 			//add Vault permissions test before reconstruct
				//if (this.setupPermissions()){	
					//if (Permission.has(player, perms)) auth = true;
					//else{
				if (Permissions.Security.has(player, perms)) auth = true;
				//}
		}else{
			auth = true; //if sender is not a player - Console
		}
		if (!auth){
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}
		if (!checkEconomy){ //config injection.
			return true;
		}
		if (args.length < 1) return false;
		String p = args[0];
		if(autoComplete) p = util.expandName(p); 
		Player victim = this.getServer().getPlayer(p);
		boolean broadcast = true;
		String amt = args[1]; //set string amount to argument	
			//Going loud
			if(args.length > 1){
				if(args[1].equalsIgnoreCase("-s")){
				broadcast = false;
				amt  = Util.combineSplit(2, args, " ");
				}else{
				amt = Util.combineSplit(1, args, " ");
				}
			}
			
		if(victim != null){
			if(!broadcast){ //If silent wake his ass up
				properties.load();
				String MsgVictim = properties.getNode("messages").getString("fineMsgVictim");
				MsgVictim = MsgVictim.replaceAll("%player%", admin);
				MsgVictim = MsgVictim.replaceAll("%amt%", amt);
				MsgVictim = MsgVictim.replaceAll("%victim%", p);
				victim.sendMessage(Util.formatMessage(MsgVictim));
			}
			if(this.setupEconomy()){
				double amtd = Double.valueOf(amt.trim()); //Covert + Take Whitespace if any
				economy.withdrawPlayer(victim.getName(), amtd); //Amount Absolute Value
			}
			log.log(Level.INFO, "[UltraBan] " + admin + " fined player " + p + " amount of " + amt + ".");
			db.addPlayer(p, amt, admin, 0, 4);
			if(broadcast){
				properties.load();
				String adminMsgAll = properties.getNode("messages").getString("fineMsgBroadcast");
				adminMsgAll = adminMsgAll.replaceAll("%player%", admin);
				adminMsgAll = adminMsgAll.replaceAll("%amt%", amt);
				adminMsgAll = adminMsgAll.replaceAll("%victim%", p);
				this.getServer().broadcastMessage(Util.formatMessage(adminMsgAll));
				return true;
			}
			return true;
		}else{
			sender.sendMessage(ChatColor.GRAY + "Player must be online!");
			return true;
		}		
	}
	private boolean ureload(CommandSender sender){
		boolean auth = false;
		Player player = null;
		String admin = "server";
		if (sender instanceof Player){
			player = (Player)sender;
			if (Permissions.Security.permission(player, "ultraban.reload")){
				auth = true;
			}else{
			 if (player.isOp()) auth = true; //defaulting to Op if no permissions or node
			}
			admin = player.getName();
		}else{
			auth = true; //if sender is not a player - Console
		}
		
		if (auth) {

			bannedPlayers.clear(); // Clear the HASHSET (RAM)
			tempBans.clear();
			//new
			db = new MySQLDatabase(); // Refill the HASHSET (RAM)
			db.initialize(this);
			this.reloadConfig();
			log.log(Level.INFO, "[UltraBan] " + admin + " reloaded the banlist.");
			log.log(Level.INFO, "[UltraBan] reloaded the config.");
			sender.sendMessage("§2Reloaded Ultraban.");
			return true;
		}else{
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}
	}
	private boolean exportBans(CommandSender sender){
		boolean auth = false;
		Player player = null;
		if (sender instanceof Player){
			player = (Player)sender;
			if (Permissions.Security.permission(player, "ultraban.export"))  auth = true;
		}else{
			auth = true;
		}
		
		if (auth) {
			try
			{
				BufferedWriter banlist = new BufferedWriter(new FileWriter("banned-players.txt",true));
				for(String p : bannedPlayers){
					banlist.newLine();
					banlist.write(p);
				}
				banlist.close();
			}
			catch(IOException e)          
			{
				UltraBan.log.log(Level.SEVERE,"UltraBan: Couldn't write to banned-players.txt");
			}
			sender.sendMessage("§2Exported banlist to banned-players.txt.");
			return true;
		}else{
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
		}
	}
	private boolean uversion(CommandSender sender){
		boolean auth = false;
		Player player = null;
		String admin = "server";
		if (sender instanceof Player){
			player = (Player)sender;
			if (Permissions.Security.permission(player, "ultraban.version")){
				auth = true;
			}else{
			 if (player.isOp()) auth = true; //defaulting to Op if no permissions or node
			}
			admin = player.getName();
		}else{
			auth = true; //if sender is not a player - Console
		}
		
		if(auth){
		PluginDescriptionFile pdfFile = this.getDescription();
		sender.sendMessage(ChatColor.BLUE + "Thank you " + admin + " for using:");
		sender.sendMessage(ChatColor.GRAY + pdfFile.getName() + " version " + pdfFile.getVersion() + " by " + pdfFile.getAuthors());
		return true;
		}else{
		sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
		}
		return false;
	}
	private boolean uhelp(CommandSender sender){
		boolean auth = false;
		Player player = null;
		String admin = "server";
		if (sender instanceof Player){
			player = (Player)sender;
			if (Permissions.Security.permission(player, "ultraban.help")){
				auth = true;
			}else{
			 if (player.isOp()) auth = true; //defaulting to Op if no permissions or node
			}
			admin = player.getName();
		}else{
			auth = true; //if sender is not a player - Console
		}
		if (auth) {
		PluginDescriptionFile pdfFile = this.getDescription();
		sender.sendMessage(ChatColor.GRAY + pdfFile.getName() + " version " + pdfFile.getVersion() + " Help System");
		sender.sendMessage(ChatColor.BLUE + "Current User " + admin);
		sender.sendMessage(ChatColor.GRAY + "-----------------------------------------------");
		sender.sendMessage(ChatColor.BLUE + "Required Info {}" + ChatColor.GREEN + " Optional ()");
		sender.sendMessage(ChatColor.GRAY + "/ban      {player} (-s) {reason}");
		sender.sendMessage(ChatColor.GRAY + "/tempban  {player} (-s) {amt} {sec/min/hour/day} {Reason}");
		sender.sendMessage(ChatColor.GRAY + "/ipban    {player} (-s) {reason}");
		sender.sendMessage(ChatColor.GRAY + "/unban    {player}");
		sender.sendMessage(ChatColor.GRAY + "/checkban {player}");
		sender.sendMessage(ChatColor.GRAY + "/kick     {player} (-s) {reason}");
		sender.sendMessage(ChatColor.GRAY + "/warn     {player} (-s) {reason}");
		sender.sendMessage(ChatColor.GRAY + "/fine     {player} (-s) {amt}");
		sender.sendMessage(ChatColor.GRAY + "/editban  (help)");
		sender.sendMessage(ChatColor.GRAY + "/exportbans");
		sender.sendMessage(ChatColor.GRAY + "/uhelp");
		sender.sendMessage(ChatColor.GRAY + "/empty	     {player}");
		sender.sendMessage(ChatColor.GRAY + "/starve	 {player}");
		sender.sendMessage(ChatColor.GRAY + "/forcespawn {player}");
		sender.sendMessage(ChatColor.GRAY + "/ureload");
		sender.sendMessage(ChatColor.GRAY + "/uversion");
		return true;
		}else{
		//all needs this message
		sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
		return true;
		}
	}
	private boolean empty(CommandSender sender, String[] args){
		boolean auth = false;
		Player player = null;
		String admin = "server";
		if (sender instanceof Player){
			player = (Player)sender;
			if (Permissions.Security.permission(player, "ultraban.emtpy")){
				auth = true;
			}else{
			 if (player.isOp()) auth = true; //defaulting to Op if no permissions or node
			}
			admin = player.getName();
		}else{
			auth = true; //if sender is not a player - Console
		}
		if(auth){
			if (args.length < 1) return false;
			String p = args[0];
			if(autoComplete) p = util.expandName(p); 
			Player victim = this.getServer().getPlayer(p);
				sender.sendMessage(ChatColor.GRAY + admin + " has cleared inventory of " + victim + "!");
				victim.sendMessage(ChatColor.GRAY + admin + " has cleared your inventory!");
				
				victim.getInventory().clear();
				return true;
		}else{
			//all needs this message
			sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
			return true;
			}
		
	}
	private boolean forcespawn(CommandSender sender, String[] args){
		boolean auth = false;
		Player player = null;
		String admin = "server";
		if (sender instanceof Player){
			player = (Player)sender;
			if (Permissions.Security.permission(player, "ultraban.spawn")){
				auth = true;
			}else{
			 if (player.isOp()) auth = true; //defaulting to Op if no permissions or node
			}
			admin = player.getName();
		}else{
			auth = true; //if sender is not a player - Console
		}
		if(auth){
			if (args.length < 1) return false;
			String p = args[0];
			if(autoComplete) p = util.expandName(p); 
			Player victim = this.getServer().getPlayer(p);
				sender.sendMessage(ChatColor.GRAY + admin + " has sent " + p + " to spawn!");
				victim.sendMessage(ChatColor.GRAY + admin + " has sent you to spawn!");
				//Further Research	
				World wtlp = victim.getWorld();
				Location tlp = wtlp.getSpawnLocation();
				victim.teleport(tlp);
		}
		
		return true;
	}
	private boolean starve(CommandSender sender, String[] args){
		boolean auth = false;
		Player player = null;
		if (sender instanceof Player){
			player = (Player)sender;
			if (Permissions.Security.permission(player, "ultraban.starve")){
				auth = true;
			}else{
			 if (player.isOp()) auth = true; //defaulting to Op if no permissions or node
			}
		}else{
			auth = true; //if sender is not a player - Console
		}
		if(auth){
			if (args.length < 1) return false;
			String p = args[0];
			if(autoComplete) p = util.expandName(p); 
			Player victim = this.getServer().getPlayer(p);
			sender.sendMessage(ChatColor.GRAY + p + " is now starving!");
			victim.sendMessage(ChatColor.GRAY + "You are now starving!");
			//player.setFoodLevel(victim.getFoodLevel());
			int st = 0;
			victim.setFoodLevel(st);
		}
		
		return true;
	}

}
