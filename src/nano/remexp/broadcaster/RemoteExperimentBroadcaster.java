package nano.remexp.broadcaster;

import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.Socket;

import nano.debugger.Debg;
import nano.remexp.StreamReceiver;
import nano.remexp.broadcaster.net.FilteringPool;
import nano.remexp.broadcaster.net.RemExpSocketAcceptor;
import nano.remexp.broadcaster.net.StreamPipe;
import nano.remexp.net.EventSocket;
import nano.remexp.net.EventSocketListener;
import nano.remexp.net.NanoComm;
import nano.remexp.net.StreamSocket;
import nano.remexp.net.StreamSocketInterface;

/**
 * This is the main class to be started as command and stream broadcaster between the
 * remote experiments server and the clients. It funnels the commands and allows only
 * a defined set to be passed. 
 * 
 * @author Dominic Bosch
 * @version 1.1 23.08.2012
 */
public class RemoteExperimentBroadcaster implements EventSocketListener, StreamSocketInterface, StreamReceiver {
	private EventSocket eventSocketToRemExp;
	private StreamSocket streamSocketToRemExp;
	private FilteringPool pool;
	private StreamPipe pipeEnd;
	private PipedOutputStream pipeStart;
	private int remExpEventPort = -1;
	private int remExpStreamPort = -1;
	private int clientEventPort = -1;
	private int clientStreamPort = -1;
	private RemExpSocketAcceptor sas;
	private RemExpSocketAcceptor sae;
	private boolean isFinishedInitializing;
	private int sampleID = 0;
	private String remExpName = "";
	private boolean isShuttingDown;
	private boolean wasNotYetConnected = true;
	
	/**
	 * The constructor of the server instantiates a new pipe through which the stream data from the 
	 * experiment are being passed towards the client.
	 * 
	 * @param confFolder The location of the configuration folder to be loaded at runtime.
	 */
	private RemoteExperimentBroadcaster(String confFolder) {
		sas = new RemExpSocketAcceptor(this);
		sae = new RemExpSocketAcceptor(this);
		initServer(confFolder);
	}

	/**
	 * The initialization of this restricted server instance sets the
	 * commands for which the server listens, processes the configuration
	 * file config.dat in the confFolder, which is necessary for initialization.
	 * 
	 * @param confFolder the location of the folder containing the config file.
	 */
	private void initServer(String confFolder) {
		isShuttingDown = false;
		isFinishedInitializing = false;
		pipeStart = new PipedOutputStream();
		PipedInputStream pipeInStr = null;
		try {
			pipeInStr = new PipedInputStream(pipeStart);
		} catch (IOException e) {
			Debg.err("Unable to connect the stream pipe! Only events will be handled!");
		}
		pipeEnd = new StreamPipe(pipeInStr);
		
		String sep = File.separator;
		XMLConfigParser xcp = new XMLConfigParser(confFolder + sep + "config.xml", this);
		if(!xcp.processFile()){
			Debg.err("Unable to properly process configuration file '" + confFolder + sep + "config.xml', shutting down...");
			System.exit(0);
		}
		Debg.print("### Server started ###");
	}
	
	protected void initLogger(int dbgmodeConsole, int dbgmodeLogFile){
		Debg.setDebugMode("cbr.log", dbgmodeConsole, dbgmodeLogFile);
	}
	
	protected void finishedInitialization(){
		isFinishedInitializing = true;
	}

	/**
	 * This function handles the initialization of the remote experiments server ports
	 * according to their definition in the config.dat file.
	 * 
	 * @param eventPort the event port on which this server listens for the connecting experiment server.
	 * @param streamPort the stream port on which this server listens for connecting experiment server.
	 */
	protected void initRemExpPorts(int eventPort, int streamPort) {
		if(remExpEventPort == -1){
			remExpEventPort = eventPort;
			remExpStreamPort = streamPort;
			sas.addPort(streamPort);
			sae.addPort(eventPort);
			Debg.print("Initiated remote experiments port scanners");
		} else {
			Debg.err("Somebody tried to change the ports to the remote experiment!");
		}
	}
	
	/**
	 * This function handles the initialization of the remote experiments server ports
	 * according to their definition in the config.dat file.
	 * 
	 * @param eventPort the event port on which this server listens for the connecting experiment server.
	 * @param streamPort the stream port on which this server listens for connecting experiment server.
	 */
	protected void addRemExpHost(String host) {
		if(!isFinishedInitializing){
			sas.addHost(host);
			sae.addHost(host);
			Debg.print("Added allowed host: " + host);
		} else Debg.err("Too late to add allowed host: " + host);
	}

