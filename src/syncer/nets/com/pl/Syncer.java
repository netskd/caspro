package syncer.nets.com.pl;

import java.io.IOException;

public class Syncer {
	public static void main( String str[] ) {
		Cl5500 waga=new Cl5500("172.16.123.247");
		Cl5500 waga1=new Cl5500("10.0.10.162");
		
		try{
			Item item;
			for ( int i=1;i<10;i++){
				//System.out.println(i);
				
				item=waga.getPlu(i);
				if ( item!=null ) {
					//System.out.println(item);
					//waga1.write(item.getAddString());
					//item=waga1.getPlu(i);
					System.out.println(item);
				}
				/**/
				//waga1.deletePlu(i);
			}
		}catch( IOException e ){
			e.printStackTrace();
		}
	}
}
