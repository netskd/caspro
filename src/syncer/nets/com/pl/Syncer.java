package syncer.nets.com.pl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Syncer implements IpCheckerInterface {

	private static List<Item> listaTowarow=new ArrayList<Item>();
	private static Wapro mag=null;
	private static konfiguracja okno;
	
	public static void main( String str[] ) {
		
		okno= new konfiguracja( );
		
		//Cl5500 waga1=new Cl5500("10.0.10.65");
		
		mag=new Wapro( okno.getHost(), okno.getUser(), okno.getPass(), okno.getDb(), okno.getMag() );
		sender wysylacz=new sender( mag, okno );
		okno.setSender(wysylacz);
		if ( !mag.connected() )
				okno.pokaz();
		else
		{
			okno.trayIt();
			//okno.setUndecorated(true);
		}
		while ( true ){
			if ( okno.autoWysylanie )
				wysylacz.wyslij();
			try { Thread.sleep(1000*okno.getPauza()); } catch( Exception ex ){};
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
	
}
