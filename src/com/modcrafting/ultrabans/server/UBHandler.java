package com.modcrafting.ultrabans.server;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import com.modcrafting.ultrabans.security.RSAServerCrypto;

public class UBHandler extends Handler{
	OutputStream out;
	RSAServerCrypto crypto;
	public UBHandler(OutputStream pw,RSAServerCrypto sc){
		out=pw;
		crypto=sc;
	}
	@Override
	public void close() throws SecurityException {
		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void flush() {
		try {
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
			e.printStackTrace();
		}
	}

}
