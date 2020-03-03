package syncer.nets.com.pl;

import java.io.IOException;

public class Syncer {
	public static void main( String str[] ) {
		Cl5500 waga=new Cl5500("172.16.123.247");
		Cl5500 waga1=new Cl5500("10.0.10.65");
		
		try{
			Item item;
			String txt=waga.wezSklad(82, 0);
			/**/
			for ( int i=1;i<100;i++){
				//System.out.println(i);
				
				item=waga.getPlu(i);
				
				if ( item!=null ) {
					//System.out.println(item);
					/**/
					waga1.addPlu(item);
					//waga1.deletePlu(i);
					  
					 /**/
					System.out.println(item);
				}
		
				//waga1.deletePlu(i);
			}
		/**/
		}catch( IOException e ){
			e.printStackTrace();
		}
	}
}
