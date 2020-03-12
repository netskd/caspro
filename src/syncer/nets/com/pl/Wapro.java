package syncer.nets.com.pl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.microsoft.sqlserver.jdbc.SQLServerException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Wapro {
	public Connection connObj;
	private String log="";
	private String baza="", host="", user="", pass="", magazyn="";
	private int idKategoriiMag=0;
	private List<Item> listaTowarow;
	private boolean connected=false;
	
	public Wapro( String host, String user, String pass, String baza, String magazyn ) {
		this.host=host;
		this.user=user;
		this.pass=pass;
		this.baza=baza;
		this.magazyn=magazyn;
		getDbConnection();
		listaTowarow=new ArrayList();
		//getTowaryKategorii();
	}
	
	
	
	public Wapro( String host, String user, String pass ) {
		this.host=host;
		this.user=user;
		this.pass=pass;
		//this.baza=baza;
		getDbConnection();
		//listaTowarow=new ArrayList();
		//getTowaryKategorii();
	}
	
	public List<String> getDatabasesList(){
		String sql="SELECT name FROM master.sys.databases";
		List<String> bazy=new ArrayList<String>();
		List<String> bazy1=new ArrayList<String>();
		try {
			if ( connObj==null ) return bazy;
			Statement stmt = connObj.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				bazy1.add(rs.getString("name"));
			}
			rs.close();
			
			for ( String baza: bazy1 ){
				rs = stmt.executeQuery("SELECT * FROM " + baza +".INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME LIKE 'WFM%' " );
				if ( rs.next() ){
		        	rs.close();
		        	rs=stmt.executeQuery("SELECT NAZWA,ID_MAGAZYNU FROM " + baza +".DBO.MAGAZYN" );
		        	while( rs.next() )
		        		bazy.add(baza + " [" + rs.getString("NAZWA").trim() +"/"+rs.getString("ID_MAGAZYNU").trim()+ "]");
		        }
	        };
	        rs.close();stmt.close();
		}catch (Exception e) {
			if ( e instanceof SQLServerException ) return bazy;
			e.printStackTrace();
			return bazy;
		}
		return bazy;
	}
	


	public void getDbConnection() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            DriverManager.setLoginTimeout(1);
            connObj = DriverManager.getConnection("jdbc:sqlserver://" + host + ":1433;databaseName="+ baza +";user="+ user +";password="+ pass);
            if(connObj != null) {
                DatabaseMetaData metaObj = (DatabaseMetaData) connObj.getMetaData();
                connected=true;
                //System.out.println("Driver Name?= " + metaObj.getDriverName() + ", Driver Version?= " + metaObj.getDriverVersion() + ", Product Name?= " + metaObj.getDatabaseProductName() + ", Product Version?= " + metaObj.getDatabaseProductVersion());
            }
        } catch(Exception sqlException) {
            //sqlException.printStackTrace();
        	connected=false;
        }
    }
	
	boolean connected(){
		return connected;
	}
    
    void getTowaryKategorii() {
    	if ( !connected ){
    		getDbConnection();
    	}
    	listaTowarow.clear();
    	log="";
    	String sql="select a.*, c.CENA_BRUTTO as cena from ARTYKUL a left join CENA_ARTYKULU c on c.ID_ARTYKULU=a.ID_ARTYKULU where a.plu>0 and a.id_magazynu=" + this.magazyn + " and id_ceny=1";
    	try {
			Statement stmt = connObj.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
	            Item item=new Item(rs.getString("Nazwa"),rs.getInt("id_artykulu"),rs.getInt("Plu"), (int)rs.getDouble("Cena")*100, rs.getString("Uwagi"));
	            listaTowarow.add(item);
			}
			
		} catch (Exception e) {
			log=e.getLocalizedMessage();
			//e.printStackTrace();
		}
    	
    }
    
    public String getLog(){
    	return log;
    }
    
    List <Item> getItems() {
    	getTowaryKategorii();
		return listaTowarow;	
    }
    
}