	/**
	 * This function handles the initialization of the client ports according to their
	 * definition in the config file. It also instantiates the @see FilteringPool object
	 * that handles the communication inbounding from the clients.
	 * 
	 * @param eventPort the event port on which the server listens for connecting clients
	 * @param streamPort the stream port on which the server listens for connecting clients
	 */
	protected void initClientPorts(int eventPort, int streamPort) {
		if(clientStreamPort == -1){
			clientStreamPort = streamPort;
			clientEventPort = eventPort;
			pool = new FilteringPool(this, pipeEnd, clientEventPort, clientStreamPort);
			if(!remExpName.equals("")) pool.setRemExpName(remExpName);
		} else {
			Debg.err("Somebody tried to change the ports to the clients!");
		}
	}
	
	/**
	 * This method is used to set the event and stream sockets as soon as
	 * they are connected.
	 * 
	 * @param sock The socket that has been connected.
	 * @param port The port identifying either the stream or event socket.
	 */
	public void setRemExpSocket(Socket sock, int port){
		if(pool != null) {
			if(port == remExpEventPort) {
				if(eventSocketToRemExp != null) {
					eventSocketToRemExp.shutDown();
					Debg.err("RemExp event socket already connected, shutting down old socket!");
				}
				eventSocketToRemExp = new EventSocket(sock, this);
				pool.addSocketToObserve(eventSocketToRemExp);
				initializeRemoteExperiment();
			} else if(port == remExpStreamPort){
				if(streamSocketToRemExp != null){
					streamSocketToRemExp.shutDown();
					Debg.err("RemExp stream socket already connected, shutting down old socket!");
				}
				streamSocketToRemExp = new StreamSocket(sock, this);
				streamSocketToRemExp.plugDisplay(this);
				pool.addSocketToObserve(streamSocketToRemExp);
			}
			if(streamSocketToRemExp != null && streamSocketToRemExp != null) pool.setRemExpConnected(true);
			else pool.setRemExpConnected(false);
		} else {
			Debg.err("Fatal error in initialization!");
			System.exit(-1);
		}
	}

	private void initializeRemoteExperiment(){
		if(eventSocketToRemExp != null && wasNotYetConnected){
			eventSocketToRemExp.put(NanoComm.strCmd(NanoComm.CMD_STOP));
			try {Thread.sleep(3000);} catch (InterruptedException e1) {}
			eventSocketToRemExp.put(NanoComm.strCmd(NanoComm.CMD_WITHDRAW));
			try {Thread.sleep(3000);} catch (InterruptedException e) {}
			if(pool!=null) pool.initRemExp();
			else Debg.err("Fatal error in initialization!");
			wasNotYetConnected = false;
		}
	}
	
	/**
	 * Remote experiment sends event thus this has to be processed and delivered further.
	 * 
	 * @param message The message that has been received from the remote experiment.
	 */
	@Override
	public void performSocketEvent(EventSocket sock, String message) {
		if(pool != null) {
			try {
				pool.handleRemExpEvent(message);
			} catch (Exception ev) {
				Debg.err("Strange exception found: " + ev.getMessage());
			}
		}
		Debg.print(" RemExp sends: " + message);
	}
	
	/**
	 * This function answers the question whether the remote experiments
	 * event socket is connected or not.
	 *  
	 * @return True if the remote experiments event socket is connected, else false. 
	 */
	public boolean isRemExpConnected(){
		return eventSocketToRemExp != null;
	}
	
	/**
	 * This function sends events directly to the remote experiment.
	 * 
	 * @param message the message to be sent through the socket
	 */
	public synchronized void sendToRemExp(String message){
		if(eventSocketToRemExp != null) eventSocketToRemExp.put(message);
		else Debg.err("No remote experiment server connected! Can't send message " + message);
	}

	/**
	 * This method removes the event socket to the remote experiment.
	 */
	@Override
	public void removeEventSocket(EventSocket sock) {
		eventSocketToRemExp = null;
		if(!isShuttingDown && pool!=null) {
			pool.setRemExpConnected(false);
			pool.removeObservedSocket(sock);
		}
	}

	/**
	 * This method removes the stream socket to the remote experiment.
	 */
	@Override
	public void removeStreamSocket(StreamSocket sock) {
		streamSocketToRemExp = null;
		if(!isShuttingDown && pool!=null) {
			pool.setRemExpConnected(false);
			pool.removeObservedSocket(sock);
		}
	}
	
