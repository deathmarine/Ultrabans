package com.modcrafting.ultrabans.db;

import java.util.logging.Level;

import com.modcrafting.ultrabans.Ultrabans;

public class Error {
	public static void execute(Ultrabans plugin, Exception ex){
		plugin.getLogger().log(Level.SEVERE, "Couldn't execute MySQL statement: ", ex);	
	}
	public static void close(Ultrabans plugin, Exception ex){
		plugin.getLogger().log(Level.SEVERE, "Failed to close MySQL connection: ", ex);
	}
}
