package nano.remexp.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Socket;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import nano.debugger.Debg;
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

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.UIManager.LookAndFeelInfo;

/**
 * This is the remote client for the virtual experiments, introduced in late 2011
 * 
 * @author Dominic Bosch
 * @version 1.1 23.08.2012
 */

/*
 * TODO List from group meeting:
 * integration tests
 * implementation into booking system of portal
 * save image dialog corrupt
 */

public class ClientApplet extends JApplet implements SocketReceiver, StreamSocketInterface {

	private static final long serialVersionUID = 1L;
	private ClientGUI gui;
	private SocketConnector satStream;
	private SocketConnector satEvent;
	private ClientConnection serverEvent;
	private StreamSocket serverStream;
	private NetObserver myImage;
	private LineSection myLiner;
	private String hostURL, userID;
	private int eventPort, streamPort;
	private NanoSocketObserver socketObserver;
	private Vector<String> startUpEvents;

	/**
	 * Initialization of the applet, trying to set Nimbus look and feel.
	 */
	public void init() {
		startUpEvents = new Vector<String>();
		Debg.setDebugMode(null, Debg.DEBUGMODE_STDOUT);
		userID = null;
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
	}
	
	/**
	 * Extracts the version number of a string array, according to the java version conventions.
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
	 * Retrieving the necessary parameters url, eventport and streamport.
	 * Initializing the GUI and starting connecting on the ports to the 
	 * remote experiment broadcaster.
	 */
	private void initComponents() {
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
		
		this.setBackground(Color.white);
		this.setContentPane(gui.createWindow(myImage, myLiner));
		
		socketObserver = new NanoSocketObserver();
		socketObserver.start("SocketObserver");
		satStream = new SocketConnector(this, hostURL, streamPort);
		satEvent = new SocketConnector(this, hostURL, eventPort);
		gui.setInitialized();
		for(String evt: startUpEvents) handleCBREvent(evt);
		startUpEvents.clear();
	}

	/**
	 * Handles events coming from the remote experiment broadcaster and executes the
	 * appropriate command on the GUI.
	 * 
	 * @param evt	the event message that was sent by the remote experiment broadcaster 
	 */
    public void handleCBREvent(String evt){
    	if(!gui.isInitialized()){
    		startUpEvents.add(evt);
    	} else {
	    	Debg.print("Communication: " + evt);
			if(evt.contains(NanoComm.COMMAND_PARAM)){
				gui.setParameter(evt);
			} else if(evt.contains(NanoComm.COMMAND_STATE)){
				gui.setState(evt);
			} else if(evt.contains(NanoComm.COMMAND_INFO)){
				gui.setInfo(evt);
			} else if(evt.contains(NanoComm.COMMAND_PRIV)){
				gui.setPrivilege(Integer.parseInt(evt.substring(NanoComm.COMMAND_PRIV.length() + 1)));
			}
    	}
	}
	
    /**
     * Retrieves the line that is currently read by the remote experiment.
     * 
     * @return	the index of the line that is read or has last been read
     */
	protected int getCurrentLine(){
		if(myImage == null) return 0;
		else return myImage.getCurrentLine();
	}
	
	/**
	 * This function packs strings of information into the protocol,
	 * so it is recognized as informatio to the client.
	 * 
	 * @param message	the message that is meant for the client
	 */
	public void printInfo(String message){
		gui.setInfo(NanoComm.strInfo(NanoComm.INFO_MSG_TO_CLIENT) + " " + message);
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
	
	/**
	 * Opens the file chooser dialog that allows the user to store the scanned image.
	 */
	protected void saveImage(){
		JFileChooser fcd = new JFileChooser();
		fcd.showSaveDialog(this);
		if(fcd != null){
			if(fcd.getSelectedFile() != null){
				Debg.print("Saved as " + fcd.getSelectedFile());
				myImage.saveImage(fcd.getSelectedFile());
			}
		}
	}
	
	/**
	 * This function packs a string into the command protocol so it is recognized 
	 * as a command. It is then sent to the server through the event socket.
	 * 
	 * @param message	the command message to be sent to the server
	 */
	protected void sendCommand(String message){
		sendString(NanoComm.strCmd(message));
	}
	
	/**
	 * Sends a plain string to the remote experiment broadcaster
	 * 
	 * @param message	the message to be sent
	 */
	private void sendString(String message){
		serverEvent.send(message);
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
		checkConnectivity();
	}
	
	/**
	 * Checks whether stream and event socket to the remote experiment broadcatser
	 * are up and running, then informs the GUI about the current state.
	 */
	protected void checkConnectivity(){
		if(serverEvent.isConnected() && serverStream != null) {
			handleCBREvent(NanoComm.strInfo(NanoComm.INFO_SERVER_CONNECTED));
		} else handleCBREvent(NanoComm.strInfo(NanoComm.INFO_SERVER_DISCONNECTED));
	}
	
	/**
	 * Removes the stream socket to the remote experiment broadcaster
	 */
	@Override
	public void removeStreamSocket(StreamSocket sock) {
		Debg.print("Removing Stream Socket");
		gui.setInfo(NanoComm.strInfo(NanoComm.INFO_SERVER_DISCONNECTED));
		socketObserver.removeSocket(sock);
		serverStream = null;
		satStream.releaseSocket();
	}

	/**
	 * Removes the event socket to the remote experiment broadcaster
	 */
	public void removeEventSocket(EventSocket sock){
		Debg.print("Removing Event Socket");
		gui.setInfo(NanoComm.strInfo(NanoComm.INFO_SERVER_DISCONNECTED));
		socketObserver.removeSocket(sock);
		satEvent.releaseSocket();
	}
	
	/**
	 * A pop-up that can be displayed to inform the user about especially important things
	 * 
	 * @author Dominic Bosch
	 * @version 1.1 29.08.2012
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

}
