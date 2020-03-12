package syncer.nets.com.pl;

import java.util.List;

public class sender {
	Wapro mag;
	List<String> scalesIps=null;
	konfiguracja okno;
	
	
	public sender( Wapro mag, konfiguracja okno ){
		this.mag=mag;
		this.okno=okno;
	}
	
	public void wyslij(){
		List<Item> listaTowarow=null;
		listaTowarow=mag.getItems();
		if ( listaTowarow.size()==0 ){
			if (mag==null || !mag.connected() ){
				okno.addLog("Brak połączenia z wf-mag"); return;
			}
			if (  mag.getLog().length()>0 )
			{
				okno.addLog(mag.getLog());
			}else
			{
				okno.addLog("Zerowa lista towarów - nie ma przypisanych PLU lub zły magazyn, itp..." );
			}
		}else{
			// wysylamy towary na wagi...
			List<String>ips=okno.getScalesIps();
			
			for ( String ip: ips)
			{
				try{
					Cl5500 waga=new Cl5500(ip);
					for ( Item i: listaTowarow ){
						if ( !waga.addPlu(i) ) 
						{
							okno.addLog( "Towar " + i + " nie został dodany\nBłąd " + waga.getLastError() );
						}
					}
					waga=null;
					okno.addLog(ip+" -> Wysłałem " + listaTowarow.size() + " towarow na wage" );
				}catch( Exception ex ){
					if ( ex instanceof java.net.SocketTimeoutException ) okno.addLog(ip+ " -> Brak połączenia z wagą");
					okno.addLog(ip+ " -> " + ex.getLocalizedMessage());
				}
			}
		}
		okno.addLog("Zakończono wysyłkę" );
	}
}
