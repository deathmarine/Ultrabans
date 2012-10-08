package com.modcrafting.ultrabans.util;

import com.boxysystems.jgoogleanalytics.LoggingAdapter;
import com.modcrafting.ultrabans.Ultrabans;

public class ErrorHandler implements LoggingAdapter {
	Ultrabans plugin;
	public ErrorHandler(Ultrabans instance){
		plugin=instance;
	}
	@Override
	public void logError(String errorMessage) {
		plugin.getLogger().info(errorMessage);
	}
	@Override
	public void logMessage(String message) {
		plugin.getLogger().info(message);
	}

}
