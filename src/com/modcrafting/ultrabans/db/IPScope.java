package com.modcrafting.ultrabans.db;

import java.net.InetAddress;
import java.util.StringTokenizer;

import org.bukkit.Bukkit;

import com.modcrafting.ultrabans.UltraBan;

public class IPScope {
	UltraBan plugin;

	public IPScope(UltraBan ultraBan) {
		this.plugin = ultraBan;
	}

		public void combine(String ip){
			String[] array= ipArray(ip);
			main(array);
			return;
		}
		
		public String[] ipArray(String ip){
			String strIP = ip.replace("*", "0");
			String strIP2 = ip.replace("*", "255");
			
			String[] array = new String[2];
			//Not sure if this will work but testing will tell.
			for (int i = 0; i < array.length; i ++) {
			    array[i] = new String(strIP + i);
			}
			for (int i = 1; i < array.length; i ++) {
			    array[i] = new String(strIP2 + i);
			}
			return array;
		}
		
	    public void main(String[] args) {
	        try {
	            long start = host2long(args[0]);
	            long end = host2long(args[1]);
	            for (long i=start; i<=end; i++) {
					plugin.bannedIPs.add(long2dotted(i));
					Bukkit.banIP(long2dotted(i));
					plugin.db.setAddress("scope", long2dotted(i));
					plugin.db.addPlayer("scope", "scopeip", "admin", 0, 1);
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }


	    public static long ip2long(InetAddress ip) {
	        long l=0;
	        byte[] addr = ip.getAddress();
	        if (addr.length == 4) { //IPV4
	            for (int i=0;i<4;++i) {
	                l += (((long)addr[i] &0xFF) << 8*(3-i));
	            }
	        } else { //IPV6
	            return 0;  // I dont know how to deal with these
	        }
	        return l;
	    }


	    public static long host2long(String host) {
	        long ip=0;
	        if (!Character.isDigit(host.charAt(0))) return -1;
	        int[] addr = ip2intarray(host);
	        if (addr == null) return -1;
	        for (int i=0;i<addr.length;++i) {
	            ip += ((long)(addr[i]>=0 ? addr[i] : 0)) << 8*(3-i);
	        }
	        return ip;
	    }

	    public static int[] ip2intarray(String host) {
	        int[] address = {-1,-1,-1,-1};
	        int i=0;
	        StringTokenizer tokens = new StringTokenizer(host,".");
	        if (tokens.countTokens() > 4) return null;
	        while (tokens.hasMoreTokens()) {
	            try {
	                address[i++] = Integer.parseInt(tokens.nextToken()) & 0xFF;
	            } catch(NumberFormatException nfe) {
	                return null;
	            }
	        }
	        return address;
	    }

	    public static String long2dotted(long address) {
	        StringBuffer sb = new StringBuffer();
	        for (int i = 0, shift = 24; i < 4; i++, shift -= 8) {
	            long value = (address >> shift) & 0xff;
	            sb.append(value);
	            if (i != 3) {
	                sb.append('.');
	            }
	        }
	        return sb.toString();
	    }

	

}
