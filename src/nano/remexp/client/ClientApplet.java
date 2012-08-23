/*
 * Copyright (c) 2011 by Tibor Gyalog, Raoul Schneider, Dino Keller,
 * Christian Wattinger, Martin Guggisberg, Dominic Bosch and The 
 * Regents of the University of Basel. All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software and its
 * documentation for any purpose, without fee, and without written agreement is
 * hereby granted, provided that the above copyright notice and the following
 * two paragraphs appear in all copies of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF BASEL BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * BASEL HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE UNIVERSITY OF BASEL SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE.  THE SOFTWARE PROVIDED HEREUNDER IS
 * ON AN "AS IS" BASIS, AND THE UNIVERSITY OF BASEL HAS NO OBLIGATION TO
 * PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 * Authors: Tibor Gyalog, Raoul Schneider, Dino Keller,
 * Christian Wattinger, Martin Guggisberg, Dominic Bosch <vexp@nano-world.net>
 * 
 */ 

package nano.remexp.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import nano.debugger.Debg;
import nano.remexp.Password;
import nano.remexp.client.awt.ImagePanel;
import nano.remexp.client.awt.LineSection;
import nano.remexp.client.awt.NetObserver;
import nano.remexp.client.net.ClientConnection;
import nano.remexp.client.net.SocketConnector;
import nano.remexp.net.NanoComm;
import nano.remexp.net.EventSocket;
import nano.remexp.net.NanoSocketObserver;
import nano.remexp.net.SocketReceiver;
import nano.remexp.net.StreamSocket;
import nano.remexp.net.StreamSocketInterface;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
/*import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;*/
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;

/**
 * This is the remote client for the virtual experiments, introduced in late 2011
 * 
 * @author Dominic Bosch
 * @version 2.0 30.09.2011
 */

/*
 * TODO List from group meeting:
 * integration tests
 * implementation into booking system of portal
 * save image dialog corrupt
 */

public class ClientApplet extends JApplet implements SocketReceiver, StreamSocketInterface {
	public static final String IMAGE_DIR = "images/";
	
	private static final long serialVersionUID = 1L;
	private static final int guiWidth = 550;
	private static final int guiHeight = 665;
	private JTextPane statusLine;
	private JComboBox comboBoxScanRange, comboBoxSample;
	private String lastPosition = "";
	private ClientGUI gui;
	private Style style;
	private ParameterPopup popupWindow;
	private SocketConnector satStream;
	private SocketConnector satEvent;
	private ClientConnection serverEvent;
	private StreamSocket serverStream;
	private NetObserver myImage;
	private LineSection myLiner;
	private String hostURL, userID, messageHistory;
	private int eventPort, streamPort;
	private NanoSocketObserver socketObserver;

	/**
	 * Initialization of the applet, trying to set Nimbus look and feel.
	 */
	public void init() {
		Debg.setDebugMode(null, Debg.DEBUGMODE_FULL, Debg.DEBUGMODE_NODEBUG);
		userID = null;
        messageHistory = "";
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					Debg.print("Nimbus look and feel set");
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			Logger.getLogger(ClientApplet.class.getName()).log(Level.SEVERE, null, ex);
			Debg.err("Nimbus class not found");
		} catch (InstantiationException ex) {
			Logger.getLogger(ClientApplet.class.getName()).log(Level.SEVERE, null, ex);
			Debg.err("Nimbus class not instantiated");
		} catch (IllegalAccessException ex) {
			Logger.getLogger(ClientApplet.class.getName()).log(Level.SEVERE, null, ex);
			Debg.err("Nimbus class not accessible");
		} catch (UnsupportedLookAndFeelException ex) {
			Logger.getLogger(ClientApplet.class.getName()).log(Level.SEVERE, null, ex);
			Debg.err("Nimbus class not supported");
		}

		String version = System.getProperty("java.version");
		String vendor = System.getProperty("java.vendor");
		Debg.print("Java Version: " + version + " from " + vendor);
        String[] arrVersion = version.split("\\.");
        if(getVersionInteger(arrVersion, 0) + (float)(getVersionInteger(arrVersion, 1))/10 < 1.6) {
        	Debg.err("Java should be updated to version 1.6");
        	new AlertPopup("Java should be updated to version 1.6 in order for this application to work properly!", new Dimension(430, 150));
        }
        else Debg.print("Your Java version seems to be fine");
		
		try {
			EventQueue.invokeAndWait(new Runnable() {
				public void run() {initComponents();}
			});
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		Debg.print("Finished applet initialization");
		printInfo(messageHistory);
		messageHistory = "";
	}
	
