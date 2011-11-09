package com.modcrafting.ultrabans.hooks;

import java.util.logging.Logger;

import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.plugins.Economy_3co;
import net.milkbowl.vault.economy.plugins.Economy_BOSE6;
import net.milkbowl.vault.economy.plugins.Economy_BOSE7;
import net.milkbowl.vault.economy.plugins.Economy_Essentials;
import net.milkbowl.vault.economy.plugins.Economy_MultiCurrency;
import net.milkbowl.vault.economy.plugins.Economy_iConomy4;
import net.milkbowl.vault.economy.plugins.Economy_iConomy5;
import net.milkbowl.vault.economy.plugins.Economy_iConomy6;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class Hooks extends Checkvault{
		public static final Logger log = Logger.getLogger("Minecraft");
		private static final JavaPlugin Plugin = null;
		public static Economy economy = null;
		public Permission permission = null;
		public static Vault vault = null;
	
		public boolean hookVault(){
		if(vault.isEnabled()) return true;
		return false;
		}
	public boolean setupEconomy(){
	RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
			return (economy != null);
	}
	public boolean setupPermissions() {
		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
		if (permissionProvider != null) {
			permission = permissionProvider.getProvider();
		}
			return (permission != null);
	}
	public boolean getVault() {
		RegisteredServiceProvider<Vault> providervault = getServer().getServicesManager().getRegistration(net.milkbowl.vault.Vault.class);
		if (providervault != null) {
			vault = providervault.getProvider();
		}
			return (vault != null);
	}
	public void loadEconomy() {
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

		// Javaplugin to Plugin
		if (packageExists(new String[] { "com.iConomy.iConomy", "com.iConomy.system.Account", "com.iConomy.system.Holdings" })) {
			Economy icon5 = new Economy_iConomy5(Plugin);
			getServer().getServicesManager().register(net.milkbowl.vault.economy.Economy.class, icon5, this, ServicePriority.High);
			log.info(String.format("[%s][Economy] iConomy 5 found: %s", getDescription().getName(), icon5.isEnabled() ? "Loaded" : "Waiting"));
		}

		// Javaplugin to Plugin
		if (packageExists(new String[] { "com.iCo6.iConomy" })) {
			Economy icon6 = new Economy_iConomy6(Plugin);
			getServer().getServicesManager().register(Economy.class, icon6, this, ServicePriority.High);
			log.info(String.format("[%s][Economy] iConomy 6 found: %s", getDescription().getName(), icon6.isEnabled() ? "Loaded" : "Waiting"));
		}
	}
/*	private void loadPermission() {
		// Try to load PermissionsEx
		if (packageExists(new String[] { "ru.tehkode.permissions.bukkit.PermissionsEx" })) {
			Permission ePerms = new Permission_Permissions3(this);
			getServer().getServicesManager().register(Permission.class, ePerms, this, ServicePriority.Highest);
			log.info(String.format("[%s][Permission] PermissionsEx found: %s", getDescription().getName(), ePerms.isEnabled() ? "Loaded" : "Waiting"));
		}

		//Try to load bPermissions
		if (packageExists(new String[] {"de.bananaco.permissions.worlds.WorldPermissionsManager"} )) {
			Permission bPerms = new Permission_bPermissions(this);
			getServer().getServicesManager().register(Permission.class, bPerms, this, ServicePriority.Highest);
			log.info(String.format("[%s][Permission] bPermissions found: %s", getDescription().getName(), bPerms.isEnabled() ? "Loaded" : "Waiting"));
		}

		// Try to load GroupManager
		if (packageExists(new String[] { "org.anjocaido.groupmanager.GroupManager" })) {
			Permission gPerms = new Permission_GroupManager(this);
			getServer().getServicesManager().register(Permission.class, gPerms, this, ServicePriority.High);
			log.info(String.format("[%s][Permission] GroupManager found: %s", getDescription().getName(), gPerms.isEnabled() ? "Loaded" : "Waiting"));
		}
		// Try to load Permissions 3 (Yeti)
		if (packageExists(new String[] { "com.nijiko.permissions.ModularControl" })) {
			Permission nPerms = new Permission_Permissions3(this);
			getServer().getServicesManager().register(Permission.class, nPerms, this, ServicePriority.High);
			log.info(String.format("[%s][Permission] Permissions 3 (Yeti) found: %s", getDescription().getName(), nPerms.isEnabled() ? "Loaded" : "Waiting"));
		}

	}
	*/

	private static boolean packageExists(String[] packages) {
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
