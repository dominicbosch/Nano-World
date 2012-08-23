package nano.remexp.broadcaster;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import nano.debugger.Debg;
import nano.remexp.CommandExecutor;
import nano.remexp.Parser;
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
 * It is a pass through object between RemoteExperimentBroadcaster and RemoteExperimentPool.
 * It handles the access rights of connected clients and processes commands according to their rights.
 * 
 * @author Dominic Bosch
 * @version 1.1 23.08.2012
 */
public class FilteringPool implements EventSocketListener, SocketReceiver, StreamSocketInterface {
    private StreamPipe myPipe;
    private RemoteExperimentBroadcaster myServer;
	private RemoteExperimentMonitor remExpMonitor;
	private ClientSocketAcceptor eventPortListener;
	private ClientSocketAcceptor streamPortListener;
	private NanoSocketObserver socketObserver;
    private Vector<EventSocket> myClients;
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
	protected FilteringPool(RemoteExperimentBroadcaster server, StreamPipe pipe, int locEvtPort, int locStrPort){
		eventPort = locEvtPort;
		streamPort = locStrPort;
        myPipe = pipe;
        myServer = server;
        rigID = "";
        myClients = new Vector<EventSocket>();
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
    
    /**
     * Orders the remote experiment monitor to initialize the remote experiment.
     */
    protected void initRemExp(){
    	remExpMonitor.initRemExp();
    }
    
    /**
     * Stores access privileges for future login attempts of the clients.
     * 
     * @param user			the user name
     * @param pw			the password
     * @param privilege		the privilege that can be gained through these credentials
     */
    protected void addAccess(String user, String pw, int privilege){
		accessList.addEntry(user, pw, privilege);
	}

    /**
     * Sets the rig id that is checked for student access on the booking system.
     * 
     * @param rig the Rig ID to be checked on the booking server
     */
    protected void addRig(String rig){
		rigID = rig;
	}

	/**
	 * Passes the sample information of a new sample to the remote experiment monitor.
	 * 
	 * @param sid			the sample id
	 * @param sampleName	the sample name
	 * @param cmd			the command to the remote experiment moving to the sample
	 * @param rel			the command sent by the remote experiment, indicating the movement to the sample succeeded
	 * @param tipX			the initial x position of the tip at this sample
	 * @param tipY			the initial y position of the tip at this sample 
	 * @param dX			the maximum distance allowed away from the initial tip position in x direction
	 * @param dY			the maximum distance allowed away from the initial tip position in y direction
	 */
	protected void addSample(int sid, String sampleName, String cmd, String rel, int tipX, int tipY, int dX, int dY){
		remExpMonitor.addSample(sid, sampleName, cmd, rel, tipX, tipY, dX, dY);
	}

	/**
	 * Informs the remote experiment monitor about the name of the remote experiment.
	 * 
	 * @param name	the name of the remote experiment
	 */
	protected void setRemExpName(String name){
		remExpMonitor.setRemExpName(name);
	}

	/**
	 * Informs the remote experiment monitor about the connection state to the remote experiment.
	 * 
	 * @param isConnected	whether the remote experiment is connected or not
	 */
	protected void setRemExpConnected(boolean isConnected){
    	if(remExpMonitor != null){
	    	remExpMonitor.setRemExpConnected(isConnected);
    	}
    }
    
    /**
     * Stores this moment now as a Date. Is used when the remote experiment sends data through the stream.
     */
	protected void setStreamTimestamp(){
    	remExpMonitor.setStreamTimeStamp(new Date());
    }
    
    /**
     * A new client has connected on the event port. The socket is added to the 
     * internal storage that manages all connected sockets in order to send 
     * events or stream data to the clients.
     * 
     * @param newSocket The socket that connected to the pool on either the event or stream port.
     */
	@Override
    public synchronized void newSocket(Socket newSocket, int port){
    	if(port == eventPort) {
    		EventSocket es = new EventSocket(newSocket, this);
    		myClients.add(es);
    		remExpMonitor.sendCurrentStateTo(es);
    	    socketObserver.addSocket(es);
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
	 * Administrator commands are sent straight through to the remote experiment.
	 * Administrator commands do not set any locks. This is implemented on purpose 
	 * since the administrator should know what he's doing.
	 * In case a controller sends a message, this function checks whether the rights
	 * of a client that gained his controller rights through the booking server have now expired.
	 * 
	 * @param sock The client socket from where the event has been received.
	 * @param message The event that has been sent.
	 */
	@Override
	public synchronized void performSocketEvent(EventSocket sock, String message) {
		Debg.print("Socket event: " + message + ", for " + sock.getRemoteID());
		if(myServer!=null){
			switch(sock.getPrivilege()){
				case NanoComm.PRIV_ADMIN:
					boolean wasServerCommand = false;
					if(adminCommandsParser!=null) {
						wasServerCommand = adminCommandsParser.executedCommand(message);
					}
					if(wasServerCommand)Debg.print("Executed admin command: " + message);
					else if(myServer.isRemExpConnected()) sendToRemExp(message);
					else sock.sendClientInfo("Can't send command, experiment not connected!");
				break;
				case NanoComm.PRIV_CONTROLLER:
					if(!staysController() && controller != null){
						controller.setPrivilege(NanoComm.PRIV_OBSERVER);
						controller.sendClientInfo("Your controller rights have expired!");
						remExpMonitor.sendCurrentStateTo(controller);
						controller = null;
					}
				default:
					if(myServer.isRemExpConnected()) remExpMonitor.addClientRequest(message, sock);
					else sock.sendClientInfo("No remote experiment server connected! Can't execute command!");
			}
		} else Debg.err("Jim, we lost our server...");
	}

    /**
     * Passes the message to the RemoteExperimentMonitor to handle it.
     * 
     * @param msg The plain message coming from the remote experiment.
     */
	protected synchronized void handleRemExpEvent(String msg){
    	Debg.print("remexp event: " + msg);
    	if(remExpMonitor != null) remExpMonitor.handleRemExpEvent(msg);
    }
    
    /**
     * This function is used to send a message to all present clients.
     * 
     * @param message the message to be sent
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
     * if no password is delivered this function tries to check this client's access rights
     * through the booking server. Else the internal access rights are checked.
     */
	@Override
	public void login(EventSocket sock, String user, String pass) {
		int priv = NanoComm.PRIV_OBSERVER;
		Debg.print(sock.getRemoteID() + " tries to login: user: " + user + ", pass: " + pass);
		if(pass.equals("")){
			boolean userHasAccess = checkUserAccess(user, rigID);
			if(userHasAccess && !staysController()){
				controller = sock;
				controller.sendClientInfo("You have been successfully verified as controller!");
				priv = NanoComm.PRIV_CONTROLLER;
			}
		} else {
			priv = accessList.getPrivilege(user, pass);
			Debg.print("found privilege " + priv + " for " + user + "/" + pass);
			if(priv != -1) sock.sendClientInfo("You have gained " + user + " rights!");
			else sock.sendClientInfo("You may only observe this experiment");
		}
		if(priv != sock.getPrivilege()) sock.setPrivilege(priv);
		if(remExpMonitor != null) remExpMonitor.sendCurrentStateTo(sock);
	}
	
	/**
	 * This function performs a check for the controller that gained the rights through the booking server.
	 * 
	 * @return true if the controller still has access rights, else false.
	 */
	private boolean staysController(){
		boolean stays = false;
		if(controller != null){
			stays = checkUserAccess(controller.getUserID(), rigID);
			if(!stays) controller.setPrivilege(NanoComm.PRIV_OBSERVER);
		}
		return stays;
	}
	
	/**
	 * This function is used to check user access via the booking system.
	 * 
	 * @param user	the user (ticket)
	 * @param rig	the rig to be checked for access
	 * @return		true if user has access to this rig, else false
	 */
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
	 * this method in order to remove the socket from the vector that holds the client event sockets.
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
	@Override
	public void removeStreamSocket(StreamSocket sock) {
		if(myPipe != null) myPipe.removeStreamSocket(sock);
		if(socketObserver != null) socketObserver.removeSocket(sock);
	}
	
	/**
	 * Add a socket that needs to be observed for aliveness.
	 * 
	 * @param ns	the socket to be observed
	 */
	protected void addSocketToObserve(NanoSocket ns){
		if(socketObserver != null) socketObserver.addSocket(ns);
    }
    
	/**
	 * Remove a socket that decided to leave us alone.
	 * 
	 * @param ns	the socket that had enough
	 */
	protected void removeObservedSocket(NanoSocket ns){
		if(socketObserver != null) socketObserver.removeSocket(ns);
	}
	
	/**
	 * Sets the thread to null which should stop the thread from running.
	 * It first forces all client event sockets to be closed and then also the pipe
	 * that holds the stream sockets.
	 */
	protected void shutDown() {
		if(socketObserver!=null) socketObserver.shutDown();
		if(myPipe!=null) myPipe.shutDown();
		if(eventPortListener!=null) eventPortListener.shutDown();
		if(streamPortListener!=null) streamPortListener.shutDown();
		if(remExpMonitor!=null) remExpMonitor.shutDown();
	    accessList.clearAll();
	    accessList = null;
	    myServer = null;
    	myPipe = null;
    	eventPortListener = null;
    	streamPortListener = null;
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
	
	/**
	 * The access table that holds all credentials and respective privileges. 
	 *
	 * @author Dominic Bosch
	 * @version 1.1 29.08.2012
	 */
	private class AccessTable {
		private Vector<AccessEntry> entries;
		
		/**
		 * Make an AccessTable ready to be used for credential validations.
		 */
		private AccessTable(){
			entries = new Vector<AccessEntry>();
		}
		
		/**
		 * Appends an access entry to the list of valid credentials.
		 * 
		 * @param uname		username
		 * @param pw		password
		 * @param priv		the privilege that is gained through these credentials
		 */
		private void addEntry(String uname, String pw, int priv){
			entries.add(new AccessEntry(uname, pw, priv));
		}
		
		/**
		 * Searches for an access entry that matches username and password.
		 * 
		 * @param uname		requested username
		 * @param pw		requested password
		 * @return			the privilege if credentials matched, else -1
		 */
		private int getPrivilege(String uname, String pw){
	    	for (AccessEntry ae: entries){
	    		int priv = ae.getPrivilege(uname, pw);
	    		if(priv > -1) return priv;
	    	}
			return -1;
		}
		
		/**
		 * Clears all access credential entries
		 */
		private void clearAll(){
	    	entries.clear();
		}
		
		/**
		 * A credential entry that stores username, password and privilege.
		 * 
		 * @author Dominic Bosch
		 * @version 1.1 29.08.2012
		 */
		private class AccessEntry {
			private String username;
			private String password;
			private int privilege;

			/**
			 * Creates a credential entry
			 * 
			 * @param uname		username
			 * @param pw		password
			 * @param priv		privilege
			 */
			private AccessEntry(String uname, String pw, int priv){
				username = uname;
				password = pw;
				privilege = priv;
			}
			
			/**
			 * Returns the privilege if the credentials match.
			 * 
			 * @param uname		username
			 * @param pass		password
			 * @return			the privilege if the credentials matched, else -1
			 */
			private int getPrivilege(String uname, String pass){
				if(username.equals(uname) && password.equals(pass)) return privilege;
				else return -1;
			}
		}
	}
	
	/**
	 * Handles the shut down command that shuts the remote experiment broadcaster down.
	 * 
	 * @author Dominic Bosch
	 * @version 1.1 29.08.2012
	 */
	private class ShutDownCommandExecutor extends CommandExecutor {
		public void execute(Hashtable<String, String> tags) {
			myServer.shutDownCBR();
		}
	}
	
	/**
	 * Handles the restart command that restarts the remote experiment broadcaster.
	 * 
	 * @author Dominic Bosch
	 * @version 1.1 29.08.2012
	 */
	private class RestartCommandExecutor extends CommandExecutor {
		public void execute(Hashtable<String, String> tags) {
			myServer.restartCBR();
		}
	}
	
	/**
	 * Handles the reconnect command that drops the connection to the remote experiment and renews it. 
	 * 
	 * @author Dominic Bosch
	 * @version 1.1 29.08.2012
	 */
	private class RemExpReconnectCommandExecutor extends CommandExecutor {
		public void execute(Hashtable<String, String> tags) {
			myServer.reconnectToRemExp();
		}
	}
	
	/**
	 * Handles the state request command which produces output about the actual configuration
	 * 
	 * @author Dominic Bosch
	 * @version 1.1 29.08.2012
	 */
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
