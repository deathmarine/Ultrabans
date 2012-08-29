/* COPYRIGHT (c) 2012 Joshua McCurry
 * This work is licensed under the
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 Unported License
 * and use of this software or its code is an agreement to this license.
 * A full copy of this license can be found at
 * http://creativecommons.org/licenses/by-nc-sa/3.0/. 
 */
package com.modcrafting.ultrabans;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.modcrafting.ultrabans.commands.Ban;
import com.modcrafting.ultrabans.commands.Check;
import com.modcrafting.ultrabans.commands.CheckIP;
import com.modcrafting.ultrabans.commands.Clean;
import com.modcrafting.ultrabans.commands.DupeIP;
import com.modcrafting.ultrabans.commands.Edit;
import com.modcrafting.ultrabans.commands.Empty;
import com.modcrafting.ultrabans.commands.Export;
import com.modcrafting.ultrabans.commands.Fine;
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
import com.modcrafting.ultrabans.util.DataHandler;
import com.modcrafting.ultrabans.util.EditBan;
import com.modcrafting.ultrabans.util.Formatting;
import com.modcrafting.ultrabans.util.Jailtools;

public class UltraBan extends JavaPlugin {
	public HashSet<String> bannedPlayers = new HashSet<String>();
	public HashSet<String> bannedIPs = new HashSet<String>();
	public HashSet<String> jailed = new HashSet<String>();
	public HashSet<String> muted = new HashSet<String>();
	public Map<String, Long> tempBans = new HashMap<String, Long>();
	public Map<String, Long> tempJail = new HashMap<String, Long>();
	public Map<String, EditBan> banEditors = new HashMap<String, EditBan>();
	public DataHandler data = new DataHandler(this);
	public Formatting util = new Formatting(this);
	public Jailtools jail = new Jailtools(this);
	public Database db;
	public net.milkbowl.vault.economy.Economy economy = null;
	public boolean autoComplete;
	public String regexAdmin = "%admin%";
	public String regexReason = "%reason%";
	public String regexVictim = "%victim%";
	public String regexAmt = "%amt%";
	public void onDisable() {
		this.getServer().getScheduler().cancelTasks(this);
		tempBans.clear();
		tempJail.clear();
		bannedPlayers.clear();
		bannedIPs.clear();
		jailed.clear();
		muted.clear();
		banEditors.clear();
	}
	public void onEnable() {
		this.getDataFolder().mkdir();
		data.createDefaultConfiguration("config.yml");
		FileConfiguration config = getConfig();
		autoComplete = config.getBoolean("auto-complete", true);
		long l = config.getLong("serverSync.timing", 72000L); 
		long time = System.currentTimeMillis();
		if(this.getConfig().getString("Database").equalsIgnoreCase("mysql")){
			db = new SQL(this);
		}else{
			db = new SQLite(this);
		}
		db.load();
		db.loadJailed();
		
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new UltraBanPlayerListener(this), this);
		pm.registerEvents(new UltraBanBlockListener(this), this);
		if(config.getBoolean("serverSync.enable", false)) this.getServer().getScheduler().scheduleAsyncRepeatingTask(this,new Runnable(){
			@Override
			public void run() {
				onDisable();
				db.load();
				db.loadJailed();
				System.out.println("UltraBans Sync is Enabled!");
			}
			
		},l,l);	
		loadCommands();
		long diff = System.currentTimeMillis()-time;
		this.getLogger().info(" Loaded. "+diff+"ms");
	}
	public boolean setupEconomy(){
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
			if (economyProvider != null) economy = economyProvider.getProvider();
			return (economy != null);
	}
	public void loadCommands(){
		getCommand("ban").setExecutor(new Ban(this));
		getCommand("checkban").setExecutor(new Check(this));
		getCommand("checkip").setExecutor(new CheckIP(this));
		getCommand("dupeip").setExecutor(new DupeIP(this));
		getCommand("editban").setExecutor(new Edit(this));
		getCommand("empty").setExecutor(new Empty(this));
		getCommand("importbans").setExecutor(new Import(this));
		getCommand("exportbans").setExecutor(new Export(this));
		getCommand("fine").setExecutor(new Fine(this));
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
}
		


