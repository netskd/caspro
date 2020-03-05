package syncer.nets.com.pl;

import java.io.IOException;

public class Syncer {
	public static void main( String str[] ) {
		Cl5500 waga=new Cl5500("172.16.123.247");
		Cl5500 waga1=new Cl5500("10.0.10.65");
		Wapro mag=new Wapro("10.0.10.7","kd","11111","wapro_demo2");
		
		try{
			Item item;
			//String txt=waga.wezSklad(82, 0);
			/**/
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
		/**/
			/**/
			String txt=waga.wezSklad(82, 0);
			for ( Item i :mag.getItems() ) {
				System.out.println(i);
				//i.setSkladTxt("Skład jakiś sobie wymyślimy\n\rMoże nawet trzeba będzie coś wpisać\n\rjajko...       ");
				waga1.addPlu(i);
			}
			/**/
		}catch( Exception e ){
			e.printStackTrace();
		}
	}
}
