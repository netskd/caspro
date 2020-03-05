package syncer.nets.com.pl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Wapro {
	public Connection connObj;
	
	private String baza="", host="", user="", pass="";
	private int idKategoriiMag=0;
	private List<Item> listaTowarow;
	
	public Wapro( String host, String user, String pass, String baza ) {
		this.host=host;
		this.user=user;
		this.pass=pass;
		this.baza=baza;
		getDbConnection();
		listaTowarow=new ArrayList();
		getTowaryKategorii();
	}
  
    public void getDbConnection() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            connObj = DriverManager.getConnection("jdbc:sqlserver://" + host + ":1433;databaseName="+ baza +";user="+ user +";password="+ pass);
            if(connObj != null) {
                DatabaseMetaData metaObj = (DatabaseMetaData) connObj.getMetaData();
                //System.out.println("Driver Name?= " + metaObj.getDriverName() + ", Driver Version?= " + metaObj.getDriverVersion() + ", Product Name?= " + metaObj.getDatabaseProductName() + ", Product Version?= " + metaObj.getDatabaseProductVersion());
            }
        } catch(Exception sqlException) {
            sqlException.printStackTrace();
        }
    }
    
    void getTowaryKategorii() {
    	String sql="select a.*, c.CENA_BRUTTO as cena from ARTYKUL a left join CENA_ARTYKULU c on c.ID_ARTYKULU=a.ID_ARTYKULU where ID_KATEGORII=7 and id_ceny=1";
    	try {
			Statement stmt = connObj.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
	            //System.out.println(rs.getString("Nazwa") + " : " + rs.getString("Plu")+ " : " + rs.getString("Cena"));
	            //public Item(String nazwa, int kodZwiazany, int plu, int cena, String sklad) {
	            Item item=new Item(rs.getString("Nazwa"),rs.getInt("id_artykulu"),rs.getInt("Plu"), (int)rs.getDouble("Cena")*100, rs.getString("Uwagi"));
	            listaTowarow.add(item);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
    List <Item> getItems() {
		return listaTowarow;	
    }
}