	/**
	 * This function is called when the remote experiment sends 
	 * data through the stream socket. The data is then shoved into the pipe. 
	 * 
	 * @param b The byte array to be sent through the pipe.
	 */
	@Override
	public void write(byte[] b) {
		try {
			if(pool != null) pool.setStreamTimestamp();
			pipeStart.write(b);
		} catch (IOException e) {
			Debg.err("Couldn't write data into pipe");
		}
	}

	/**
	 * Not used for remote experiment socket.
	 */
	@Override
	public void login(EventSocket sock, String user, String pass) {}

	protected void addUser(String uname, String pw, String priv){
		if(uname == null || priv == null){
			Debg.err("Missing parameter for command adduser");
		} else {
			if(pw == null) pw = "";
			try {
				int privVal = NanoComm.class.getDeclaredField(priv).getInt(null);
				if(pool != null) pool.addAccess(uname, pw, privVal);
				Debg.print("user " + uname + " loaded");
			} catch (Exception e) {
				Debg.err("Error in loading privilege: " + priv + ", field doesn't exist!");
			}
		}
	}
	
	protected void setAllowedControl(String priv){
		try {
			int privVal = NanoComm.class.getDeclaredField(priv).getInt(null);
			if(pool != null) pool.addAllowedControl(privVal);
			Debg.print("setting privilege " + privVal + " as allowed to show controls.");
		} catch (Exception e) {
			Debg.err("Error in setting privilege allowed to show controls: " + priv + ", field doesn't exist!");
		}
	}

	protected void addSample(String nm, String cmd, String rel, String x, String y, String dx, String dy){
		if(nm == null || cmd == null || rel == null || x == null || y == null || dx == null || dy == null){
			Debg.err("Missing parameter for command addsample");
		} else try{
			Debg.print("Added sample: ID=" + sampleID + ", name=" + nm + ", cmd=" + cmd + ", release=" + rel 
					+ ", x=" + x + ", y=" + y + ", dx=" + dx +", dy=" + dy);
			if(pool != null) pool.addSample(sampleID++, nm, cmd, rel, Integer.parseInt(x), 
					Integer.parseInt(y), Integer.parseInt(dx), Integer.parseInt(dy));
		} catch(Exception e){Debg.err("Error in casting to int in addSample");}
	}

	/**
	 * Handles the initialization of the rig.
	 */
	protected void addRig(String rigname){
		if(!isFinishedInitializing){
			if(pool != null) pool.addRig(rigname);
			Debg.print("Rig loaded: " + rigname);
		} Debg.err("Too late to add rig " + rigname);
	}

	protected void setRemExpName(String name) {
		if(pool == null) remExpName = name;
		else pool.setRemExpName(name);
	}

	/**
	 * This function removes the link to the remote experiment and thus initializes reconnection.
	 */
	public void reconnectToRemExp(){
		removeEventSocket(eventSocketToRemExp);
		removeStreamSocket(streamSocketToRemExp);
	}
	
	public void restartCBR(){
		shutDownCBR();
		try {
			Thread.sleep(25000);
		} catch (InterruptedException e) {Debg.err("I was so bothered, I couldn't sleep while restarting.");}
		Debg.print(" ### Restarting Server ###");
		new RemoteExperimentBroadcaster("conf");
	}

	public void shutDownCBR(){
		isShuttingDown = true;
		Debg.err(" !!! SHUTDOWN CBR !!!");
		if(sas!=null) sas.shutDown();
		if(sae!=null) sae.shutDown();
		if(pool!=null) pool.shutDown();
		if(pipeEnd!=null) pipeEnd.shutDown();
		try {if(pipeStart!=null) pipeStart.close();} catch (IOException e) {Debg.err("Pipe closing failed");}
		sas = null;
		sae = null;
		eventSocketToRemExp = null;
		streamSocketToRemExp = null;
		pool = null;
		pipeEnd = null;
		pipeStart = null;
		remExpEventPort = -1;
		remExpStreamPort = -1;
		clientEventPort = -1;
		clientStreamPort = -1;
		isFinishedInitializing = false;
		sampleID = 0;
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {}
		Debg.closeLog();
	}

	/**
	 * The main method to start the Remote experiments broadcaster that is placed between
	 * a remote experiments server and the clients as a filtering and locking instance.
	 * 
	 * @param args no arguments are required to start the server.
	 * Only the file {@code config.dat} in the folder {@code conf} must be present.
	 * This file needs to contain the commands {@code initremexphost} and {@code initclientports}
	 * and should hold a definition similar to the following:
	 * 
	 */
	public static void main(String[] args) {
		new RemoteExperimentBroadcaster("conf");
	}
}