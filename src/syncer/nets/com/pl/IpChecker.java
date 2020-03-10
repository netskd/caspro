package syncer.nets.com.pl;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Callable;


public class IpChecker {
	String classIpAddr="";
	List<String> ips;
	private boolean ended=false;
	IpCheckerInterface listener;

	public IpChecker(IpCheckerInterface listener){
		String classIpAddr=getIp();
		addListener(listener);
		IpCheck(classIpAddr.substring(0,classIpAddr.lastIndexOf('.')));
	}
	
	public void addListener( IpCheckerInterface obj )
	{
		listener=obj;
	}
	
	private void IpCheck(String classIp) {
		classIpAddr=classIp;
		ips=new ArrayList<String>();
		
		Thread[] watki=new Thread[256];
		
		for ( int i=1; i<255; i++ ){
			final String addr=classIpAddr+"."+i;
			watki[i-1]=new Thread(){
				public void run(){
					if (listener.check(addr) ){
						System.out.println( "Dodaje na adresie:"+addr);
						ips.add(addr);
					}
				};
			};
			
			watki[i-1].start();
		}
	
		while ( !ended ){
			ended=true;
			for ( int i=0; i<254; i++ )
				if ( watki[i].isAlive() ) ended=false;
		}
		
	}

	public IpChecker( String ip, IpCheckerInterface listener ) {
		addListener(listener);
		IpCheck(ip.substring(0,ip.lastIndexOf('.')));
	}
	
	public boolean finished(){
		return ended;
	}
	
	private String getIp(){
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
	            NetworkInterface iface = interfaces.nextElement();
	            // filters out 127.0.0.1 and inactive interfaces
	            if (iface.isLoopback() || !iface.isUp() || iface.isPointToPoint() || iface.isVirtual() )
	                continue;

	            Enumeration<InetAddress> addresses = iface.getInetAddresses();
	            while(addresses.hasMoreElements()) {
	                InetAddress addr = addresses.nextElement();
	                if (addr.isLinkLocalAddress() ) continue;
	                String ip = addr.getHostAddress();
	                System.out.println(iface.getDisplayName() + " " + ip);
	                classIpAddr=ip.substring(0,ip.lastIndexOf('.'));
	                return ip;
	            }
	        }
	    } catch (SocketException e) {
	        throw new RuntimeException(e);
	    }
		return "";
	}
	
	String check( Callable<Boolean> check){
		return null;	
	}
}
