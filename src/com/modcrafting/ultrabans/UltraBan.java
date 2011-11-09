package com.modcrafting.ultrabans;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
// Why you change bukkit
import org.bukkit.util.config.Configuration;

import com.modcrafting.ultrabans.commands.EditBan;
import com.modcrafting.ultrabans.commands.EditCommand;
import com.modcrafting.ultrabans.db.MySQLDatabase;
import com.modcrafting.ultrabans.util.Util;
import com.nijikokun.bukkit.Permissions.Permissions;

@SuppressWarnings("deprecation")
public class UltraBan extends JavaPlugin {

	public static final Logger log = Logger.getLogger("Minecraft");
	Permissions CurrentPermissions = null;
	public MySQLDatabase db;
	Util util;
	String maindir = "plugins/UltraBan/";
	File Settings = new File(maindir + "config.properties");
	public HashSet<String> bannedPlayers = new HashSet<String>();
	public HashSet<String> bannedIPs = new HashSet<String>();
	public Map<String,Long> tempBans = new HashMap<String,Long>();
	public Map<String, EditBan> banEditors = new HashMap<String, EditBan>();
	private final UltraBanPlayerListener playerListener = new UltraBanPlayerListener(this);
	public Configuration properties = new Configuration(new File("plugins/UltraBan/config.yml"));

	//public static UltraBan plugin;
	public boolean autoComplete;
	//public boolean checkPermissions;
	public boolean checkEconomy;
	
	
		
	public void onDisable() {
		tempBans.clear();
		bannedPlayers.clear();
		System.out.println("UltraBan disabled.");
	}
	protected void createDefaultConfiguration(String name) {
		File actual = new File(getDataFolder(), name);
		if (!actual.exists()) {

			InputStream input =
				this.getClass().getResourceAsStream(name);
			if (input != null) {
				FileOutputStream output = null;

				try {
					output = new FileOutputStream(actual);
					byte[] buf = new byte[8192];
					int length = 0;
					while ((length = input.read(buf)) > 0) {
						output.write(buf, 0, length);
					}

					System.out.println(getDescription().getName()
							+ ": Default configuration file written: " + name);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (input != null)
							input.close();
					} catch (IOException e) {}

					try {
						if (output != null)
							output.close();
					} catch (IOException e) {}
				}
			}
		}
	}
	public void onEnable() {
		new File(maindir).mkdir();

		createDefaultConfiguration("config.yml");
		properties.load();
		//boolean useMysql = properties.getBoolean("mysql", false);
		this.autoComplete = properties.getBoolean("auto-complete", true);
		//this.checkPermissions = properties.getBoolean("usePermissions", true);
		this.checkEconomy = properties.getBoolean("useFines", true);
		//if (useMysql){
		
		
		
		defaultPerms();
		db = new MySQLDatabase();
		db.initialize(this);	
		PluginManager pm = getServer().getPluginManager();
		if (!this.isEnabled()) return;
		//if (!setupPermissions()) return;
		pm.registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Priority.Highest, this);
		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Monitor, this);
		
		getCommand("editban").setExecutor(new EditCommand(this));
		
		PluginDescriptionFile pdfFile = this.getDescription();
		log.log(Level.INFO,"[" + pdfFile.getName() + "]" + " version " + pdfFile.getVersion() + " is enabled!" );
		
	}
	private void defaultPerms(){ //Fallback Permissions 3, Ex
		 Plugin plugin = this.getServer().getPluginManager().getPlugin("Permissions");

		 if (CurrentPermissions == null) {
		 // Permission plugin already registered
		 return;
		 }

		 if (plugin != null) {
		 CurrentPermissions = (Permissions) plugin;
		 } else {
		 this.getServer().getPluginManager().disablePlugin(this);
		 }
	}
}

		


