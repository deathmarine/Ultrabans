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

import net.milkbowl.vault.Vault;
import net.milkbowl.vault.permission.Permission;
import net.milkbowl.vault.permission.plugins.Permission_GroupManager;
import net.milkbowl.vault.permission.plugins.Permission_Permissions3;
import net.milkbowl.vault.permission.plugins.Permission_PermissionsBukkit;
import net.milkbowl.vault.permission.plugins.Permission_PermissionsEx;
import net.milkbowl.vault.permission.plugins.Permission_SuperPerms;
import net.milkbowl.vault.permission.plugins.Permission_bPermissions;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.plugins.Economy_3co;
import net.milkbowl.vault.economy.plugins.Economy_BOSE6;
import net.milkbowl.vault.economy.plugins.Economy_BOSE7;
import net.milkbowl.vault.economy.plugins.Economy_Essentials;
import net.milkbowl.vault.economy.plugins.Economy_MultiCurrency;
import net.milkbowl.vault.economy.plugins.Economy_iConomy4;
import net.milkbowl.vault.economy.plugins.Economy_iConomy5;
import net.milkbowl.vault.economy.plugins.Economy_iConomy6;

import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
// Why you change bukkit
import org.bukkit.util.config.Configuration;

import com.modcrafting.ultrabans.commands.EditBan;
import com.modcrafting.ultrabans.commands.EditCommand;
import com.modcrafting.ultrabans.db.MySQLDatabase;
import com.modcrafting.ultrabans.util.Util;
import com.nijikokun.bukkit.Permissions.Permissions;
/**
 * Ultraban Bukkit Plugin
 * Created by Deathmarine
 */
@SuppressWarnings("deprecation")
public class UltraBan extends JavaPlugin {

	public static final Logger log = Logger.getLogger("Minecraft");
	Permissions CurrentPermissions = null;
	public MySQLDatabase db;
/*	
	public void launchDatabase(){	//For new configuration + alt database
		if (this.getConfig().getString("useMySql") != null)
			this.resendConfig();
		if (this.getConfig().getBoolean("useMySql")){
		MySQLDatabase db;
		}else{ 
		FlatFileDatabase db;
		}
	}
	*/
	Util util;
	String maindir = "plugins/UltraBan/";
	public HashSet<String> bannedPlayers = new HashSet<String>();
	public HashSet<String> bannedIPs = new HashSet<String>();
	public Map<String,Long> tempBans = new HashMap<String,Long>();
	public Map<String, EditBan> banEditors = new HashMap<String, EditBan>();
	private final UltraBanPlayerListener playerListener = new UltraBanPlayerListener(this);
	//TODO Setup new configuration
	public Configuration properties = new Configuration(new File("plugins/UltraBan/config.yml"));
	//public FileConfiguration properties = new FileConfiguration();
	public static Economy economy = null;
	public static Permission permission = null;
	public static Vault	vault = null;
	public boolean autoComplete;
	public boolean checkPermissions;
	public boolean checkEconomy; //Disables Fine
	public boolean getVault(){	//Checks if Vault is loaded
		if(vault.isEnabled()) return true;
			return false;
	}
	protected boolean setupEconomy(){	//Vault Economy
	RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
			return (economy != null);
	}
	protected boolean setupPermissions() {	//Vault Permissions
		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
		if (permissionProvider != null) {
			permission = permissionProvider.getProvider();
		}
			return (permission != null);
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
		
		if (this.getVault()){
			loadPermission(); 
			loadEconomy(); 
		}else{
			defaultPerms();
		}
		db = new MySQLDatabase();
		db.initialize(this); 
		PluginManager pm = getServer().getPluginManager();
		if (!this.isEnabled()) return;
		//if (!setupPermissions()) return;
		pm.registerEvent(Event.Type.PLAYER_LOGIN, playerListener, Priority.Highest, this);
		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Monitor, this);
		//reconstruct
		getCommand("editban").setExecutor(new EditCommand(this));
		
