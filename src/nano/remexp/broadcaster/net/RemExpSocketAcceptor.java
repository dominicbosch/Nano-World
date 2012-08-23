package nano.remexp.broadcaster.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import nano.debugger.Debg;
import nano.remexp.ThreadHandler;
import nano.remexp.broadcaster.RemoteExperimentBroadcaster;

/**
 * This thread runs on the defined port until a socket has been connected and stops then.
 * This class is used to connect to the remote experiments server once, accepting inbound
 * connection from the remote experiment.
 * 
 * @author Dominic Bosch
 * @version 1.1 23.08.2012
 */
public class RemExpSocketAcceptor extends ThreadHandler{
	private Vector<String> myHosts;
	private int myPort;
	private RemoteExperimentBroadcaster restrictedServer;
	private ServerSocket acceptSocket;
	private boolean flaggedForTermination;

	/**
	 * The constructor needs the port on which the thread listens for a socket and the server
	 * instance that will be informed about the connected socket.
	 * 
	 * @param newPort The port on which to scan for new incoming socket connections.
	 * @param socketAcceptor The object that receives the sockets once they are connected.
	 */
    public RemExpSocketAcceptor(RemoteExperimentBroadcaster socketAcceptor){
	    restrictedServer = socketAcceptor;
	    acceptSocket = null;
	    myHosts = new Vector<String>();
	    flaggedForTermination = false;
	    //super.start(this.getClass().getName()); // Don't start the thread here, only when the addPort method has been called!
    }

    public void addPort(int port){
    	myPort = port;
	    super.start(this.getClass().getSimpleName());
	    Debg.print("Listening for new remote experiments event socket connection onto port " + port);
    }
    
    public void addHost(String host){
	    myHosts.add(host);	
    }
    
    /**
     * Runs until a socket connection has been found, incoming from the correct url.
     */
    public void doTask(){//FIXME connection stability between remote experiment and cbr. reconnect and also propagate such actions
    	//FIXME unavailable position combobox when remexp is connected after cbr starts running or the other way round
    	int numOfAttempts = 0;
        Socket dummySocket = null;
    	String host = null;
        while((acceptSocket == null || dummySocket == null) && !flaggedForTermination){
		    try{
		    	boolean isAllowed = false;
		        if(acceptSocket == null) acceptSocket = new ServerSocket(myPort);
		    	dummySocket = acceptSocket.accept();
		    	host = dummySocket.getInetAddress().getHostAddress();
		    	Debg.print(host	+ " attempted to connect as remote experiment on port " + myPort);
				for (String he: myHosts) if(he.equals(host)) isAllowed = true;
		    	if(isAllowed){
					Debg.print("Added new socket on port " + myPort + " for remote " 
							+ dummySocket.getInetAddress().getHostAddress() + ":" + dummySocket.getPort() 
							+ " and stopped accepting any other connections onto this port!");
					restrictedServer.setRemExpSocket(dummySocket, myPort);
		    	} else {
					Debg.err("Illegal access attempt on port " + myPort + " from remote " 
							+ dummySocket.getInetAddress().getHostAddress() + ":" + dummySocket.getPort() 
							+ "! This host isn't allowed to connect.");
		    		dummySocket.close();
		    		dummySocket = null;
		    		acceptSocket.close();
		    		acceptSocket = null;
		    	}
		    } catch(IOException e){
		    	if(acceptSocket == null){
		    		Debg.err("Either the server is shutting down or port " + myPort + " is probably already in use... trying again in five seconds");
		    	} else e.printStackTrace();
		    }
		    if(++numOfAttempts % 20 == 0) Debg.print("Still waiting for the remote experiment to connect on " 
		    		+ myPort + ", " + numOfAttempts + " connection attempts have been recorded");
	    	try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				Debg.err("Wasn't allowed to sleep...");
			}
    	}
    }

    /**
     * Used to force the shutdown the instance if stopThread didn't lead to a successful shut down
     * or an immediate shutdown is needed.
     */
	@Override
	public void shutDown() {
		flaggedForTermination = true;
		stopThread();
		if(acceptSocket != null) try {
			acceptSocket.close();
		} catch (IOException e) {Debg.err("Failed shutting down socket.");}
		acceptSocket = null;
		restrictedServer = null;
	}
}
