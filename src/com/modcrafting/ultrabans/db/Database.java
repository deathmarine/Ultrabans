package com.modcrafting.ultrabans.db;

import com.modcrafting.ultrabans.UltraBan;


public abstract class Database {

	protected UltraBan plugin;
/**
 * Wickity Wickity Wooh
 */
	public abstract void initialize(UltraBan plugin);

	public abstract boolean removeFromBanlist(String player);

	public abstract void addPlayer(String p, String reason, String admin, long tempTime);

	public abstract String getBanReason(String player);

	public abstract void addAddress(String p, String ip);

}