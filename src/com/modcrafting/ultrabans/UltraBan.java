package com.modcrafting.ultrabans;
/**
 * Wickity Wickity Wooh
 * Got to love the magic!
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import com.modcrafting.ultrabans.commands.Ban;
import com.modcrafting.ultrabans.commands.Check;
import com.modcrafting.ultrabans.commands.EditBan;
import com.modcrafting.ultrabans.commands.EditCommand;
import com.modcrafting.ultrabans.commands.Empty;
import com.modcrafting.ultrabans.commands.Export;
import com.modcrafting.ultrabans.commands.Fine;
import com.modcrafting.ultrabans.commands.Help;
import com.modcrafting.ultrabans.commands.Import;
import com.modcrafting.ultrabans.commands.Ipban;
import com.modcrafting.ultrabans.commands.Jail;
import com.modcrafting.ultrabans.commands.Kick;
import com.modcrafting.ultrabans.commands.Lockdown;
import com.modcrafting.ultrabans.commands.Perma;
import com.modcrafting.ultrabans.commands.Reload;
import com.modcrafting.ultrabans.commands.Spawn;
import com.modcrafting.ultrabans.commands.Starve;
import com.modcrafting.ultrabans.commands.Tempban;
import com.modcrafting.ultrabans.commands.Unban;
import com.modcrafting.ultrabans.commands.Version;
import com.modcrafting.ultrabans.commands.Warn;
import com.modcrafting.ultrabans.db.SQLDatabases;
import com.nijikokun.bukkit.Permissions.Permissions;

public class UltraBan extends JavaPlugin {

	public final static Logger log = Logger.getLogger("Minecraft");
	Permissions CurrentPermissions = null;
	public SQLDatabases db = new SQLDatabases();
	public String maindir = "plugins/UltraBan/";
	public HashSet<String> bannedPlayers = new HashSet<String>();
	public HashSet<String> bannedIPs = new HashSet<String>();
	public HashSet<String> jailed = new HashSet<String>();
	public Map<String,Long> tempBans = new HashMap<String,Long>();
	public Map<String, EditBan> banEditors = new HashMap<String, EditBan>();
	private final UltraBanPlayerListener playerListener = new UltraBanPlayerListener(this);
	private final UltraBanBlockListener blockListener = new UltraBanBlockListener(this);
	public boolean autoComplete;
	public boolean useFines;
	public boolean useJail;
	public boolean useLockdown;
	public boolean useEmpty;
	public boolean useSpawn;
	public boolean useStarve;
	public boolean useWarn;
	public boolean usePermaban;
	
	public void onDisable() {
		tempBans.clear();
		bannedPlayers.clear();
		bannedIPs.clear();
		jailed.clear();
		banEditors.clear();
		System.out.println("UltraBan disabled.");
	}
	protected void createDefaultConfiguration(String name) {
		File actual = new File(getDataFolder(), name);
		if (!actual.exists()) {

			InputStream input =
				this.getClass().getResourceAsStream("/" + name);
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
		YamlConfiguration Config = (YamlConfiguration) getConfig();
		PluginDescriptionFile pdfFile = this.getDescription();
		new File(maindir).mkdir();
		createDefaultConfiguration("config.yml"); //Swap for new setup
		this.autoComplete = Config.getBoolean("auto-complete", true);
		this.useFines = Config.getBoolean("useFines", true);
		this.useJail = Config.getBoolean("useJail", true);
		this.useLockdown = Config.getBoolean("useLockdown", true);
		this.useEmpty = Config.getBoolean("useEmpty", true);
		this.useSpawn = Config.getBoolean("useForceRespawn", true);
		this.useStarve = Config.getBoolean("useStarve", true);
		this.useWarn = Config.getBoolean("useWarn", true);
		this.usePermaban = Config.getBoolean("usePermaban", true);
		loadCommands();
		loadPerms();
		db.initialize(this);	
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Priority.Highest, this);
		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Monitor, this);
		pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, playerListener, Priority.High, this);
		pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
		log.log(Level.INFO,"[" + pdfFile.getName() + "]" + " version " + pdfFile.getVersion() + " is enabled!" );
		
	}
	private void loadPerms(){
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
	public void loadCommands(){

		getCommand("ban").setExecutor(new Ban(this));
		getCommand("checkban").setExecutor(new Check(this));
		getCommand("editban").setExecutor(new EditCommand(this));
		getCommand("empty").setExecutor(new Empty(this));
		getCommand("importbans").setExecutor(new Import(this));
		getCommand("exportbans").setExecutor(new Export(this));
		if(useFines) getCommand("fine").setExecutor(new Fine(this));
		getCommand("uhelp").setExecutor(new Help(this));
		getCommand("ipban").setExecutor(new Ipban(this));
		getCommand("kick").setExecutor(new Kick(this));
		getCommand("ureload").setExecutor(new Reload(this));
		getCommand("forcespawn").setExecutor(new Spawn(this));
		getCommand("starve").setExecutor(new Starve(this));
		getCommand("tempban").setExecutor(new Tempban(this));
		getCommand("unban").setExecutor(new Unban(this));
		getCommand("uversion").setExecutor(new Version(this));
		getCommand("warn").setExecutor(new Warn(this));
		getCommand("jail").setExecutor(new Jail(this));
		getCommand("permaban").setExecutor(new Perma(this));
		getCommand("lockdown").setExecutor(new Lockdown(this));
	}
	public void setObject(){
		
	}
}

		


