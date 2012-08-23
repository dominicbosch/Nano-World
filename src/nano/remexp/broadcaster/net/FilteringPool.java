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

package nano.remexp.broadcaster.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.Vector;

import nano.debugger.Debg;
import nano.remexp.CommandExecutor;
import nano.remexp.Parser;
import nano.remexp.broadcaster.RemoteExperimentBroadcaster;
import nano.remexp.net.NanoComm;
import nano.remexp.net.EventSocket;
import nano.remexp.net.EventSocketListener;
import nano.remexp.net.NanoSocket;
import nano.remexp.net.NanoSocketObserver;
import nano.remexp.net.SocketReceiver;
import nano.remexp.net.StreamSocket;
import nano.remexp.net.StreamSocketInterface;

/**
 * The pool of clients that need to be dealt with. It holds all event and stream connections
 * to clients, plus the output end of the stream pipe coming from the remote experiment.
 * 
 * @author Dominic Bosch
 * @version 1.0 21.10.2011
 */
public class FilteringPool implements EventSocketListener, SocketReceiver, StreamSocketInterface {
    private StreamPipe myPipe;
    private RemoteExperimentBroadcaster myServer;
	private RemoteExperimentMonitor remExpMonitor;
	private ClientSocketAcceptor eventPortListener;
	private ClientSocketAcceptor streamPortListener;
	private NanoSocketObserver socketObserver;
    private Vector<EventSocket> myClients;
    private Vector<Integer> listAllowedControls;
    private AccessTable accessList;
	private Parser adminCommandsParser;
    private EventSocket controller;
    private String rigID;
	private int eventPort, streamPort;
	
    /**
     * Instantiates an object of this class with the given arguments. It starts
     * two listening threads on the stream and event port where clients are allowed
     * to connect to.
     * 
     * @param server The server instance that is used to send commands to the remote experiment.
     * @param pipe The pipe that is filled with the data stream coming from the remote experiment.
     * @param locEvtPort The event port on which the pool listens for new clients.
     * @param locStrPort The stream port on which the pool listens for new clients.
     */
    public FilteringPool(RemoteExperimentBroadcaster server, StreamPipe pipe, int locEvtPort, int locStrPort){
		eventPort = locEvtPort;
		streamPort = locStrPort;
        myPipe = pipe;
        myServer = server;
        rigID = "";
        myClients = new Vector<EventSocket>();
        listAllowedControls = new Vector<Integer>();
		remExpMonitor = new RemoteExperimentMonitor(this);
		eventPortListener = new ClientSocketAcceptor(locEvtPort, this);
		streamPortListener = new ClientSocketAcceptor(locStrPort, this);
		socketObserver = new NanoSocketObserver();
		socketObserver.start("SocketObserver");
		accessList = new AccessTable();
		Hashtable<String, CommandExecutor> commands = new Hashtable<String, CommandExecutor>();
		commands.put("restartcbr", new RestartCommandExecutor());
		commands.put("shutdowncbr", new ShutDownCommandExecutor());
		commands.put("remexpreconnect", new RemExpReconnectCommandExecutor());
		commands.put("showstate", new StateRequestCommandExecutor());
		adminCommandsParser = new Parser(commands);
    }
    
    public void initRemExp(){
    	remExpMonitor.initRemExp();
    }
    
    public void addAccess(String user, String pw, int privilege){
		accessList.addEntry(user, pw, privilege);
	}

    public void addAllowedControl(int privilege){
		listAllowedControls.add(privilege);
	}

	public void addRig(String rig){
		rigID = rig;
	}

	public void addSample(int sid, String sampleName, String cmd, String rel, int tipX, int tipY, int dX, int dY){
		remExpMonitor.addSample(sid, sampleName, cmd, rel, tipX, tipY, dX, dY);
	}

	public void setRemExpName(String name){
		remExpMonitor.setRemExpName(name);
	}

	public void setRemExpConnected(boolean isConnected){
    	if(remExpMonitor != null){
	    	remExpMonitor.setRemExpConnected(isConnected);
	    	if(!isConnected) remExpMonitor.resetRemExp();
    	}
    }
    
    /**
     * Stores this moment now as a Date. Is used when the remote experiment sends data through the stream.
     */
    public void setStreamTimestamp(){
    	remExpMonitor.setStreamTimestamp();
    }
    
    /**
     * A new client has connected on the event port. The socket is added to the 
     * internal storage that manages all connected sockets in order to send 
     * events to the clients.
     * 
     * @param newSocket The socket that connected to the pool on the event port.
     */
    public synchronized void newSocket(Socket newSocket, int port){
    	//TODO implementation of a threadpool would be useful in the future if a lot of people connect to the experiment.
    	if(port == eventPort) {
    		EventSocket es = new EventSocket(newSocket, this);
    		myClients.add(es);
    		remExpMonitor.sendCurrentStateTo(es);
    	    socketObserver.addSocket(es);
    	    es.put("Welcome to the RAFM of the University of Basel!");
    	}
    	else if(port == streamPort) {
    		StreamSocket ss = new StreamSocket(newSocket, this);
    		myPipe.plugStreamSocket(ss);
    		socketObserver.addSocket(ss);
    	}
    }

