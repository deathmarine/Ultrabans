package com.modcrafting.ultrabans.live;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Config {
	private String name;
	private String version;
	private String author;
	public Config(){
		InputStream in = this.getClass().getResourceAsStream("/live.cfg");
		InputStreamReader inr = new InputStreamReader(in);
		BufferedReader br =	new BufferedReader(inr);
		try{
			String line;
			while ((line = br.readLine()) != null){
				if(line.contains("name=")) name=line.split("=")[1];
				if(line.contains("version=")) version=line.split("=")[1];
				if(line.contains("author=")) author=line.split("=")[1];
			}
			in.close();
			inr.close();
			br.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
	}
	public String getVersion(){
		return version;
	}
	public String getName(){
		return name;
	}
	public String getAuthor(){
		return author;
	}
}
