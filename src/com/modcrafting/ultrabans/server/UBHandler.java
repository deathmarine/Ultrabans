package com.modcrafting.ultrabans.server;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import com.modcrafting.ultrabans.live.security.RSAServerCrypto;

public class UBHandler extends Handler{
	ConnectionHandler out;
	RSAServerCrypto crypto;
	public UBHandler(ConnectionHandler connectionHandler,RSAServerCrypto sc){
		out=connectionHandler;
		crypto=sc;
	}
	@Override
	public void close() throws SecurityException {
		//Do nothing
	}

	@Override
	public void flush() {
		//Do nothing
	}

	@Override
	public void publish(LogRecord record) {
		if (!isLoggable(record))
			return;
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
		Date now = new Date();
		now.setTime(System.currentTimeMillis());
		String s = ".console. "+format.format(now)+" ["+record.getLevel()+"] "+record.getMessage();
		try {
			out.write(crypto.encrypt(s.getBytes()));
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}

}