	/**
	 * Extracts the version number of a string array, according to the java versioning conventions.
	 * 
	 * @param version The String array containing the version information.
	 * @param index The index in the string array to be extracted into an integer.
	 * @return The integer expressing the version or 0 if an unhandled exception happened.
	 */
	private static int getVersionInteger(String[] version, int index){
		int v = 0;
    	try {
	        v = Integer.parseInt(version[index]);
    	} catch(ArrayIndexOutOfBoundsException arre){
    		v = 0;
    	} catch(NumberFormatException nfe){
    		v = getVersionInteger(version[index].split("[-_]"), 0);
    	} catch(Exception e){
    		v = 0;
    	}
    	return v;
	}
	
	/**
	 * Retreiving the necessary parameters url, eventport and streamport.
	 * Initializing the GUI and starting connecting on the ports to the 
	 * remote experiment broadcaster.
	 */
	private void initComponents() {
		popupWindow = new ParameterPopup();
		hostURL = getParameter("url");
		String tmpEventPort = getParameter("eventport");
		String tmpStreamPort = getParameter("streamport");
		if(hostURL == null || tmpEventPort == null || tmpStreamPort == null){
			Debg.err("One or more of the requested parameters haven't been found... Stopping!\nparameters url, eventport and streamport are required for this program to run properly!");
			stop();
			return;
		}
		try {
			eventPort = Integer.parseInt(tmpEventPort);
			streamPort = Integer.parseInt(tmpStreamPort);
		} catch (NumberFormatException nfe) {
			Debg.err("one of the parameters eventport and streamport couldn't be converted into an integer");
			stop();
			return;
		}
		Debg.print("Applet parameters loaded successfully");

		gui = new ClientGUI(this);
		serverEvent = new ClientConnection(this);
		myImage = new NetObserver();
		myLiner = new LineSection();
		
		Debg.print("Socket connectors and observer initialized, initializing GUI");
		
		JPanel panelWindow = new JPanel();
		ClientGUI.setupComponent(panelWindow, new Dimension(guiWidth, guiHeight));
		panelWindow.setBackground(Color.white);
		this.setSize(new Dimension(guiWidth, guiHeight));
		this.setContentPane(panelWindow);
		this.setBackground(Color.white);
		
		JLabel labelNano = new JLabel();
		labelNano.setIcon(new ImageIcon(getClass().getResource(IMAGE_DIR + "nano_logo.jpg")));
		ClientGUI.setupComponent(labelNano, new Dimension(500, 96));
		ImagePanel myImagePanel = new ImagePanel(myImage);
		JPanel panelGraphs = gui.setupGraphs(myImage, myLiner);
		JPanel panelControls = gui.setupControls();
		JPanel panelStatus = gui.setupStatusLine();
		
		//this.setJMenuBar(createMenuBar());
		panelWindow.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
		panelWindow.add(labelNano);
		panelWindow.add(myImagePanel);
		panelWindow.add(panelGraphs);
		panelWindow.add(panelControls);
		panelWindow.add(panelStatus);
		
		socketObserver = new NanoSocketObserver();
		socketObserver.start("SocketObserver");
		satStream = new SocketConnector(this, hostURL, streamPort);
		satEvent = new SocketConnector(this, hostURL, eventPort);
		gui.setInitialized(true);
	}
/*
    public JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");
        JMenuItem menuItem;
        menuBar.add(menu);

        menuItem = new JMenuItem("Change Connection");
        menuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				popupWindow.initConnectionPopup();
				popupWindow.setVisible(true);
			}
		});
        menu.add(menuItem);
        menuItem = new JMenuItem("Login");
        menuItem.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				popupWindow.initLoginPopup();
				popupWindow.setVisible(true);
			}
		});
        menu.add(menuItem);
        return menuBar;
    }
	*/
    public void handleCBREvent(String evt){
    	Debg.print("Communication: " + evt);
		if(evt.contains(NanoComm.COMMAND_PARAM)){
			gui.setParameter(evt);
		} else if(evt.contains(NanoComm.COMMAND_STATE)){
			gui.setState(Integer.parseInt(evt.substring(NanoComm.COMMAND_STATE.length() + 1)));
		} else if(evt.contains(NanoComm.COMMAND_INFO)){
			gui.setInfo(Integer.parseInt(evt.substring(NanoComm.COMMAND_INFO.length() + 1)));
		} else if(evt.contains(NanoComm.COMMAND_PRIV)){
			gui.setPrivilege(Integer.parseInt(evt.substring(NanoComm.COMMAND_PRIV.length() + 1)));
		}
	}

