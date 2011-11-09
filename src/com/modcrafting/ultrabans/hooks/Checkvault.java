package com.modcrafting.ultrabans.hooks;

import org.bukkit.plugin.Plugin;

public abstract class Checkvault implements Plugin{
	public abstract void loadEconomy();
	public abstract boolean setupEconomy();
	public abstract boolean setupPermissions();
	public abstract boolean getVault();
	public abstract boolean hookVault();
			
}
