package syncer.nets.com.pl;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class Syncer implements IpCheckerInterface {
	private Wapro mag=null;
	public static void main( String str[] ) {
		
		konfiguracja okno=new konfiguracja();
		
		
		Cl5500 waga1=new Cl5500("10.0.10.65");
		
		Wapro mag=new Wapro( okno.getHost(), okno.getUser(), okno.getPass(), okno.getDb(), okno.getMag() );
		if ( !mag.connected() )
				okno.pokaz();
		trayIt(okno);
		try{
			Item item;
			//String txt=waga.wezSklad(82, 0);
			/* 
			for ( int i=1;i<11;i++){
				//System.out.println(i);
				
				item=waga.getPlu(i);
				
				if ( item!=null ) {
					//System.out.println(item);
					
					//waga1.addPlu(item);
					//waga1.deletePlu(i);
					  
					
					System.out.println(item);
				}
		
				//waga1.deletePlu(i);
			}
		*/
			/**/
			//String txt=waga.wezSklad(82, 0);
			//item = waga1.getPlu(799);
			
			for ( Item i :mag.getItems() ) {
				System.out.println(i);
				waga1.addPlu(i);
			}
 
		}catch( Exception e ){
			e.printStackTrace();
		}
	}

	@Override
	public boolean check(String ip) {
		
		Cl5500 waga=new Cl5500(ip);
		try {
			waga.getDate();
			return true;
		} catch (IOException e) {// TODO Auto-generated catch block
			return false;
		}
	}
	
	
	public static void trayIt(konfiguracja okno) {
		 if (!SystemTray.isSupported()) {
	            System.out.println("SystemTray is not supported");
	            return;
	        }
	        final PopupMenu popup = new PopupMenu();
	        final TrayIcon trayIcon =
	                new TrayIcon(Toolkit.getDefaultToolkit().createImage("images/Scale-icon.png"));
	        final SystemTray tray = SystemTray.getSystemTray();
	       
	        // Create a pop-up menu components
	        MenuItem aboutItem = new MenuItem("About");
	        CheckboxMenuItem cb1 = new CheckboxMenuItem("Automatycznie przesyłaj");
	        //CheckboxMenuItem cb2 = new CheckboxMenuItem("Set tooltip");
	        MenuItem konfiguracja = new MenuItem("Konfiguracja");
	        MenuItem exitItem = new MenuItem("Zakończ");
	        exitItem.addActionListener( e-> { System.exit(0); } );
	        konfiguracja.addActionListener(e->{okno.pokaz();});
	        //Add components to pop-up menu
	        popup.add(aboutItem);
	        popup.addSeparator();
	        popup.add(cb1);
	        //popup.add(cb2);
	        popup.addSeparator();
	        popup.add(konfiguracja);

	        popup.add(exitItem);
	       
	        trayIcon.setPopupMenu(popup);
	       
	        try {
	            tray.add(trayIcon);
	        } catch (AWTException e) {
	            System.out.println("TrayIcon could not be added.");
	        }
	}
	
}
