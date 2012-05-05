package com.modcrafting.ultrabans.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.modcrafting.ultrabans.UltraBan;

public class Help implements CommandExecutor{
	UltraBan plugin;
	
	public Help(UltraBan ultraBan) {
		this.plugin = ultraBan;
	}
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		boolean auth = false;
		boolean server = false;
		Player player = null;
		if (sender instanceof Player){
			player = (Player)sender;
			if(player.hasPermission("ultraban.help") || player.isOp()) auth = true;
		}else{
			auth = true;
			server = true;
		}
		if (server){
			sender.sendMessage("Required Info {} Optional () Silent -s Anonymous -a");
			sender.sendMessage("/ban        {player} (-s/-a) {reason}");
			sender.sendMessage("/permaban   {player} (-s/-a) {reason}");		
			sender.sendMessage("/tempban    {player} (-s/-a) {amt} {sec/min/hour/day} {Reason}");
			sender.sendMessage("/ipban      {player} (-s/-a) {reason}");
			sender.sendMessage("/tempipban  {player} (-s/-a) {amt} {sec/min/hour/day} {Reason}");
			sender.sendMessage("/unban      {player}");
			sender.sendMessage("/jail (set/pardon/{player}) {player}");
			sender.sendMessage("/tempjail   {player} (-s/-a) {amt} {sec/min/hour/day} {Reason}");
			sender.sendMessage("/checkban   {player}");
			sender.sendMessage("/checkip    {player}");
			sender.sendMessage("/dupeip     {player}");
			sender.sendMessage("/history    {amt}");
			sender.sendMessage("/kick       {player} (-s/-a) {reason}");
			sender.sendMessage("/warn       {player} (-s/-a) {reason}");
			sender.sendMessage("/fine       {player} (-s/-a) {amt}");
			sender.sendMessage("/umute      {player}");
			sender.sendMessage("/empty      {player}");
			sender.sendMessage("/forcespawn {player}");
			sender.sendMessage("/starve     {player}");
			sender.sendMessage("/rules      (help)");
			sender.sendMessage("/editban    (help)");
			sender.sendMessage("/lockdown   {on/off/status}");
			sender.sendMessage("/uhelp /exportbans /importbans /ureload /uversion");
			return true;
		}
		if (auth) {
		sender.sendMessage(ChatColor.GRAY + "Ultrabans " + ChatColor.BLUE + "Required Info {}" + ChatColor.GREEN + " Optional ()" + ChatColor.RED + " Silent -s");
		if (player.hasPermission("ultraban.ban")) sender.sendMessage(ChatColor.GRAY + "/ban {player} {reason}");
		if (player.hasPermission("ultraban.permaban"))sender.sendMessage(ChatColor.GRAY + "/permaban {player} {reason}");		
		if (player.hasPermission("ultraban.tempban"))sender.sendMessage(ChatColor.GRAY + "/tempban {player} {amt} {sec/min/hour/day} {Reason}");
		if (player.hasPermission("ultraban.ipban"))sender.sendMessage(ChatColor.GRAY + "/ipban {player} {reason}");		
		if (player.hasPermission("ultraban.tempipban"))sender.sendMessage(ChatColor.GRAY + "/tempipban {player} {amt} {sec/min/hour/day} {Reason}");
		if (player.hasPermission("ultraban.unban"))sender.sendMessage(ChatColor.GRAY + "/unban {player}");
		if (player.hasPermission("ultraban.check"))sender.sendMessage(ChatColor.GRAY + "/checkban /checkip /dupeip {player}");
		if (player.hasPermission("ultraban.kick"))sender.sendMessage(ChatColor.GRAY + "/kick {player} {reason}");
		if (player.hasPermission("ultraban.warn"))sender.sendMessage(ChatColor.GRAY + "/warn {player} {reason}");
		if (player.hasPermission("ultraban.fine"))sender.sendMessage(ChatColor.GRAY + "/fine {player} {amt}");
		if (player.hasPermission("ultraban.emtpy"))sender.sendMessage(ChatColor.GRAY + "/empty {player}");
		if (player.hasPermission("ultraban.spawn"))sender.sendMessage(ChatColor.GRAY + "/forcespawn {player}");
		if (player.hasPermission("ultraban.starve"))sender.sendMessage(ChatColor.GRAY + "/starve {player}");
		if (player.hasPermission("ultraban.mute"))sender.sendMessage(ChatColor.GRAY + "/umute (unmute/{player}) {player}");
		if (player.hasPermission("ultraban.rules"))sender.sendMessage(ChatColor.GRAY + "/rules (help)");
		if (player.hasPermission("ultraban.editban"))sender.sendMessage(ChatColor.GRAY + "/editban (help)");
		if (player.hasPermission("ultraban.jail"))sender.sendMessage(ChatColor.GRAY + "/jail (set/pardon/{player}) {player}");		
		if (player.hasPermission("ultraban.tempjail"))sender.sendMessage(ChatColor.GRAY + "/tempjail {player} {amt} {sec/min/hour/day} {Reason}");
		if (player.hasPermission("ultraban.lockdown"))sender.sendMessage(ChatColor.GRAY + "/lockdown {on/off/status}");
		if (player.hasPermission("ultraban.admin")) sender.sendMessage(ChatColor.GRAY + "/uhelp /exportbans /ureload /uversion /importbans");
		return true;
		}else{
		sender.sendMessage(ChatColor.RED + "You do not have the required permissions.");
		return true;
		}
			
	}

}
