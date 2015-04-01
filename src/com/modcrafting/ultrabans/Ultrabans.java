/* COPYRIGHT (c) 2015 Deathmarine
 * This file is part of Ultrabans.
 * Ultrabans is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Ultrabans is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Ultrabans.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.modcrafting.ultrabans;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.modcrafting.ultrabans.commands.Ban;
import com.modcrafting.ultrabans.commands.CheckID;
import com.modcrafting.ultrabans.commands.CheckIP;
import com.modcrafting.ultrabans.commands.Checkban;
import com.modcrafting.ultrabans.commands.Clean;
import com.modcrafting.ultrabans.commands.DupeIP;
import com.modcrafting.ultrabans.commands.Export;
import com.modcrafting.ultrabans.commands.Help;
import com.modcrafting.ultrabans.commands.History;
import com.modcrafting.ultrabans.commands.Import;
import com.modcrafting.ultrabans.commands.Inventory;
import com.modcrafting.ultrabans.commands.Ipban;
import com.modcrafting.ultrabans.commands.Jail;
import com.modcrafting.ultrabans.commands.Kick;
import com.modcrafting.ultrabans.commands.Lockdown;
import com.modcrafting.ultrabans.commands.Mute;
import com.modcrafting.ultrabans.commands.Pardon;
import com.modcrafting.ultrabans.commands.Perma;
import com.modcrafting.ultrabans.commands.Ping;
import com.modcrafting.ultrabans.commands.Reload;
import com.modcrafting.ultrabans.commands.Spawn;
import com.modcrafting.ultrabans.commands.Starve;
import com.modcrafting.ultrabans.commands.Status;
import com.modcrafting.ultrabans.commands.Tempban;
import com.modcrafting.ultrabans.commands.Tempipban;
import com.modcrafting.ultrabans.commands.Tempjail;
import com.modcrafting.ultrabans.commands.Unban;
import com.modcrafting.ultrabans.commands.Version;
import com.modcrafting.ultrabans.commands.Warn;
import com.modcrafting.ultrabans.db.Database;
import com.modcrafting.ultrabans.db.SQL;
import com.modcrafting.ultrabans.db.SQLite;
import com.modcrafting.ultrabans.listeners.UltraBanBlockListener;
import com.modcrafting.ultrabans.listeners.UltraBanPlayerListener;
import com.modcrafting.ultrabans.util.InfoAlias;
import com.modcrafting.ultrabans.util.InfoBan;
import com.modcrafting.ultrabans.util.InfoIP;
import com.modcrafting.ultrabans.util.Jailtools;

public class Ultrabans extends JavaPlugin {
	public HashSet<String> muted = new HashSet<String>();
	
	public Set<InfoBan> cache = new HashSet<InfoBan>();
	public Set<InfoIP> cacheIP = new HashSet<InfoIP>();
	public Set<InfoAlias> cacheAlias = new HashSet<InfoAlias>();
	
	public Jailtools jail = new Jailtools(this);
	public UltrabansAPI api = new UltrabansAPI(this);
	
	public YamlConfiguration lang;
	private Database db;
	
	public static final String ADMIN = "%admin%";
	public static final String REASON = "%reason%";
	public static final String VICTIM = "%victim%";
	public static final String AMOUNT = "%amt%";
	public static final String MODE = "%mode%";
	public static final String TIME = "%time%";
	
	public static String DEFAULT_ADMIN;
	public static String DEFAULT_REASON;
	public static String DEFAULT_DENY_MESSAGE;
	
	private static Ultrabans constant;
	private boolean log;
	
	public void onDisable() {
		this.getServer().getScheduler().cancelTasks(this);
		cache.clear();
		cacheIP.clear();
		muted.clear();
	}
	
	public void onEnable() {
		long time = System.currentTimeMillis();
		setConstant(this);
		this.getDataFolder().mkdir();
		this.saveDefaultConfig();
		FileConfiguration config = getConfig();
		log = config.getBoolean("Log.Enabled",true);
		
		String la = config.getString("Language", "en-us");
		File fls = new File(this.getDataFolder(),"./lang/");
		fls.mkdir();
		File fl = new File(fls,"/"+la+".yml");
		if(!fl.exists()){
			try{
				fl.createNewFile();
	            BufferedInputStream in = new BufferedInputStream(this.getResource(la+".yml"));
	            FileOutputStream fout = new FileOutputStream(fl);
	            byte[] data = new byte[1024];
	            int c;
	            while ((c = in.read(data, 0, 1024)) != -1)
	                fout.write(data, 0, c);
	            in.close();
	            fout.close();
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		lang = YamlConfiguration.loadConfiguration(fl);
		
		DEFAULT_ADMIN = config.getString("Label.Console", "Server");
		DEFAULT_REASON = config.getString("Label.Reason", "Unsure");
		DEFAULT_DENY_MESSAGE = ChatColor.RED + lang.getString("Permission");
		
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new UltraBanPlayerListener(this), this);
		pm.registerEvents(new UltraBanBlockListener(this), this);
		
		loadCommands();

		//Storage
		if(config.getString("Database").equalsIgnoreCase("mysql")){
			db = new SQL(this);
		}else{
			db = new SQLite(this);
		}
		db.load();
		
		this.getLogger().info("Loaded. "+((long) (System.currentTimeMillis()-time)/1000)+" secs.");
	}
	
	public void loadCommands(){
		getCommand("ban").setExecutor(new Ban(this));
		getCommand("checkban").setExecutor(new Checkban(this));
		getCommand("checkip").setExecutor(new CheckIP(this));
		getCommand("checkuuid").setExecutor(new CheckID(this));
		getCommand("dupeip").setExecutor(new DupeIP(this));
		getCommand("importbans").setExecutor(new Import(this));
		getCommand("exportbans").setExecutor(new Export(this));
		getCommand("uhelp").setExecutor(new Help(this));
		getCommand("ipban").setExecutor(new Ipban(this));
		getCommand("kick").setExecutor(new Kick(this));
		getCommand("ureload").setExecutor(new Reload(this));
		getCommand("forcespawn").setExecutor(new Spawn(this));
		getCommand("starve").setExecutor(new Starve(this));
		getCommand("tempban").setExecutor(new Tempban(this));
		getCommand("tempipban").setExecutor(new Tempipban(this));
		getCommand("unban").setExecutor(new Unban(this));
		getCommand("uversion").setExecutor(new Version(this));
		getCommand("warn").setExecutor(new Warn(this));
		getCommand("jail").setExecutor(new Jail(this));
		getCommand("tempjail").setExecutor(new Tempjail(this));
		getCommand("permaban").setExecutor(new Perma(this));
		getCommand("lockdown").setExecutor(new Lockdown(this));
		getCommand("umute").setExecutor(new Mute(this));
		getCommand("history").setExecutor(new History(this));
		getCommand("pardon").setExecutor(new Pardon(this));
		getCommand("invof").setExecutor(new Inventory(this));
		getCommand("ustatus").setExecutor(new Status(this));
		getCommand("uclean").setExecutor(new Clean(this));
		getCommand("uping").setExecutor(new Ping(this));
	}
	
	public static Ultrabans getPlugin() {
		return constant;
	}
	
	private static void setConstant(Ultrabans constant) {
		Ultrabans.constant = constant;
	}
	
	public Database getUBDatabase(){
		return db;
	}

	public boolean getLog() {
		return log;
	}

	public YamlConfiguration getLangConfig() {
		return lang;
	}

	public UltrabansAPI getAPI() {
		return api;
	}
	
	public boolean addInfoBan(InfoBan ban){
		return cache.add(ban);
	}
	
	/**
	 * Returns a List of IP addresses matching UUID from Local Cache
	 * @param uuid
	 * @return
	 */
	public List<String> checkIPofUUID(String uuid){
		List<String> list = new ArrayList<String>();
		InfoIP[] breakdown = (InfoIP[]) cacheIP.toArray();
		for(int i = 0; i<breakdown.length; i++){
			if(breakdown[i].getUuid().equals(uuid)){
				list.add(breakdown[i].getIp());
			}
		}
		return list;
	}
	
	/**
	 * Returns a List of UUIDs matching an IP address from Local Cache
	 * @param uuid
	 * @return
	 */
	public List<String> checkUUIDofIP(String ip){
		List<String> list = new ArrayList<String>();
		InfoIP[] breakdown = (InfoIP[]) cacheIP.toArray();
		for(int i = 0; i<breakdown.length; i++){
			if(breakdown[i].getIp().equals(ip)){
				list.add(breakdown[i].getUuid());
			}
		}
		return list;
	}
	
}
		


