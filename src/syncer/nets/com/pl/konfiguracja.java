package syncer.nets.com.pl;


import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.CheckboxMenuItem;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static javax.crypto.Cipher.DECRYPT_MODE;
import static javax.crypto.Cipher.ENCRYPT_MODE;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.MutableComboBoxModel;
import javax.swing.SpringLayout;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.BadLocationException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class konfiguracja extends JFrame {

	/**
	 * 
	 */
	
	private JTextField serwer, login;
	private JPasswordField password;
	private JComboBox<Object> db;
	JList<String> wagi=new JList<String>();
	DefaultListModel<String> wagiLista=new DefaultListModel<String>();
	private String serwerTxt="", loginTxt="", passTxt="", dbTxt="";
	final SystemTray tray = SystemTray.getSystemTray();
	final private static Image img=Toolkit.getDefaultToolkit().createImage( konfiguracja.class.getResource("/waga.png"));
	final static TrayIcon trayIcon = new TrayIcon(img);
	Syncer sync=null;
	public boolean autoWysylanie=true;
    
	
	private JTabbedPane tabbedPane=null;
	private boolean canTray=false;
	public int pauza=5*60;			// pauza w sekundach
	private JTextArea logger=new JTextArea(15,40);
	private sender wysylacz=null;
	private JScrollPane scroller=null;
	
	protected Component makeTextPanel(String text) {
	    JPanel panel = new JPanel(false);
	    JLabel filler = new JLabel(text);
	    filler.setHorizontalAlignment(JLabel.CENTER);
	    panel.setLayout(new GridLayout(1, 1));
	    panel.add(filler);
	    return panel;
	}
	
	private void tworzPodklad(){
		setTitle("Konfiguracja konektora.");
		tabbedPane = new JTabbedPane();
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		JPanel baza=new JPanel(false);
		baza.setLayout(new BorderLayout() );

		
		JPanel panelDolny=new JPanel( false );
		panelDolny.setLayout(new FlowLayout( FlowLayout.RIGHT ) );
		
		JButton ok=new JButton("Ok");
		ok.addActionListener( e -> { if (saveXML()) if ( trayIt()  ) this.setVisible(false); } );
		JButton cancel=new JButton("Anuluj");
		cancel.addActionListener(e -> { if ( trayIt()  ) this.setVisible(false);  });
		JButton zamknij=new JButton("Wyjdź");
		zamknij.addActionListener(e -> { System.exit(0);  });
		panelDolny.add(ok);panelDolny.add(cancel);panelDolny.add(zamknij);
		
		baza.add(panelDolny,BorderLayout.SOUTH);
		
		//JComponent panel1 = (JComponent) makeTextPanel("Ustawienia WF-Mag");
		JComponent panel1=getMagPanel();
		tabbedPane.addTab("Ustawienia WF-Mag", null, panel1,
		                  "Zmiana ustawień dostępu do bazy wf-mag");
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

		JComponent panel2 = getScalesPanel();
		tabbedPane.addTab("Lista wag", null, panel2,
		                  "Dodaj/usuń obsługiwane wagi");
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

		JComponent panel3 = this.getLogPanel();
		tabbedPane.addTab("Log transmisji", null, panel3,
		                  "Tu zobaczysz logi wysyłania");
		tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);

		panel1.setPreferredSize(new Dimension(460, 250));

		baza.add(tabbedPane,BorderLayout.CENTER);
		this.add(baza);
		
		
		// uzupelnienie jezeli są ustawione
		List<String> combo=new ArrayList<String>();
		combo.add(dbTxt);
		DefaultComboBoxModel<Object> model = new DefaultComboBoxModel<Object>( combo.toArray() );
		db.setModel( model );db.setSelectedIndex(0);
		
		this.serwer.setText(serwerTxt);
		this.login.setText(loginTxt);
		this.password.setText(passTxt);
		
		this.pack();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width/2-this.getSize().width/2, dim.height/2-this.getSize().height/2);
	}
	
	private static final long serialVersionUID = 1L;
	
	private void addSearchSQLListener( JButton szukajSql, JTextField serwer ){
		JFrame frame=this;
		
		
		// obsługa buttona szukania
		
		szukajSql.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				IpChecker sqle=new IpChecker( new IpCheckerInterface(){

					@Override
					public boolean check(String ip) {
						Socket s;
						 try {
				                s = new Socket();
				                s.setReuseAddress(true);
				                SocketAddress sa = new InetSocketAddress(ip, 1433);
				                s.connect(sa, 5 * 100);
				                s.close();
				             } catch (IOException e) {
				                //if ( e.getMessage().equals("Connection refused")) 
				                    return false;
				             }
						 s=null;
						 return true;
					}
				});
				
				if ( sqle.ips.size()>0 )
				{
					if (sqle.ips.size()==1 )
						serwer.setText(sqle.ips.get(0));
					else{
						serwer.setText( (String) JOptionPane.showInputDialog(null, "Wybierz...",
						        "Znalazlem kilka serwerów", JOptionPane.QUESTION_MESSAGE, null, 
						        sqle.ips.toArray(), // Array of choices
						        sqle.ips.get(0)) 
								 );
					}
				}
				else
				{
					JOptionPane.showMessageDialog( frame, "Nie znalazłem serwera MSQL w sieci lokalnej.\nPodaj adres ręcznie." ,"Przykra informacja!",
					        JOptionPane.ERROR_MESSAGE);
				}
				sqle=null; // garbage IT !!!!!
			}
		});

	}
	
	
	
	private JPanel getMagPanel(){
		JPanel p=new JPanel();
		p.setLayout( new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(5,5,5,5);
        //constraints.weightx = 0.1;
        
        constraints.gridx = 0;
        constraints.gridy = 0;     
		JLabel serwerLabel=new JLabel("Serwer bazy:", JLabel.LEADING);
		p.add(serwerLabel, constraints);
		
		constraints.gridx = 1;
		constraints.weightx = 0.69;
		serwer=new JTextField(20);
		serwerLabel.setLabelFor(serwer);
		p.add(serwer, constraints);
		
		constraints.gridx = 2;  
		constraints.weightx = 0.1;
		JButton szukajSql=new JButton("..");szukajSql.setToolTipText("Szukaj serwera w sieci");
		addSearchSQLListener(szukajSql,serwer);
		p.add(szukajSql, constraints);
		
		constraints.gridx = 0;
        constraints.gridy = 1;     
        JLabel loginLabel=new JLabel("Login:", JLabel.LEADING);
        p.add(loginLabel, constraints);
        
        constraints.gridx = 1;
        constraints.gridy = 1;     
        login=new JTextField(20);
        p.add(login, constraints);
        
        constraints.gridx = 0;
        constraints.gridy = 2;     
        JLabel passLabel=new JLabel("Hasło:", JLabel.LEADING);
        p.add( passLabel, constraints);
        
        constraints.gridx = 1;
        constraints.gridy = 2;     
        password=new JPasswordField(20);
        p.add(password, constraints);
        
        constraints.gridx = 0;
        constraints.gridy = 3;     
        JLabel dbLabel=new JLabel("Baza Danych:", JLabel.LEADING);
        p.add( dbLabel, constraints);
        
        constraints.gridx = 1;
        constraints.gridy = 3;     
        db=new JComboBox<Object>();
        addDbListener(db);
        p.add(db, constraints);
        
		return p;
	}
	
	private JPanel getScalesPanel(){
		JPanel p=new JPanel();
		p.setLayout(new BorderLayout());
		JPanel lewy=new JPanel(), prawy=new JPanel();
		
		JList<String> wagi=new JList<String>(wagiLista);
		lewy.setLayout(new BorderLayout());
		
		p.add( lewy, BorderLayout.CENTER);
		p.add( prawy, BorderLayout.EAST);
		
		JButton dodaj=new JButton("+");
		dodaj.addActionListener( e -> { List l=pokazWyborDodaniaWag(); dodajWagi(l); } );
		
		JButton usun=new JButton("-");
		prawy.setLayout(new GridLayout(2,1));
		prawy.add(dodaj);prawy.add(usun);
		usun.addActionListener(e -> { wagiLista.remove(wagi.getSelectedIndex()); } );
		
		wagi.setLayoutOrientation(JList.VERTICAL);
		JScrollPane listScroller = new JScrollPane(wagi);
		lewy.add(listScroller,BorderLayout.CENTER);
		
		return p;
	}
	
	private JPanel getLogPanel(){
		JPanel p=new JPanel();
		p.setLayout(new BorderLayout());
		JPanel gora=new JPanel(), prawy=new JPanel();
		gora.setLayout(new BorderLayout());
		JPanel centralny=new JPanel();
		centralny.setLayout(new BorderLayout());
		
		p.add( gora, BorderLayout.NORTH);
		p.add( centralny, BorderLayout.CENTER);
		
		JLabel freqLbl=new JLabel("Częstość ");
		String[] freqValues = { "1 minuta", "5 minut", "10 minut", "30 minut", "1 godzina" , "6 godzin" };
		JComboBox<String> freq=new JComboBox<String>(freqValues);
		freq.setSelectedIndex(1);
		freq.addActionListener(e->{
			switch( freq.getSelectedIndex() ){
				case 0:pauza=1*60;break;
				case 1:pauza=5*60;break;
				case 2:pauza=10*60;break;
				case 3:pauza=30*60;break;
				case 4:pauza=1*60*60;break;
				case 5:pauza=6*60*60;break;
			}
		});
		
		JButton wyslijTeraz=new JButton("Wyślij teraz");
		wyslijTeraz.addActionListener( e-> { wysylacz.wyslij(); } );
		gora.add(  freqLbl, BorderLayout.WEST );
		gora.add( freq , BorderLayout.CENTER );
		gora.add( wyslijTeraz, BorderLayout.EAST );
		logger.setEditable(false);
		scroller= new JScrollPane(logger);
		scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);  
	    scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);  
	  
		centralny.add(scroller,BorderLayout.CENTER);
		
		return p;
	}
	
	
	
	private void dodajWagi(List<?> l) {
		for( Object waga: l )
		{
			if ( !wagiLista.contains((String)waga) ) wagiLista.addElement((String) waga); 
		}
	}

	private List<String> pokazWyborDodaniaWag() {
		List<String> l=new ArrayList<String>();
		String[] buttons = { "Automat", "Ręcznie", "Anuluj" };    
		int returnValue = JOptionPane.showOptionDialog(null, "Dodawanie wag\n\nWybierz w jaki sposób dodać wagi.\nJeśli wybierzesz \"Automat\", zostaną dodane wszystkie \nwykryte wagi dostępne w sieci lokalnej...\nJeśli wybierzesz \"Ręcznie\" Będziesz mógł podać adres IP wagi\n\n", "Dodaj",
		        JOptionPane.INFORMATION_MESSAGE, 0, null, buttons, buttons[0]);
		
		switch( returnValue ){
			case 2: // anuluj
				break;
			case 1: // ręcznie
				 	String m = JOptionPane.showInputDialog(this, "Podaj adres ip wagi","Ręcznie dodaj wagę", JOptionPane.QUESTION_MESSAGE);
				 	if ( m!=null && m.length()>0 ) l.add(m.trim());
				break;
			case 0: //automatycznie
					IpChecker wagi=new IpChecker(  new IpCheckerInterface(){
		
						@Override
						public boolean check(String ip) {
							Cl5500 waga=new Cl5500(ip);
							try {
								waga.getDate();
								return true;
							} catch (IOException e) {
								return false;
							}
						};			
					});
					for ( String waga: wagi.ips )
						l.add(waga);
				break;
		}
		return l;
	}

	private void addDbListener(JComboBox<Object> db) {
		db.addPopupMenuListener( new PopupMenuListener(){

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				Wapro mag=new Wapro( serwer.getText(),login.getText(), new String(password.getPassword()) );
				List <String> lista=mag.getDatabasesList();
				DefaultComboBoxModel<Object> model = new DefaultComboBoxModel<Object>( lista.toArray() );
				db.setModel( model );
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
		});
	}

	public konfiguracja(  ){
		//sync=s;
		System.out.println( img );
		trayIcon.setImageAutoSize(true);
		//trayIcon.displayMessage("Uwaga",  "111", TrayIcon.MessageType.NONE);
		setIconImage(img);
		readXML();
		canTray(); 
		//tworzPodklad();	
		//setResizable(false);
	}

	public boolean readXML(){
		File fXmlFile = new File("./syncer.xml");
		try{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);

			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("wf-mag");
			Node nNode = nList.item(0);
			Element eElement = (Element) nNode;
			String kombo=decryptSimple(eElement.getElementsByTagName("serwer").item(0).getTextContent(),"jajcarze");
			int pierwszyN=kombo.indexOf('\n');
			int drugiN=kombo.indexOf('\n',pierwszyN+1);
			this.serwerTxt=kombo.substring(0,pierwszyN);
			//serwer.setText(serwerTxt.trim());
			this.loginTxt=kombo.substring(pierwszyN+1,drugiN);
			//login.setText(user.trim());
			passTxt=kombo.substring(drugiN+1);
			//password.setText(haslo.trim());
			dbTxt=eElement.getElementsByTagName("baza").item(0).getTextContent();
			
			NodeList listaWag=doc.getElementsByTagName("wagi");
			
			wagiLista.clear();
			NodeList n=((Element)listaWag.item(0)).getElementsByTagName("ip");
			
			for ( int i=0 ; i<n.getLength(); i++ )
			{
				wagiLista.addElement(n.item(i).getTextContent());
			}
			
		}catch( Exception ex )
		{
			//ex.printStackTrace();
			System.out.println("Pewnie nie ma pliku" );
			return false;
		}
		return true;
	}
	public boolean saveXML(){
    	try{
    		 Wapro mag=new Wapro( serwer.getText().trim(),login.getText().trim(), new String(password.getPassword()) );
    		 if (!mag.connected()){
    			 mag=null;
    			 JOptionPane.showMessageDialog(null, "Nie można połączyć z wapro", "Uwaga: " , JOptionPane.INFORMATION_MESSAGE);
    			 return false;
    		 }
	    	 DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
	         DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
	         Document document = documentBuilder.newDocument();
	
	         // root element
	         Element root = document.createElement("konfiguracja");
	         document.appendChild(root);
	         Element wapro = document.createElement("wf-mag");
	         root.appendChild(wapro);
	         Element server = document.createElement("serwer");
	         String kombo=serwer.getText().trim()+"\n"+login.getText().trim()+"\n"+new String(password.getPassword());
	         server.appendChild(document.createTextNode(encryptSimple(kombo,"jajcarze")));
	         wapro.appendChild(server);
	         Element baza = document.createElement("baza");baza.appendChild(document.createTextNode(db.getSelectedItem().toString()));
	         wapro.appendChild(baza);
	         Element wagiElements = document.createElement("wagi");
	         wapro.appendChild(wagiElements);
	         
	         // zapisujemy liste wag
	         
	         
	         for ( int i=0; i<wagiLista.getSize(); i++ ) {
	             Element ipWagi = document.createElement("ip");
	             ipWagi.appendChild(document.createTextNode(String.valueOf( wagiLista.getElementAt(i))));
	             wagiElements.appendChild(ipWagi);
	         }
	         
	         
	         TransformerFactory transformerFactory = TransformerFactory.newInstance();
	         Transformer transformer = transformerFactory.newTransformer();
	         DOMSource domSource = new DOMSource(document);
	         StreamResult streamResult = new StreamResult(new File("./syncer.xml"));
	         transformer.transform(domSource, streamResult);
	         System.out.println("Done creating XML File");
    	}
    	catch( Exception ex ){
    		ex.printStackTrace();
    		
    		return false;
    	}
    	return true;
    }
	
	public static String encrypt(String text, String pass) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            Key key = new SecretKeySpec(messageDigest.digest(pass.getBytes("UTF-8")), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(ENCRYPT_MODE, key);

            byte[] encrypted = cipher.doFinal(text.getBytes("UTF-8"));
            byte[] encoded = Base64.getEncoder().encode(encrypted);
            return new String(encoded, "UTF-8");

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
            throw new RuntimeException("Cannot encrypt", e);
        }
    }

    public static String decrypt(String text, String pass) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            Key key = new SecretKeySpec(messageDigest.digest(pass.getBytes("UTF-8")), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(DECRYPT_MODE, key);

            byte[] decoded = Base64.getDecoder().decode(text.getBytes("UTF-8"));
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, "UTF-8");

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
            throw new RuntimeException("Cannot decrypt", e);
        }
    }

    public static String encryptSimple(String text, String pass) {
        try {
           

            byte[] encrypted = text.getBytes();
            for ( int i=0; i< encrypted.length; i++ )
            	encrypted[i]=(byte) (encrypted[i]+i);
            byte[] encoded = Base64.getEncoder().encode(encrypted);
            return new String(encoded, "UTF-8");

        } catch ( UnsupportedEncodingException e ) {
            throw new RuntimeException("Cannot encrypt", e);
        }
    }

    public static String decryptSimple(String text, String pass) {
        try {
          
            byte[] decoded = Base64.getDecoder().decode(text.getBytes("UTF-8"));
          
            for ( int i=0; i< decoded.length; i++ )
                decoded[i]=(byte) (decoded[i]-i);
            return new String(decoded, "UTF-8");

        } catch ( UnsupportedEncodingException e ) {
            throw new RuntimeException("Cannot decrypt", e);
        }
    }

	public void pokaz() {
		if ( this.tabbedPane==null ){
			this.tworzPodklad();
			setResizable(false);
			pack();
		}
		setVisible(true);
		tray.remove(trayIcon);
	}

	public String getHost() {
		return serwerTxt.trim();
	}
	
	public String getUser() {
		return loginTxt.trim();
	}
	
	public String getPass() {
		return passTxt.trim();
	}
	
	public String getDb() {
		if ( dbTxt.indexOf('[')<0 ) return"";
		return dbTxt.substring(0, dbTxt.indexOf('[')-1).trim();
	}
	
	public String getMag() {
		if ( dbTxt.indexOf('[')<0 ) return"";
		return dbTxt.substring(dbTxt.indexOf('/')+1, dbTxt.lastIndexOf(']') ).trim();
	}
	
	public boolean canTray(){
		 if (!SystemTray.isSupported()) {
	            System.out.println("SystemTray is not supported");
	            return false;
	        }
	        final PopupMenu popup = new PopupMenu();
	       
	        // Create a pop-up menu components
	        MenuItem aboutItem = new MenuItem("About");
	        CheckboxMenuItem cb1 = new CheckboxMenuItem("Automatycznie przesyłaj");
	        cb1.setState(autoWysylanie);
	        cb1.addActionListener(e -> { autoWysylanie=cb1.getState(); } );
	        MenuItem konfiguracja = new MenuItem("Konfiguracja");
	        MenuItem exitItem = new MenuItem("Zakończ");
	        exitItem.addActionListener( e-> { System.exit(0); } );
	        konfiguracja.addActionListener(e->{pokaz();});
	        //Add components to pop-up menu
	        popup.add(aboutItem);
	        popup.addSeparator();
	        popup.add(cb1);
	        //popup.add(cb2);
	        popup.addSeparator();
	        popup.add(konfiguracja);

	        popup.add(exitItem);
	       
	        trayIcon.setPopupMenu(popup);
	        canTray=true;
	        return true;
	}
	
	public boolean trayIt() {
			if ( !canTray ) return false;
	        try {
	        	SystemTray.getSystemTray().add(trayIcon);
	        } catch (AWTException e) {
	            System.out.println("TrayIcon could not be added.");
	            return false;
	        }
	        return true;
	}

	public void addLog(String str) {
		SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss |");
		Date date = new Date(System.currentTimeMillis());
		try{
			if ( logger.getLineCount()>200 ){
				int end = logger.getLineEndOffset(0);
				logger.replaceRange("", 0, end);
			}
			str = formatter.format(date) + " " + str + "\n";
			logger.append(str);
				
			logger.scrollRectToVisible(logger.modelToView(logger.getDocument().getLength()));
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public List<String> getScalesIps() {
		return (ArrayList<String>) Collections.list(wagiLista.elements());
	}

	public void setSender(sender wysylacz) {
		this.wysylacz=wysylacz;
	}

	public int getPauza() {
		//switch( cb1. )
		return pauza;
	}
}
