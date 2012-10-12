package com.modcrafting.ultrabans.util;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.modcrafting.ultrabans.tracker.Track;

public class LoggerHandler extends Handler{
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
			Track.track(record.getLevel().getName()+"&"+record.getThrown()+"&"+record.getSourceClassName());
		}
		
	}

}
