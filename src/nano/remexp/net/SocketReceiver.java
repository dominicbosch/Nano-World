package nano.remexp.net;

import java.net.Socket;


/**
 * An interface that enables classes to receive sockets from connectors.
 * 
 * @author Dominic Bosch
 * @version 1.1 23.08.2012
 */
public interface SocketReceiver{
	
	/**
	 * A new socket has been found and was reported, thus adding it
	 * 
	 * @param newSocket The new connected socket.
	 * @param port The local port on which it has been connected.
	 */
    public void newSocket(Socket newSocket, int port);
}

