package com.modcrafting.ultrabans.util;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.boxysystems.jgoogleanalytics.FocusPoint;
import com.modcrafting.ultrabans.Ultrabans;

public class LoggerHandler extends Handler{
	Ultrabans plugin;
	public LoggerHandler(Ultrabans instance){
		plugin=instance;
	}
	@Override
	public void close() throws SecurityException {
		return;
		
	}

	@Override
	public void flush() {
		return;		
	}

	@Override
	public void publish(LogRecord record) {
		if(record.getLevel()!=Level.INFO){
			FocusPoint focusPoint = new FocusPoint(record.getLevel().getName()+"&"+record.getThrown()+"&"+record.getSourceClassName());
			plugin.tracker.trackAsynchronously(focusPoint);			
		}
		
	}

}
