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
import com.nijikokun.bukkit.Permissions.Permissions;

public class Jail implements CommandExecutor{

	public static final Logger log = Logger.getLogger("Minecraft");
	UltraBan plugin;
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
			boolean auth = false;
			Player player = null;
			String admin = "server";
			if (sender instanceof Player){
				player = (Player)sender;
				if (Permissions.Security.permission(player, "ultraban.jail")){
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
				if(args[0].equalsIgnoreCase("set")){
						this.setlp = player.getLocation();
						setJail(setlp);
						sender.sendMessage(ChatColor.GRAY + "Jail has been set to " + ChatColor.AQUA + setlp.toString());
						return true;
				}
				if(args[0].equalsIgnoreCase("pardon")){
						String jaile = args[1];
						if(autoComplete) jaile = expandName(jaile);
						plugin.jailed.remove(jaile.toLowerCase());
						Player jailee = plugin.getServer().getPlayer(jaile);
						Location tlp = jailee.getWorld().getSpawnLocation();
						jailee.teleport(tlp);
						jailee.sendMessage(ChatColor.GRAY + "You've been released from Jail!");
						sender.sendMessage(ChatColor.GRAY + jaile + " released from Jail!");
						return true;
				}
				String p = args[0];
				Player victim = plugin.getServer().getPlayer(p);
				if(autoComplete) p = expandName(p);	
				plugin.db.addPlayer(p, "Jailed", admin, 0, 6);
				sender.sendMessage(ChatColor.GRAY + p + " is now in Jail!");
				sender.sendMessage(ChatColor.GRAY + admin + " don't forget to let him out!");
				victim.sendMessage(ChatColor.GRAY + "You've been Arrested.");
				plugin.jailed.add(p.toLowerCase());
				Location stlp = getJail();
				victim.teleport(stlp);
					
				}
			
			return true;
	}

	public void setJail(Location location) {
		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
        config.set("jail.x", (int) setlp.getX());
        config.set("jail.y", (int) setlp.getY());
        config.set("jail.z", (int) setlp.getZ());
        config.set("jail.world", setlp.getWorld().getName());

    }
    public Location getJail(){
    	YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
        Location setlp = new Location(
                plugin.getServer().getWorld(config.getString("jail.world", plugin.getServer().getWorlds().get(0).getName())),
                config.getInt("jail.x", 0),
                config.getInt("jail.y", 0),
                config.getInt("jail.z", 0));
        	return setlp;
            
        }
    }

        
