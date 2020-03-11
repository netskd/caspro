package syncer.nets.com.pl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Enumeration;
import java.util.List;

public class Cl5500 {
	String name="";
	String ip;
	private Socket socket;
	protected boolean isConnected=false;
	private String lastError;
	
	public static enum pola{
		plu(1),
		nasze(11),
		plutype(4),
		cena(6),
		grupa(9),
		nazwa(10),
		ilosc(14),
		sklad(25),
		label(80),
		dni(16);
		private int value;
		
		pola(int i){
			this.value=i;
		}

		public int getValue() {
			// TODO Auto-generated method stub
			return value;
		}
	}
	
	
	public void setIp( String ip )
	{
		this.ip=ip;
	}
	
	public Cl5500( String ip ){
		setIp(ip);	
	}
	
	public Cl5500(){
		
	}
	
	private boolean openConnection() throws IOException{
		socket=new Socket();
		socket.connect(new InetSocketAddress(ip, 20304),3500);
		isConnected=true;
		return true;
	}
	
	private Item pobierzTowar(ByteArrayInputStream data ){
		byte b1,b2,b3;
		Item i=new Item();
		String str=data.toString();
		
		while( (b1=(byte)data.read()) !=-1 ){
			if ( b1!='F' ) continue;
			b2=(byte) data.read();
			if ( b2 != '=' ) continue;
			
			byte field[]=new byte[2],scode[]=new byte[2],len[]=new byte[2];
			
			// ok, jestesmy na poczatku bloku. tu czytam 2 bajty potem bedzie . lub , - czytamy do przecinka i potem dwa bajty dlugosci bloku
			//data.read();//konsumujemy =
			data.read( field,0,2 );
			if ( data.read() == '.' )
				data.read( scode,0,2 );data.read();// konsumujemy ,
			data.read(len,0,2);data.read();//konsumujemy :
			//byte[] dane=new byte[Integer.decode("0x"+new String(len))];
			int fld=Integer.decode("0x"+new String( field ));
			int lenInt=Integer.decode("0x"+new String( len ));
			
			//DataInputStream in = new DataInputStream(data);
			switch(fld){
				case 2://plu
				{
					byte[] numer=new byte[4];
					int plu=-1;
					data.read(numer,0,4);
					plu= ByteBuffer.wrap(numer).order(ByteOrder.LITTLE_ENDIAN).getInt();
					i.setPlu(plu);
					break;
				}
				case 11://nasze plu
				{
					byte[] numer=new byte[4];
					int plu=-1;
					data.read(numer,0,4);
					plu= ByteBuffer.wrap(numer).order(ByteOrder.LITTLE_ENDIAN).getInt();

					i.setPlu2(plu);
					break;
				}
					
				case 4: //plutype
				{
					byte[] numer=new byte[1];
					int wagowy=data.read();
					i.setWagowy(wagowy==1);
					break;
				}
				
				case 6://cena
				{
					byte[] numer=new byte[4];
					int plu=-1;
					//try {
					data.read(numer,0,4);
					plu= ByteBuffer.wrap(numer).order(ByteOrder.LITTLE_ENDIAN).getInt();
					i.setCena(plu);
					break;
				}
				
				case 10://nazwa
					byte[] b=new byte[lenInt];
					try{
						data.read(b, 0, lenInt);
						String nazwa=new String(b,"ISO-8859-2");
						i.setNazwa(nazwa);
					}catch( Exception e )
					{
						e.printStackTrace();
					}
					break;
				case 14://pcs
					{
						byte[] numer=new byte[2];
						int number=-1;
						//try {
						data.read(numer,0,2);
						number= ByteBuffer.wrap(numer).order(ByteOrder.LITTLE_ENDIAN).getShort();
						i.setIlosc(number);
						break;
					}
				case 25://sklad
					{
						byte[] numer=new byte[2];
						int number=-1;
						//try {
						data.read(numer,0,2);
						number= ByteBuffer.wrap(numer).order(ByteOrder.LITTLE_ENDIAN).getShort();
						i.setSkladInt(number);
						break;
					}
				case 80://label
				{
					byte[] numer=new byte[2];
					int number=-1;
					data.read(numer,0,2);
					number= ByteBuffer.wrap(numer).order(ByteOrder.LITTLE_ENDIAN).getShort();
					i.setLabel(number);
					break;
				}
				case 3://prefix
				{
					if ( lenInt==0) break;
					byte[] numer=new byte[2];
					int number=-1;
					data.read(numer,0,2);
					
					i.setPrefix( new String(numer) );
					break;
				}
					
				case 16://dni do spozycia
				{
					byte[] numer=new byte[2];
					int number=-1;
					//try {
					data.read(numer,0,2);
					number= ByteBuffer.wrap(numer).order(ByteOrder.LITTLE_ENDIAN).getShort();
					i.setDni(number);
					break;
				}
			}
			
		}
		return i;
	}
	
	public boolean isConnected() { return isConnected; };
	
	public boolean closeConnection() throws IOException{
		socket.getOutputStream().flush();
		socket.close();
		isConnected=false;
		return true;
	}
	
	public long readCasHeader() throws IOException{
		
		ByteArrayOutputStream bs=new ByteArrayOutputStream();
		byte byt;
		try {
			 socket.setSoTimeout(1000);
			 while ((byt=(byte) socket.getInputStream().read()) !=':'){
				 bs.write(byt);
			 }
			 //System.out.println("Head:" + bs.toString());
			 String header=bs.toString().substring(bs.toString().indexOf("L")+1);
			 return Long.decode("0x"+header);		 
		}catch( NumberFormatException ex ){
			//if ( bs.toString().substring(0,2).compareTo("^=")==0 )
				bs.reset();
				while ( (byt=(byte) socket.getInputStream().read()) != 10 ){
					bs.write(byt);
				}; // blad, odczytujemy stream do konca
				//System.out.println("Błąd:"+bs.toString().substring(1));
				return -Long.decode(bs.toString().substring(1));
			//return -1;
		}
	}
	
