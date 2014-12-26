package com.modcrafting.ultrabans.commands;

import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.*;

import com.modcrafting.ultrabans.Ultrabans;

public class Ubinfo implements CommandExecutor {
    public Ubinfo(Ultrabans ultrabans) {
	}

	public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (sender instanceof Player) {
            final Player player = (Player)sender;
            if (cmd.getName().equalsIgnoreCase("ubinfo")) {
                player.sendMessage(ChatColor.RED + "Info on Ultrabans");
                player.sendMessage(ChatColor.WHITE + "");
                player.sendMessage(ChatColor.AQUA + "Authors: [Death_marine, TekkitCommando]");
                player.sendMessage(ChatColor.WHITE + "");
                player.sendMessage(ChatColor.AQUA + "Github URL: https://github.com/TekkitCommando/Ultrabans");
                player.sendMessage(ChatColor.WHITE + "");
                player.sendMessage(ChatColor.AQUA + "BukkitDev URL: dev.bukkit.org/bukkit-plugins/ultrabans");
                player.sendMessage(ChatColor.WHITE + "");
                player.sendMessage(ChatColor.AQUA + "Have a Suggestion or Bug Report? Submit a Ticket --> http://dev.bukkit.org/bukkit-plugins/ultrabans/tickets/");
                player.sendMessage(ChatColor.WHITE + "");
                player.sendMessage(ChatColor.AQUA + "Website: http://www.sourceforums.org");
                player.sendMessage(ChatColor.WHITE + "");
                player.sendMessage(ChatColor.AQUA + "Email: admin@sourceforums.org");
            }
        }
        return true;
    }
}