	protected void setScanRangeComboBox(JComboBox box){comboBoxScanRange = box;}
	protected void setSampleComboBox(JComboBox box){comboBoxSample = box;}
	protected void setStatusStyle(Style s){style = s;}
	protected void setStatusLine(JTextPane tp){statusLine = tp;}
	protected void buttonApproachActionPerformed(ActionEvent evt) {serverEvent.send(NanoComm.strCmd(NanoComm.CMD_AUTOAPPROACH));}
	protected void buttonWithdrawActionPerformed(ActionEvent evt) {serverEvent.send(NanoComm.strCmd(NanoComm.CMD_WITHDRAW));}
	protected void buttonStartActionPerformed(ActionEvent evt) {serverEvent.send(NanoComm.strCmd(NanoComm.CMD_START));}
	protected void buttonStopActionPerformed(ActionEvent evt) {serverEvent.send(NanoComm.strCmd(NanoComm.CMD_STOP));}
	protected void buttonStopStageActionPerformed(ActionEvent evt) {serverEvent.send(NanoComm.strCmd(NanoComm.CMD_STOPAPPROACH));}
	protected void buttonCamAngleActionPerformed(ActionEvent evt) {serverEvent.send(NanoComm.strCmd(NanoComm.CMD_CAMANGLE));}
	
	protected void buttonSaveActionPerformed(ActionEvent evt) {
		JFileChooser fcd = new JFileChooser();
		fcd.showSaveDialog(this);
		if(fcd != null){
			if(fcd.getSelectedFile() != null){
				System.out.println("Saved as " + fcd.getSelectedFile());
				myImage.saveImage(fcd.getSelectedFile());
			}
		}
	}
	
	protected void buttonLoginActionPerformed(ActionEvent evt) {
		popupWindow.initLoginPopup();
		popupWindow.setVisible(true);
	}

	protected void comboBoxScanRangeActionPerformed(ActionEvent evt) {
		String cbEntry = (String) comboBoxScanRange.getSelectedItem();
		// only if a button has been pressed it was a user input. else it has been changed via the server
		if(evt.getModifiers() > 0) {
			serverEvent.send(NanoComm.strCmd(NanoComm.CMD_SCANRANGE + " value=" + cbEntry.substring(0, cbEntry.length() - 3)));
		}
	}
	
	protected void comboBoxSampleActionPerformed(ActionEvent evt) {
		String choice = (String)comboBoxSample.getSelectedItem();
		// only if a button has been pressed it was a user input. else it has been changed via the server
		if(evt.getModifiers() > 0) {
			if(choice.equals("Calibrate")) {
				serverEvent.send(NanoComm.strCmd(NanoComm.CMD_CALIBRATESTAGE));
			} else {
				if(!choice.equals(lastPosition)){
					serverEvent.send(NanoComm.strCmd(gui.getSampleCommand(choice)));
				}
			}
		}
		lastPosition = choice;
	}
	
	protected void labelArrowUpActionPerformed(MouseEvent e) {sendArrowCommand("up");}
	protected void labelArrowRightActionPerformed(MouseEvent evt) {sendArrowCommand("right");}
	protected void labelArrowLeftActionPerformed(MouseEvent evt) {sendArrowCommand("left");}
	protected void labelArrowDownActionPerformed(MouseEvent evt) {sendArrowCommand("down");}
	
	private void sendArrowCommand(String direction){
		if(serverEvent != null) serverEvent.send(NanoComm.strCmd(NanoComm.CMD_MOVETIP + " value=" + direction));
	}
	
	protected int getCurrentLine(){
		if(myImage == null) return 0;
		else return myImage.getCurrentLine();
	}
	
	/**
	 * This enables javascript from the webpage to set the user id even after the applet has loaded.
	 * 
	 * @param id The user ID of the student as in the portal.
	 */
	public void setUserID(String id){
		if(userID == null){
			userID = id;
			serverEvent.send(NanoComm.strCmd("lilalogin userid=" + userID));
		} else Debg.err("tried again to set user ID");
	}
	
	protected void sendAdminCommand(String message){
		serverEvent.send(message);
	}
	
