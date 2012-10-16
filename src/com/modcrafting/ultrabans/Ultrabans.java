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
import net.h31ix.updater.Updater;
import net.h31ix.updater.Updater.UpdateResult;
import net.h31ix.updater.Updater.UpdateType;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.boxysystems.jgoogleanalytics.JGoogleAnalyticsTracker;
import com.modcrafting.ultrabans.commands.Ban;
import com.modcrafting.ultrabans.commands.Check;
import com.modcrafting.ultrabans.commands.CheckIP;
import com.modcrafting.ultrabans.commands.Clean;
import com.modcrafting.ultrabans.commands.DupeIP;
import com.modcrafting.ultrabans.commands.Edit;
import com.modcrafting.ultrabans.commands.Empty;
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
import com.modcrafting.ultrabans.live.security.RSAServerCrypto;
import com.modcrafting.ultrabans.server.UBServer;
import com.modcrafting.ultrabans.tracker.Track;
import com.modcrafting.ultrabans.util.DataHandler;
import com.modcrafting.ultrabans.util.EditBan;
import com.modcrafting.ultrabans.util.Formatting;
import com.modcrafting.ultrabans.util.Jailtools;
import com.modcrafting.ultrabans.util.LoggerHandler;

public class Ultrabans extends JavaPlugin {
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
	
	public RSAServerCrypto crypto;
	public Database db;
	
	public final String regexAdmin = "%admin%";
	public final String regexReason = "%reason%";
	public final String regexVictim = "%victim%";
	public final String regexAmt = "%amt%";
	public final String regexMode = "%mode%";
	
	public String admin;
	public String reason;
	public String perms;
	
	private UBServer ubserver;
	
	public void onDisable() {
		this.getServer().getScheduler().cancelTasks(this);
		tempBans.clear();
		tempJail.clear();
		bannedPlayers.clear();
		bannedIPs.clear();
		jailed.clear();
		muted.clear();
		banEditors.clear();
		if(ubserver!=null) ubserver.disconnect();
	}
	public void onEnable() {
		long time = System.currentTimeMillis();
		this.getDataFolder().mkdir();
		data.createDefaultConfiguration("config.yml");
		FileConfiguration config = getConfig();
		
		admin=config.getString("Label.Console", "Server");
		reason=config.getString("Label.Reason", "Unsure");
		perms=config.getString("Messages.Permission","You do not have the required permissions.");
		
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new UltraBanPlayerListener(this), this);
		pm.registerEvents(new UltraBanBlockListener(this), this);
		loadCommands();

		PluginDescriptionFile pdf = this.getDescription();
		//Storage
		if(config.getString("Database").equalsIgnoreCase("mysql")){
			db = new SQL(this);
		}else{
			db = new SQLite(this);
		}
		db.load();
		db.loadJailed();
		//Sync
		if(config.getBoolean("Sync.Enabled", false)){
			long t = config.getLong("Sync.Timing", 72000L); 
			this.getServer().getScheduler().scheduleAsyncRepeatingTask(this,new Runnable(){
				@Override
				public void run(){
					onDisable();
					db.load();
					db.loadJailed();
				}
			},1,t);	
		}
		//Updater
		if(config.getBoolean("AutoUpdater.Enabled",true)){
			Updater up = new Updater(this,pdf.getName().toLowerCase(),this.getFile(),UpdateType.DEFAULT,true);
			if(!up.getResult().equals(UpdateResult.SUCCESS)||up.pluginFile(this.getFile().getName())){
				if(up.getResult().equals(UpdateResult.FAIL_NOVERSION)){
					this.getLogger().info("Unable to connect to dev.bukkit.org.");
				}else{
					this.getLogger().info("No Updates found on dev.bukkit.org.");
				}
			}else{
				this.getLogger().info("Update "+up.getLatestVersionString()+" found please restart your server.");
			}
		}
		//Statistic Tracker
		if(config.getBoolean("GoogleAnalytics.Enabled",true)){
			JGoogleAnalyticsTracker tracker = new JGoogleAnalyticsTracker(pdf.getName(),pdf.getVersion(),"UA-35400100-2");
			new Track(tracker);
			//PluginInstances.
			Track.track(pdf.getName()+pdf.getVersion()+" Loaded");
			//PluginErrorLogger
			this.getLogger().addHandler(new LoggerHandler());

			//Pssfffttt... Metrics? Ha.
		}
		//Live Gui
		if(config.getBoolean("Live.Enabled",false)){
			this.getLogger().info("Live initializing.");
			int port = config.getInt("Live.Port",9981);
			//RSA 2048bit
			crypto = new RSAServerCrypto(this.getDataFolder());
			ubserver = new UBServer(port,this);
			new Thread(ubserver).start();
			this.getLogger().info("Live Address: "+this.getServer().getIp()+":"+port);
		}
		this.getLogger().info("Loaded. "+((long) (System.currentTimeMillis()-time)/1000)+" secs.");
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
		


