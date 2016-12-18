package com.example.carcontrollerapp;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class Tools {
	
	public String GetLocalIP(){
		String sLocalIP = new String();
		InetAddress sResultIP;
		
		try {
			Enumeration<NetworkInterface> netInterfaces = netInterfaces = NetworkInterface.getNetworkInterfaces();
			while (netInterfaces.hasMoreElements()){
				NetworkInterface ni = netInterfaces.nextElement();
				Enumeration<InetAddress> ips = ni.getInetAddresses();
		
				while (ips.hasMoreElements()){
					sResultIP = ips.nextElement();
					if( (!sResultIP.getHostAddress().contains(":")) && (!sResultIP.isLoopbackAddress()) )
					sLocalIP = sLocalIP + sResultIP.getHostAddress() + "\n\n";
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sLocalIP;
	}
}
