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

import net.milkbowl.vault.economy.Economy;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import com.modcrafting.ultrabans.commands.Ban;
import com.modcrafting.ultrabans.commands.Check;
import com.modcrafting.ultrabans.commands.CheckIP;
import com.modcrafting.ultrabans.commands.DupeIP;
import com.modcrafting.ultrabans.commands.EditBan;
import com.modcrafting.ultrabans.commands.EditCommand;
import com.modcrafting.ultrabans.commands.Empty;
import com.modcrafting.ultrabans.commands.Export;
import com.modcrafting.ultrabans.commands.Fine;
import com.modcrafting.ultrabans.commands.Help;
import com.modcrafting.ultrabans.commands.History;
import com.modcrafting.ultrabans.commands.Import;
import com.modcrafting.ultrabans.commands.Ipban;
import com.modcrafting.ultrabans.commands.Jail;
import com.modcrafting.ultrabans.commands.Kick;
import com.modcrafting.ultrabans.commands.Lockdown;
import com.modcrafting.ultrabans.commands.Mute;
import com.modcrafting.ultrabans.commands.Perma;
import com.modcrafting.ultrabans.commands.Reload;
import com.modcrafting.ultrabans.commands.Rules;
import com.modcrafting.ultrabans.commands.Spawn;
import com.modcrafting.ultrabans.commands.Starve;
import com.modcrafting.ultrabans.commands.Tempban;
import com.modcrafting.ultrabans.commands.Unban;
import com.modcrafting.ultrabans.commands.Version;
import com.modcrafting.ultrabans.commands.Warn;
import com.modcrafting.ultrabans.db.IPScope;
import com.modcrafting.ultrabans.db.SQLDatabases;

public class UltraBan extends JavaPlugin {

	public final static Logger log = Logger.getLogger("Minecraft");
	public SQLDatabases db = new SQLDatabases();
	public IPScope ipscope = new IPScope(this);
	public String maindir = "plugins/UltraBan/";
	public HashSet<String> bannedPlayers = new HashSet<String>();
	public HashSet<String> bannedIPs = new HashSet<String>();
	public HashSet<String> jailed = new HashSet<String>();
	public HashSet<String> muted = new HashSet<String>();
	public Map<String, Long> tempBans = new HashMap<String, Long>();
	public Map<String, EditBan> banEditors = new HashMap<String, EditBan>();
	private final UltraBanPlayerListener playerListener = new UltraBanPlayerListener(this);
	private final UltraBanBlockListener blockListener = new UltraBanBlockListener(this);
	public net.milkbowl.vault.permission.Permission permission = null;
	public net.milkbowl.vault.economy.Economy economy = null;
	public boolean autoComplete;
	public boolean useFines;
	public boolean useJail;
	public boolean useLockdown;
	public boolean useEmpty;
	public boolean useSpawn;
	public boolean useStarve;
	public boolean useWarn;
	public boolean usePermaban;
	public boolean useRules;
	
	public void onDisable() {
		tempBans.clear();
		bannedPlayers.clear();
		bannedIPs.clear();
		jailed.clear();
		muted.clear();
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
		this.useRules = Config.getBoolean("useRules", true);
		loadCommands();
		if (Config != null) log.log(Level.INFO, "[" + pdfFile.getName() + "]" + " Configuration: config.yml Loaded!");
		db.initialize(this);	
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(playerListener, this);
		pm.registerEvents(blockListener, this);
		log.log(Level.INFO, "[" + pdfFile.getName() + "] Listeners enabled, Server is secured.");
		log.log(Level.INFO,"[" + pdfFile.getName() + "]" + " version " + pdfFile.getVersion() + " has been initialized!" );
		
	}
	public Boolean setupPermissions()
    {
        RegisteredServiceProvider<net.milkbowl.vault.permission.Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }
	public boolean setupEconomy(){
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
			if (economyProvider != null) {
				economy = economyProvider.getProvider();
			}
				return (economy != null);
		}
	public void loadCommands(){

		getCommand("ban").setExecutor(new Ban(this));
		getCommand("checkban").setExecutor(new Check(this));
		getCommand("checkip").setExecutor(new CheckIP(this));
		getCommand("dupeip").setExecutor(new DupeIP(this));
		getCommand("editban").setExecutor(new EditCommand(this));
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
		getCommand("rules").setExecutor(new Rules(this));
		getCommand("unban").setExecutor(new Unban(this));
		getCommand("uversion").setExecutor(new Version(this));
		getCommand("warn").setExecutor(new Warn(this));
		getCommand("ujail").setExecutor(new Jail(this));
		getCommand("permaban").setExecutor(new Perma(this));
		getCommand("lockdown").setExecutor(new Lockdown(this));
		getCommand("umute").setExecutor(new Mute(this));
		getCommand("history").setExecutor(new History(this));
	}
}

		


