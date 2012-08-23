package nano.remexp.broadcaster;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import nano.debugger.Debg;
import nano.remexp.ThreadHandler;

/**
 * This class enables to scan on a port for incoming sockets and to add them to the desired listener.
 * 
 * @author Dominic Bosch
 * @version 1.1 29.08.2012
 */
public class ClientSocketAcceptor extends ThreadHandler{
	private FilteringPool clientPool;
	private ServerSocket socketAcceptor;
	private int listenPort;

	/**
	 * Starts a new thread that listens on a port for connecting clients.
	 * 
	 * @param port	the port on which this thread listens for new connections
	 * @param pool	the client pool that will accept the new clients
	 */
    public ClientSocketAcceptor(int port, FilteringPool pool){
    	socketAcceptor = null;
	    clientPool = pool;
	    listenPort = port;
	    super.start(this.getClass().getSimpleName());
    }

    /**
     * Listening on the predefined port for incoming sockets. Waiting 10 seconds if the port is used already.  
     */
	@Override
	public void doTask() {
        Socket dummySocket;
        Debg.print("Listening for inbound sockets on port: " + listenPort);
        try{
            if(socketAcceptor == null) socketAcceptor = new ServerSocket(listenPort);
            dummySocket = socketAcceptor.accept();
            clientPool.newSocket(dummySocket, listenPort);
            Debg.print("Added new socket on port " + listenPort + " for remote " 
						+ dummySocket.getInetAddress().getHostAddress() + ":" + dummySocket.getPort());
        }  catch(IOException e){
			try {Thread.sleep(10000);} catch (InterruptedException e1) {Debg.err("Couldn't sleep...");}
	        Debg.err("Error: port " + listenPort + " already in use, please free the resource, trying again in 10 seconds...");
        }
	}

	/**
	 * Calling stopThread method, trying to close the ServerSocket and then unlinking all resources. 
	 */
	@Override
	public void shutDown() {
		stopThread();
		if(socketAcceptor != null){
			try {socketAcceptor.close();} catch (IOException e) {Debg.err("Error on shut down");}
			socketAcceptor = null; 
		}
		clientPool = null;
	}
}