	/**
	 * The pool receives a message from the client and passes it to the remote
	 * experiment monitor if the remote experiment isn't busy at the moment.
	 * The command will not be stored and the client needs to
	 * deliver it again as soon as the remote experiment is ready again.
	 * 
	 * @param sock The client socket from where the event has been received.
	 * @param message The event that has been sent.
	 */
	@Override
	public synchronized void performSocketEvent(EventSocket sock, String message) {
		Debg.print("Socket event: " + message + ", for " + sock.getRemoteID());
		switch(sock.getPrivilege()){
			case NanoComm.PRIV_ADMIN:
				if(adminCommandsParser.executedCommand(message));
				else if(myServer.isRemExpConnected()) sendToRemExp(message);
				else sock.put("Can't send command, experiment not connected!");
				//sock.put("command '" + message + "' sent to remote experiment");
			break;
			case NanoComm.PRIV_CONTROLLER:
				//if(sock != controller && controller != null) Debg.err("Why is " + sock.getRemoteID() + " also controller, original controller: " + controller.getRemoteID());
				//via login one can also be controller and doesn't steal controllership from probable LiLa user
				boolean expired = !staysController(); 
				if(expired && controller != null){
					controller.put("Your controller rights have expired!");
					controller = null;
				}
				else if(remExpMonitor.isValidRemExpCommand(message) && (!expired || sock != controller)){
					if(myServer.isRemExpConnected()){
						remExpMonitor.handleClientRequest(message, sock);
					} else sock.put("No remote experiment server connected! Can't execute command!");
				}
			break;
			default:
				sock.put("You got only observer rights!");
				Debg.print(message);
		}
	}

    /**
     * Passes the message to the RemoteExperimentMonitor to handle it.
     * 
     * @param msg The plain message coming from the remote experiment.
     */
    public synchronized void handleRemExpEvent(String msg){
    	Debg.print("remexp event: " + msg);
    	remExpMonitor.handleRemExpEvent(msg);
    }
    
    /**
     * This function handles the event channel from the
     * remote experiments server towards the clients.
     * 
     * @param message The event that has been sent by the remote experiment and needs to
     * be broadcast to all clients.
     */
    protected synchronized void broadcast(String message){
    	int i = 0;
    	String debgmsg = "Pool received message(" + message + ") from remote experiment," + 
    			" trying to send it to sockets: ";
    	if(myClients != null) for (EventSocket es : myClients){
    		if(es.isAliveThread()){
        		es.put(message);
        		debgmsg += "[" + i++ + "]" + es.getRemoteID() + ", ";	
    		}
    	}
		Debg.print("    " + debgmsg);
    }
    
    /**
     * This method is used to send commands straight to the remote experiments server.
     * 
     * @param message The command to be sent to the remote experiment.
     */
    protected synchronized void sendToRemExp(String message){
    	myServer.sendToRemExp(message);
    }

    /**
     * 
     */
	@Override
	public void login(EventSocket sock, String user, String pass) {
		int priv = NanoComm.PRIV_OBSERVER;
		Debg.print(sock.getRemoteID() + " tries to login: user: " + user + ", pass: " + pass);
		if(pass.equals("")){
			boolean userHasAccess = checkUserAccess(user, rigID);
			if(userHasAccess && !staysController()){
				controller = sock;
				controller.put("You have been successfully verified as controller!");
				priv = NanoComm.PRIV_CONTROLLER;
			}
		} else {
			priv = accessList.getPrivilege(user, pass);
			Debg.print("found privilege " + priv + " for " + user + "/" + pass);
			switch(priv){
				case NanoComm.PRIV_ADMIN:
					sock.put("You have gained admin rights!");
					break;
				case NanoComm.PRIV_CONTROLLER:
					sock.put("You have gained controller rights!");
					break;
				default:
					sock.put("You may only observe this experiment");
					priv = NanoComm.PRIV_OBSERVER;
					break;
			}
		}
		sock.setPrivilege(priv);
		remExpMonitor.sendCurrentStateTo(sock);
	}
	
	private boolean staysController(){
		boolean stays = false;
		if(controller != null){
			stays = checkUserAccess(controller.getUserID(), rigID);
			if(!stays) controller.setPrivilege(NanoComm.PRIV_OBSERVER);
		}
		return stays;
	}
	
