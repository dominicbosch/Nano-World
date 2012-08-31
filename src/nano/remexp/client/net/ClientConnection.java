package nano.remexp.client.net;

import java.net.Socket;

import nano.debugger.Debg;
import nano.remexp.client.ClientApplet;
import nano.remexp.net.EventSocket;
import nano.remexp.net.EventSocketListener;

/**
 * This is the interface between the client and the socket.
 * 
 * @author Dominic Bosch
 * @version 1.1 23.08.2012
 */
public class ClientConnection implements EventSocketListener {
	private EventSocket myClientSocket = null;
	private ClientApplet applet;

	/**
	 * The constructor needs to know the applet to handle
	 * events coming through the socket.
	 * 
	 * @param app The applet that shows different behaviour on events
	 */
	public ClientConnection(ClientApplet app) {
		applet = app;
	}

	/**
	 * Informs about the connectivity to the client.
	 * 
	 * @return	true if connected, else false
	 */
	public boolean isConnected(){
		return myClientSocket != null;
	}
	
	/**
	 * Releases the client socket.
	 */
	public void releaseSocket(){
		if(myClientSocket != null) myClientSocket.shutDown();
		myClientSocket = null;
	}
	
	/**
	 * A new socket has connected and is registered.
	 * 
	 * @param sock The socket that connected successfully to the server
	 */
	public EventSocket addEventClientSocket(Socket sock){
		myClientSocket = new EventSocket(sock, this);
		return myClientSocket;
	}
	
	/**
	 * Sends a message over the socket.
	 *  
	 * @param message	The message to be sent
	 */
	public void send(String message) {
		if (myClientSocket != null) {
			myClientSocket.put(message);
		} else {
			applet.printInfo("You are not connected!");
			Debg.err("No connection available!");
		}
	}

	/**
	 * If an event is passed through the socket it will be displayed to the client or if it is a stage event,
	 * the appropriate action is being handled by the applet.
	 */
	@Override public void performSocketEvent(EventSocket sock, String msg) {
		applet.handleCBREvent(msg);
	}

	/**
	 * Shuts down the socket to the server and sets it null.
	 */
	@Override public void removeEventSocket(EventSocket sock) {
		applet.removeEventSocket(sock);
	}


	/**
	 * Shutting down the client socket
	 */
	public void shutDown() {
		if (myClientSocket != null) {
			myClientSocket.shutDown();
			myClientSocket = null;
		}
		applet = null;
	}
	/**
	 * not used for client
	 */
	@Override public void login(EventSocket sock, String username, String pass) {}
}