		PluginDescriptionFile pdfFile = this.getDescription();
		log.log(Level.INFO,pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
		log.info(String.format("[%s] Enabled Version %s", getDescription().getName(), getDescription().getVersion()));
		
	}
	private void loadEconomy() {	//Modified from Vault
		// Try to load MultiCurrency
		if (packageExists(new String[] { "me.ashtheking.currency.Currency", "me.ashtheking.currency.CurrencyList" })) {
			Economy econ = new Economy_MultiCurrency(this);
			getServer().getServicesManager().register(Economy.class, econ, this, ServicePriority.Normal);
			log.info(String.format("[%s][Economy] MultiCurrency found: %s", getDescription().getName(), econ.isEnabled() ? "Loaded" : "Waiting"));
		}

		// Try to load 3co
		if (packageExists(new String[] { "me.ic3d.eco.ECO" })) {
			Economy econ = new Economy_3co(this);
			getServer().getServicesManager().register(Economy.class, econ, this, ServicePriority.Normal);
			log.info(String.format("[%s][Economy] 3co found: %s", getDescription().getName(), econ.isEnabled() ? "Loaded" : "Waiting"));
		}

		// Try to load BOSEconomy
		if (packageExists(new String[] { "cosine.boseconomy.BOSEconomy", "cosine.boseconomy.CommandManager" })) {
			Economy bose6 = new Economy_BOSE6(this);
			getServer().getServicesManager().register(Economy.class, bose6, this, ServicePriority.Normal);
			log.info(String.format("[%s][Economy] BOSEconomy6 found: %s", getDescription().getName(), bose6.isEnabled() ? "Loaded" : "Waiting"));
		}

		// Try to load BOSEconomy
		if (packageExists(new String[] { "cosine.boseconomy.BOSEconomy", "cosine.boseconomy.CommandHandler" })) {
			Economy bose7 = new Economy_BOSE7(this);
			getServer().getServicesManager().register(net.milkbowl.vault.economy.Economy.class, bose7, this, ServicePriority.Normal);
			log.info(String.format("[%s][Economy] BOSEconomy7 found: %s", getDescription().getName(), bose7.isEnabled() ? "Loaded" : "Waiting"));
		}

		// Try to load Essentials Economy
		if (packageExists(new String[] { "com.earth2me.essentials.api.Economy", "com.earth2me.essentials.api.NoLoanPermittedException", "com.earth2me.essentials.api.UserDoesNotExistException" })) {
			Economy essentials = new Economy_Essentials(this);
			getServer().getServicesManager().register(net.milkbowl.vault.economy.Economy.class, essentials, this, ServicePriority.Low);
			log.info(String.format("[%s][Economy] Essentials Economy found: %s", getDescription().getName(), essentials.isEnabled() ? "Loaded" : "Waiting"));
		}

		// Try to load iConomy 4
		if (packageExists(new String[] { "com.nijiko.coelho.iConomy.iConomy", "com.nijiko.coelho.iConomy.system.Account" })) {
			Economy icon4 = new Economy_iConomy4(this);
			getServer().getServicesManager().register(net.milkbowl.vault.economy.Economy.class, icon4, this, ServicePriority.High);
			log.info(String.format("[%s][Economy] iConomy 4 found: ", getDescription().getName(), icon4.isEnabled() ? "Loaded" : "Waiting"));
		}

		// Try to load iConomy 5
		if (packageExists(new String[] { "com.iConomy.iConomy", "com.iConomy.system.Account", "com.iConomy.system.Holdings" })) {
			Economy icon5 = new Economy_iConomy5(this);
			getServer().getServicesManager().register(net.milkbowl.vault.economy.Economy.class, icon5, this, ServicePriority.High);
			log.info(String.format("[%s][Economy] iConomy 5 found: %s", getDescription().getName(), icon5.isEnabled() ? "Loaded" : "Waiting"));
		}

		// Try to load iConomy 6
		if (packageExists(new String[] { "com.iCo6.iConomy" })) {
			Economy icon6 = new Economy_iConomy6(this);
			getServer().getServicesManager().register(Economy.class, icon6, this, ServicePriority.High);
			log.info(String.format("[%s][Economy] iConomy 6 found: %s", getDescription().getName(), icon6.isEnabled() ? "Loaded" : "Waiting"));
		}
	}
	private void loadPermission() {		//Modified from Vault
		// Try to load PermissionsEx
		if (packageExists(new String[] { "ru.tehkode.permissions.bukkit.PermissionsEx" })) {
			Permission ePerms = new Permission_PermissionsEx(vault);
			getServer().getServicesManager().register(Permission.class, ePerms, this, ServicePriority.Highest);
			log.info(String.format("[%s][Permission] PermissionsEx found: %s", getDescription().getName(), ePerms.isEnabled() ? "Loaded" : "Waiting"));
			log.info(String.format("[%s] - Warning Using PEX can cause SuperPerms compatibility issues with non-Vault enabled plugins.", getDescription().getName()));
		}

		//Try loading PermissionsBukkit
		if (packageExists(new String[] {"com.platymuus.bukkit.permissions.PermissionsPlugin"} )) {
			Permission pPerms = new Permission_PermissionsBukkit(vault);
			getServer().getServicesManager().register(Permission.class, pPerms, this, ServicePriority.Highest);
			log.info(String.format("[%s][Permission] PermissionsBukkit found: %s", getDescription().getName(), pPerms.isEnabled() ? "Loaded" : "Waiting"));
		}

		//Try to load bPermissions
		if (packageExists(new String[] {"de.bananaco.permissions.worlds.WorldPermissionsManager"} )) {
			Permission bPerms = new Permission_bPermissions(vault);
			getServer().getServicesManager().register(Permission.class, bPerms, this, ServicePriority.Highest);
			log.info(String.format("[%s][Permission] bPermissions found: %s", getDescription().getName(), bPerms.isEnabled() ? "Loaded" : "Waiting"));
		}

		// Try to load GroupManager
		if (packageExists(new String[] { "org.anjocaido.groupmanager.GroupManager" })) {
			Permission gPerms = new Permission_GroupManager(vault);
			getServer().getServicesManager().register(Permission.class, gPerms, this, ServicePriority.High);
			log.info(String.format("[%s][Permission] GroupManager found: %s", getDescription().getName(), gPerms.isEnabled() ? "Loaded" : "Waiting"));
		}
		// Try to load Permissions 3 (Yeti)
		if (packageExists(new String[] { "com.nijiko.permissions.ModularControl" })) {
			Permission nPerms = new Permission_Permissions3(vault);
			getServer().getServicesManager().register(Permission.class, nPerms, this, ServicePriority.High);
			log.info(String.format("[%s][Permission] Permissions 3 (Yeti) found: %s", getDescription().getName(), nPerms.isEnabled() ? "Loaded" : "Waiting"));
		}

		Permission perms = new Permission_SuperPerms(vault);
		getServer().getServicesManager().register(Permission.class, perms, this, ServicePriority.Lowest);
		log.info(String.format("[%s][Permission] SuperPermissions loaded as backup permission system.", getDescription().getName()));

	}
	private static boolean packageExists(String[] packages) { //Modified from Vault
		try {
			for (String pkg : packages) {
				Class.forName(pkg);
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