	private boolean checkUserAccess(String user, String rig) {
		String result = null;
		try {
			URL url = new URL("https://www.library-of-labs.org/LilaBookingSystem/resources/lilaLabBooking/rigaccess/" + user + "/" + rig);
			URLConnection conn = url.openConnection ();
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuffer sb = new StringBuffer();
			String line;
			while ((line = rd.readLine()) != null) {
				sb.append(line);
			}
			rd.close();
			result = sb.toString();
		} catch (Exception e){
			Debg.err("Unable to contact LiLa booking server...");
		}
		if(result != null) {
			Debg.print("Result from Booking system check: " + result);
			if(result.equals("true")) return true;
		}
		return false;
	}

	/**
	 * If the event socket stops to read the in buffer and thus exits the loop it calls
	 * this method in order to remove the socket from the vector holding the client event sockets.
	 * 
	 * @param sock the socket that has been closed
	 */
	@Override
	public synchronized void removeEventSocket(EventSocket sock) {
		if(myClients != null) myClients.remove(sock);
		if(socketObserver != null) socketObserver.removeSocket(sock);
	}

	/**
	 * If the event socket stops to read the in buffer and thus exits the loop it calls
	 * this method in order to remove the socket from the vector holding the client event sockets.
	 * 
	 * @param sock the socket that has been closed
	 */
	public void removeStreamSocket(StreamSocket sock) {
		if(myPipe != null) myPipe.removeStreamSocket(sock);
		if(socketObserver != null) socketObserver.removeSocket(sock);
	}

    public void addSocketToObserve(NanoSocket ns){
		socketObserver.addSocket(ns);
    }
    
	public void removeObservedSocket(NanoSocket ns){
		socketObserver.removeSocket(ns);
	}
	
	/**
	 * Sets the thread to the null pointer which should stop the thread from running.
	 * It first forces all client event sockets to be closed and then also the pipe
	 * that holds the stream sockets.
	 */
	//@Override
	public void shutDown() {
		if(socketObserver!=null) socketObserver.shutDown();
		if(listAllowedControls!=null) listAllowedControls.clear();
		if(myPipe!=null) myPipe.shutDown();
		if(eventPortListener!=null) eventPortListener.shutDown();
		if(streamPortListener!=null) streamPortListener.shutDown();
		if(remExpMonitor!=null) remExpMonitor.shutDown();
	    accessList.clearAll();
	    myServer = null;
    	myPipe = null;
    	eventPortListener = null;
    	streamPortListener = null;
    	listAllowedControls = null;
    	remExpMonitor = null;
    	socketObserver = null;
		adminCommandsParser = null;
	    rigID = "";
		eventPort = -1;
		streamPort = -1;
		if(myClients!=null){
		    myClients.clear();// Do not shutdown sockets here since they are being halted by the socketObserver
		    myClients = null;
		}
	}
	
	private class AccessTable {
		private Vector<AccessEntry> entries;
		
		private AccessTable(){
			entries = new Vector<AccessEntry>();
		}
		
		private void addEntry(String uname, String pw, int priv){
			entries.add(new AccessEntry(uname, pw, priv));
		}
		
		private int getPrivilege(String uname, String pw){
	    	for (AccessEntry ae: entries){
	    		int priv = ae.getPrivilege(uname, pw);
	    		if(priv > -1) return priv;
	    	}
			return -1;
		}
		
		private void clearAll(){
	    	entries.clear();
		}
		
		private class AccessEntry {
			private String username;
			private String password;
			private int privilege;

			private AccessEntry(String uname, String pw, int priv){
				username = uname;
				password = pw;
				privilege = priv;
			}
			
			private int getPrivilege(String uname, String pass){
				if(username.equals(uname) && password.equals(pass)) return privilege;
				else return -1;
			}
		}
	}
	
	private class ShutDownCommandExecutor extends CommandExecutor {
		public void execute(Hashtable<String, String> tags) {
			myServer.shutDownCBR();
		}
	}
	
	private class RestartCommandExecutor extends CommandExecutor {
		public void execute(Hashtable<String, String> tags) {
			myServer.restartCBR();
		}
	}
	
	private class RemExpReconnectCommandExecutor extends CommandExecutor {
		public void execute(Hashtable<String, String> tags) {
			myServer.reconnectToRemExp();
		}
	}
	
	private class StateRequestCommandExecutor extends CommandExecutor {
		public void execute(Hashtable<String, String> tags) {
			remExpMonitor.printCurrentState();
			if(controller != null) Debg.print(controller.getRemoteID() + " is in charge as controller");
			else Debg.print("Nobody is controller");
			Debg.print(myClients.size() + " client(s) connected");
			for(EventSocket sock: myClients) Debg.print(sock.getRemoteID() + " has rights: " + sock.getPrivilege());
		}
	}
}