	/**
	 * Add information to the status box.
	 * 
	 * @param msg The message to add.
	 */
	public void printInfo(String msg){
		if(statusLine == null) messageHistory = msg + "\n" + messageHistory;
		else {
			try {
				statusLine.getDocument().insertString(0, msg + "\n", style);
				statusLine.setCaretPosition(0);
			} catch (BadLocationException e) {}
		}
	}

	/**
	 * If one of the sockets succeeded to connect to the server it is handled here
	 */
	@Override
	public void newSocket(Socket socket, int port) {
		if(port == eventPort){
			socketObserver.addSocket(serverEvent.addEventClientSocket(socket));
			Debg.print("Event socket connected");
		} else if(port == streamPort){
			serverStream = new StreamSocket(socket, this);
			serverStream.plugDisplay(myImage);
			serverStream.plugDisplay(myLiner);
			socketObserver.addSocket(serverStream);
			Debg.print("Stream socket connected");
		} else Debg.err("Odd SocketAcceptThread tries to add socket... " + port);
		if(serverEvent.isConnected() && serverStream != null) {
			gui.setServerConnected(true);
			printInfo("Successfully connected to server!");
		}
	}
	
	@Override
	public void removeStreamSocket(StreamSocket sock) {
		Debg.print("Removing Stream Socket");
		gui.setServerConnected(false);
		socketObserver.removeSocket(sock);
		serverStream = null;
		satStream.releaseSocket();
	}
	
	public void removeEventSocket(EventSocket sock){
		Debg.print("Removing Event Socket");
		gui.setServerConnected(false);
		socketObserver.removeSocket(sock);
		satEvent.releaseSocket();
	}
	/*
	private void reconnectToHost(){
		gui.setServerConnected(false);
		serverEvent.releaseSocket();
		if(serverStream != null)serverStream.shutDown();
		satEvent.setHost(hostURL, eventPort);
		satStream.setHost(hostURL, streamPort);
		satEvent.restartConnecting();
		satStream.restartConnecting();
	}
	*/
	private class AlertPopup extends JFrame{
		private static final long serialVersionUID = 1L;
		
		private AlertPopup(String info, Dimension size){
			JLabel infoLabel = new JLabel(info);
			JLabel spacer = new JLabel();
			JButton ok = new JButton("Ok");
			ok.addActionListener(new ActionListener(){
				@Override public void actionPerformed(ActionEvent arg0) {hidePopup();}
			});
			this.setLayout(new FlowLayout());
			ClientGUI.setupComponent(infoLabel, new Dimension((int)size.getWidth() - 20, (int)size.getHeight() - 100));
			ClientGUI.setupComponent(spacer, new Dimension((int)size.getWidth() - 20, 10));
			ClientGUI.setupComponent(ok, new Dimension(50, 25));
			ClientGUI.setupComponent(this, size);
			add(infoLabel);
			add(spacer);
			add(ok);
			setVisible(true);
		}
		private void hidePopup(){
			setVisible(false);
		}
	}

	private class ParameterPopup extends JFrame{
		private static final long serialVersionUID = 1L;
		private JTextField fieldOne;
		private JTextField fieldTwo;
		private JTextField fieldThree;
	    private JPanel contentPanel;
		
		private ParameterPopup(){
			setBackground(Color.white);
		    contentPanel = new JPanel();
			ClientGUI.setupComponent(contentPanel, new Dimension(305, 25));
            add(contentPanel);
		}

