package syncer.nets.com.pl;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class konfiguracja extends JFrame {

	/**
	 * 
	 */
	
	private JTextField serwer, login;
	private JPasswordField password;
	private JComboBox db;
	
	private JTabbedPane tabbedPane;
	
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
		JButton cancel=new JButton("Anuluj");
		cancel.addActionListener(e -> { this.dispose(); });
		panelDolny.add(ok);panelDolny.add(cancel);
		
		baza.add(panelDolny,BorderLayout.SOUTH);
		
		//JComponent panel1 = (JComponent) makeTextPanel("Ustawienia WF-Mag");
		JComponent panel1=getMagPanel();
		tabbedPane.addTab("Ustawienia WF-Mag", null, panel1,
		                  "Zmiana ustawień dostępu do bazy wf-mag");
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

		JComponent panel2 = (JComponent) makeTextPanel("Lista wag2");
		tabbedPane.addTab("Lista wag", null, panel2,
		                  "Dodaj/usuń obsługiwane wagi");
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

		JComponent panel3 = (JComponent) makeTextPanel("Log transmisji");
		tabbedPane.addTab("Log transmisji", null, panel3,
		                  "Tu zobaczysz co jest jeszcze do wysłania");
		tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);

		panel1.setPreferredSize(new Dimension(460, 250));

		baza.add(tabbedPane,BorderLayout.CENTER);
		this.add(baza);
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
				IpChecker sqle=new IpChecker( "10.0.10.0", new IpCheckerInterface(){

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
        db=new JComboBox();
        addDbListener(db);
        p.add(db, constraints);
        
		return p;
	}
	
	private void addDbListener(JComboBox db) {
		db.addPopupMenuListener( new PopupMenuListener(){

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				wapro
				
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
	}

	public konfiguracja(){
		tworzPodklad();
		pack();
		//setResizable(false);
	}

}
