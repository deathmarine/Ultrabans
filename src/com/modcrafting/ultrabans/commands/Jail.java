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
			if (sender instanceof Player){
				player = (Player)sender;
				if (Permissions.Security.permission(player, "ultraban.jail")){
					auth = true;
				}else{
				 if (player.isOp()) auth = true; //defaulting to Op if no permissions or node
				}
			}else{
				auth = true; //if sender is not a player - Console
			}
			if(auth){
				if (args.length < 1) return false;
				if(args[0] == "set"){
						this.setlp = player.getLocation();
						setJail(setlp);
						return true;
					}
				if(args[0] == "pardon"){
						String victim = args[1];
						if(autoComplete) victim = expandName(victim);
						plugin.jailed.remove(victim.toLowerCase());
						Player jailee = plugin.getServer().getPlayer(victim);
						Location tlp = jailee.getWorld().getSpawnLocation();
						jailee.teleport(tlp);
						return true;
					}
				String p = args[0];
				if(autoComplete) p = expandName(p);	
				String admin = player.getName();
				plugin.db.addPlayer(p, "Jailed", admin, 0, 6);
				Player nvictim = plugin.getServer().getPlayer(p);
				sender.sendMessage(ChatColor.GRAY + p + " is now in Jail!");
				sender.sendMessage(ChatColor.GRAY + admin + " don't forget to let him out!");
				nvictim.sendMessage(ChatColor.GRAY + "You've been Arrested.");
				plugin.jailed.add(p.toLowerCase());
				Location tlp = getJail();
				nvictim.teleport(tlp);
			}
			return true;
	}
	public void setJail(Location location) {
        this.jworld = setlp.getWorld().getName();
        this.x = setlp.getX();
        this.y = setlp.getBlockY();
        this.z = setlp.getZ();
        this.yaw = Math.round(setlp.getYaw()) % 360;
        this.pitch = Math.round(setlp.getPitch()) % 360;
        YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
        config.set("jail.x", x);
        config.set("jail.y", y);
        config.set("jail.z", z);
        config.set("jail.yaw", yaw);
        config.set("jail.pitch", pitch);
        config.set("jail.world", world);
        plugin.saveConfig();

    }
    public Location getJail(){
    	YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
    		         config.get("jail.x", x);
    		         config.get("jail.y", y);
    		         config.get("jail.z", z);
    		         config.get("jail.yaw", yaw);
    		         config.get("jail.pitch", pitch);
    		         config.get("jail.world", world);
    		         return new Location(world, x, y, z, yaw, pitch);
       
            
        }
    }

        