		protected void initLoginPopup(){
			contentPanel.removeAll();
			JLabel labelText = new JLabel("Please enter your credentials: ");
			JLabel labelUser = new JLabel("Username: ");
			fieldOne = new JTextField();
			JLabel labelPass = new JLabel("Password: ");
			fieldTwo = new JPasswordField();
			JButton ok = new JButton("Ok");
			addEnterKeyToComponent(ok, ok);
			addEnterKeyToComponent(fieldOne, ok);
			addEnterKeyToComponent(fieldTwo, ok);
			ok.addActionListener(new ActionListener(){
				@Override public void actionPerformed(ActionEvent arg0) {login();}
			});
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(new ActionListener(){
				@Override public void actionPerformed(ActionEvent arg0) {hidePopup();}
			});
			contentPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
			ClientGUI.setupComponent(labelText, new Dimension(305, 25));
			ClientGUI.setupComponent(labelUser, new Dimension(100, 25));
			ClientGUI.setupComponent(fieldOne, new Dimension(200, 25));
			ClientGUI.setupComponent(labelPass, new Dimension(100, 25));
			ClientGUI.setupComponent(fieldTwo, new Dimension(200, 25));
			ClientGUI.setupComponent(ok, new Dimension(80, 25));
			ClientGUI.setupComponent(cancel, new Dimension(80, 25));
			ClientGUI.setupComponent(this, new Dimension(350, 175));
			contentPanel.setBackground(Color.white);
			contentPanel.add(labelText);
			contentPanel.add(labelUser);
			contentPanel.add(fieldOne);
			contentPanel.add(labelPass);
			contentPanel.add(fieldTwo);
			contentPanel.add(ok);
			contentPanel.add(cancel);
		}
/*
		protected void initConnectionPopup(){
			contentPanel.removeAll();
			JLabel labelText = new JLabel("Please enter the host information: ");
			JLabel labelUser = new JLabel("Host: ");
			fieldOne = new JTextField();
			JLabel labelEPort = new JLabel("Event-Port: ");
			fieldTwo = new JTextField();
			JLabel labelSPort = new JLabel("Data-Port: ");
			fieldThree = new JTextField();
			JButton ok = new JButton("Ok");
			addEnterKeyToComponent(ok, ok);
			addEnterKeyToComponent(fieldOne, ok);
			addEnterKeyToComponent(fieldTwo, ok);
			addEnterKeyToComponent(fieldThree, ok);
			
			ok.addActionListener(new ActionListener(){
				@Override public void actionPerformed(ActionEvent arg0) {connect();}});
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(new ActionListener(){
				@Override public void actionPerformed(ActionEvent arg0) {hidePopup();}
			});
			contentPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
			ClientGUI.setupComponent(labelText, new Dimension(305, 25));
			ClientGUI.setupComponent(labelUser, new Dimension(100, 25));
			ClientGUI.setupComponent(fieldOne, new Dimension(200, 25));
			ClientGUI.setupComponent(labelEPort, new Dimension(100, 25));
			ClientGUI.setupComponent(fieldTwo, new Dimension(200, 25));
			ClientGUI.setupComponent(labelSPort, new Dimension(100, 25));
			ClientGUI.setupComponent(fieldThree, new Dimension(200, 25));
			ClientGUI.setupComponent(ok, new Dimension(80, 25));
			ClientGUI.setupComponent(cancel, new Dimension(80, 25));
			ClientGUI.setupComponent(this, new Dimension(350, 200));
			contentPanel.setBackground(Color.white);
			fieldOne.setText(hostURL);
			fieldTwo.setText(""+eventPort);
			fieldThree.setText(""+streamPort);
			contentPanel.add(labelText);
			contentPanel.add(labelUser);
			contentPanel.add(fieldOne);
			contentPanel.add(labelEPort);
			contentPanel.add(fieldTwo);
			contentPanel.add(labelSPort);
			contentPanel.add(fieldThree);
			contentPanel.add(ok);
			contentPanel.add(cancel);
		}
*/
		private void  addEnterKeyToComponent(JComponent comp, JButton pressButton){
			String actionKey = pressButton.toString();
		    InputMap inputMap = comp.getInputMap();
			ActionMap actionMap = comp.getActionMap();

		    Action actionListener = new ButtonPresser(pressButton);
			actionMap.put(actionKey, actionListener);
		    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true), actionKey);
		    comp.setActionMap(actionMap);
		}
		
		class ButtonPresser extends AbstractAction{
	    	private JButton pressMe;
	    	public ButtonPresser(JButton b){pressMe = b;}
	    	public void actionPerformed(ActionEvent e) {pressMe.doClick();}
		}
		
		private void login(){
			serverEvent.send(NanoComm.strCmd("login username=" + fieldOne.getText() 
					+ " password=" + Password.computeHashHex(fieldTwo.getText())));
			hidePopup();
		}
/*
		private void connect(){
			String tmpURL;
			int tmpEvt, tmpStream;
			Debg.print("Connecting to " + fieldOne.getText() + " on ports " + fieldTwo.getText() + " and " + fieldThree.getText());
			tmpURL = hostURL;
			tmpEvt = eventPort;
			tmpStream = streamPort;
			hostURL = fieldOne.getText();
			try{
				eventPort = Integer.parseInt(fieldTwo.getText());
				streamPort = Integer.parseInt(fieldThree.getText());
				reconnectToHost();
			} catch (NumberFormatException e){
				printInfo("Invalid host connection parameters!");
				hostURL = tmpURL;
				eventPort = tmpEvt;
				streamPort = tmpStream;
			}
			hidePopup();
		}
		*/
		private void hidePopup(){
			setVisible(false);
		}
	}


}
