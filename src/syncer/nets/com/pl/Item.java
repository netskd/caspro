package syncer.nets.com.pl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Item {
	private int plu=-1;
	private int plu2=-1;
	private String prefix=null;
	private double cena=0;
	private String nazwa=null;
	private String formatKodu=null;
	private boolean wagowy=true;
	private int ilosc=1;
	private String skladTxt=null;
	public int skladInt=0;
	private int label=0;
	private int przydatny=0;
	
	public Item() {
	}
	
	public String toString(){
		return getPlu() + "|" + plu2 +"|" + (wagowy?"wazony|":"niewazony|") + getName() + "|cena:" + String.format("%1.2f PLN", cena) + "|skład:" + skladInt;
	}

	public String getName() {
				return nazwa;
	}
	
	public boolean getWagowy() { return wagowy; };

	public void setPlu ( int plu ) {  this.plu=plu; };
	public void setPlu2( int plu ) {  this.plu2=plu; };
	public void setIlosc( int number ) {  this.ilosc=number; };
	public void setNazwa ( String x  ) {  this.nazwa=x; };
	public void setSkladInt ( int x  ) {  this.skladInt=x; };
	public void setDni( int x  ) {  this.przydatny=x; };
	public void setLabel( int x  ) {  this.label=x; };
	public void setFormat ( String x  ) {  this.formatKodu=x; };
	public void setPrefix ( String x  ) {  this.prefix=x; };
	public void setCena ( int x  ) { 
		this.cena=x/100.0; // trzeba powalczyc, nie wiemy jak i co tu wchodzi
	};
	public void setWagowy( boolean x ) { this.wagowy=x; };
	//public boolean jestSklad(){ return skladInt>0;};
	public void setSkladTxt(String str ){
		skladTxt=str;
	}
	
	public int getPlu() {
		// TODO Auto-generated method stub
		return plu;
	}
	
	private void addPluBlock(ByteArrayOutputStream bs) throws IOException{
		String pomocniczy= String.format("F=%02x.4C,04:",Cl5500.pola.plu.getValue());
		bs.write(pomocniczy.getBytes());
		ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(plu);
        bb.flip();
        bs.write(bb.array());
	}
	private void addPlu2Block(ByteArrayOutputStream bs) throws IOException{
		String pomocniczy= String.format("F=%02x.4C,04:",Cl5500.pola.nasze.getValue());
		bs.write(pomocniczy.getBytes());
		ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(plu2);
        bb.flip();
        bs.write(bb.array());
	}
	private void addTypeBlock(ByteArrayOutputStream bs) throws IOException{
		String pomocniczy= String.format("F=%02x.4D,01:",Cl5500.pola.plutype.getValue());
		bs.write(pomocniczy.getBytes());
		byte b=(byte) (wagowy?1:0);
		
        bs.write(b);
	}
	
	public byte[] getAddString(){
		ByteArrayOutputStream bs=new ByteArrayOutputStream(),bs1=new ByteArrayOutputStream();
		String pomocniczy;
		//byte crc=0;
		pomocniczy="W02A" + String.format("%06x", plu) + ",01L0000:";
		
		try {
				addPriceBlock(bs1);
				addPluBlock(bs1);
				addPlu2Block(bs1);
				addTypeBlock(bs1);
				addNameBlock(bs1);
				crc(bs1);
				pomocniczy="W02A" + String.format("%06x,01L%02x:", plu,bs1.size()-1);
				bs.write(pomocniczy.getBytes());
				bs.write(bs1.toByteArray());
				bs1=null; // garbage it
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return bs.toByteArray();
	}
	
	private byte crc( ByteArrayOutputStream bs ) throws IOException{
		byte ret=0;
		byte[] b=bs.toByteArray();
		String str=new String(b);
		for ( int i=0;i<b.length;i++ ){
			ret^=b[i];
		}	
		bs.write(b);
		return ret;
	}
	
	private void addNameBlock(ByteArrayOutputStream bs) throws IOException {
		String pomocniczy= String.format("F=%02x.4C,%02x:",Cl5500.pola.nazwa.getValue(),nazwa.length());
		bs.write( pomocniczy.getBytes("ISO-8859-2") );
		bs.write( nazwa.getBytes("ISO-8859-2") );
	}

	private void addPriceBlock(ByteArrayOutputStream bs) throws IOException {
		String pomocniczy= String.format("F=%02x.4C,04:",Cl5500.pola.cena.getValue());
		bs.write(pomocniczy.getBytes());
		ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        int cena=(int) (this.cena*100.0);
        bb.putInt( cena );
        bb.flip();
        bs.write(bb.array());
	}

	public byte[] getDeleteString(){
		return null;
	}
	
	public boolean jestSklad(){
		return skladTxt.length()>0;
	}
	public byte[] getIngredientsString( ) throws IOException{
		ByteArrayOutputStream bs1=new ByteArrayOutputStream(),bs=new ByteArrayOutputStream();
		
		if ( skladTxt.length()<512 ){
			String block0= String.format("X=0.D=%02x.%s", skladTxt.length(), skladTxt );
			bs1.write(block0.getBytes() );
		}else
		{
			String block0= String.format("X=00.D=%02x.%s", 512, skladTxt.substring( 0,512 ) );
			bs1.write(block0.getBytes() );
			block0= String.format("X=01.D=%02x.%s", 512, skladTxt.substring( 0,512 ) );
			bs1.write(block0.getBytes() );
			crc(bs);
		}
		
		String naglowek= String.format("W30F01,%dL%02x:",plu,bs1.size()-1 ); // przypisujemy do id skladu numer plu towaru...
		bs.write(naglowek.getBytes());
		bs.write(bs1.toByteArray());
		return bs.toByteArray() ;
	}
	
}
