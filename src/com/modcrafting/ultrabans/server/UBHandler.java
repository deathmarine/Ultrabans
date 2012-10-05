package com.modcrafting.ultrabans.server;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class UBHandler extends Handler{
	PrintWriter out;
	public UBHandler(PrintWriter pw){
		out=pw;
	}
	@Override
	public void close() throws SecurityException {
		out.close();
	}

	@Override
	public void flush() {
		out.flush();
	}

	@Override
	public void publish(LogRecord record) {
		if (!isLoggable(record))
			return;
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		Date now = new Date();
		now.setTime(System.currentTimeMillis());
		out.println(format.format(now)+" ["+record.getLevel()+"] "+record.getMessage());
	}

}