	public String getDate() throws IOException{
		String ret=null;
		if ( !isConnected ){
			openConnection();
		};
		
		socket.getOutputStream().write( "R45F01,00\n".getBytes() );
		try{
			int size=(int) readCasHeader();
			
			byte b[]=new byte[size];
			socket.getInputStream().read(b,0,size);
			ret=new String(b);
		}catch ( SocketTimeoutException | NegativeArraySizeException ex  ){
			closeConnection();
		}
		//R45F01,00
		return ret;
	}
	
	public Item getPlu(int plu) throws IOException{
		String ret=null;
		Item item = null;
		
		if ( !isConnected ){
			openConnection();
		};
		
		String str=("R14F01" + String.format("%06x,0\n", plu) );
		socket.getOutputStream().write( str.getBytes() );
		try{
			int size=(int) readCasHeader();
			if ( size<0 ) return null;
			byte b[]=new byte[size];
			socket.getInputStream().read(b,0,size);
			//System.out.println(new String(b));
			
			int crc;
			if ( socket.getInputStream().available()>0 )
				crc=socket.getInputStream().read(); // konsumujemy crc
			item=pobierzTowar(new ByteArrayInputStream(b));
			String txt="";
			if ( item.skladInt>0 )
			{
				for ( int i=0; i<2; i++ )
					txt += wezSklad(item.skladInt,i);
			}
			item.setSkladTxt(txt);
			ret=new String(b,"Windows-1250");
		}catch ( SocketTimeoutException | NegativeArraySizeException ex  ){
			closeConnection();
		}
		return item;
	}
	
	public String wezSklad(int numer, int wiersz) throws IOException{
		if ( !isConnected ){
			openConnection();
		};
		String str=("R31F01," + String.format("%04x%02d\n", numer, wiersz) );
		socket.getOutputStream().write( str.getBytes() );
		int size=(int) readCasHeader();
		if ( size<0 ) return "";
		byte[] buff=new byte[size];
		socket.getInputStream().read(buff,0,size);
		String ret=new String( buff,"ISO-8859-2");
		ret=ret.substring(ret.indexOf(".D=")+3 );
		ret=ret.substring(ret.indexOf('.')+1 );
		//while ( socket.getInputStream().read() != '\n') ; // czytamy do konca soketa
		return ret;
	}
	
	public boolean deletePlu(int plu) throws IOException {
		if (!isConnected ){
			openConnection();
		};
		String str=String.format( "C00F13,01%06x\n", plu);
		//System.out.println(str.toString());
		socket.getOutputStream().write( str.getBytes() );
		getRetValue();
		str=String.format( "C00F31,01%04x\n", plu);
		//System.out.println(str.toString());
		socket.getOutputStream().write( str.getBytes() );
		
		return getRetValue();
	}
	
	
	
	public boolean getRetValue() throws IOException{
		byte byt;
		lastError="";
		ByteArrayOutputStream bs=new ByteArrayOutputStream();
		while ( ( byt=(byte) socket.getInputStream().read())!=10 ){
			bs.write(byt);
		}
		byt=(byte) socket.getInputStream().read();
		String s=bs.toString();
		
		if ( s.contains(":E") ){
			lastError="s";
			System.out.println("Błąd:"+s);
			return false;
		}
		return true;
	}
	
	
	public boolean write( byte[] b) throws IOException, InterruptedException{
		if ( !isConnected ){
			openConnection();
		};
		socket.getOutputStream().write(b);
		//Thread.sleep(10);
		
		//System.out.println(bs.toString());
		//int size=(int) readCasHeader();
		//if ( size<0 ) return false;
		return getRetValue();
	}
	
	public boolean addPlu( Item i ) throws IOException, InterruptedException
	{
		boolean ret=write(i.getAddString());
		if ( i.jestSklad() ){
			byte[] addTxt=i.getIngredientsString();
			String txt=addTxt.toString();
			ret=write(addTxt);
		}
		return ret;
	}
	
	public boolean dodajSklad(){
		return true;
	}

	public String getLastError() {
		return lastError;
	}
	
	
	
	//R13F01000001,0
	//W02A00001,01L027F:^=01.*=01.$=0.&=010203F2.@=4F50.?=3.N=03A8.F=01.57,02:F=02.4C,04:F=04.4D,01:F=0A.53,15:BURACZKI ZASMA?ANE KPF=1E.53,00:F=1F.53,00:F=09.57,02:F=31.53,00:F=51.57,02:F=37.57,02:F=64.4C,04:F=03.53,00:F=0B.4C,04:F=0E.57,02:F=0F.42,01:F=1A.42,01:F=06.4C,04:?F=5B.4C,04:F=08.42,01:F=0D.4C,04:F=18.57,02:F=17.57,02:F=56.57,02:F=24.42,01:F=14.57,02:F=12.57,02:F=13.42,01:F=10.4C,04:F=11.42,01:F=16.57,02:F=19.57,02:F=23.57,02:F=32.57,02:F=46.57,02:F=5A.42,01:F=47.57,02:F=45.4C,04:F=40.57,02:F=44.4C,04:F=3C.42,01:F=3D.57,02:F=41.4C,04:F=3E.57,02:F=42.4C,04:U
}
