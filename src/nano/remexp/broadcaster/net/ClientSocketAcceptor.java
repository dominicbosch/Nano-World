package nano.remexp.broadcaster.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import nano.debugger.Debg;
import nano.remexp.ThreadHandler;

/**
 * This class enables to scan on a port for incoming sockets and to add them to the desired listener.
 * 
 * @author Dominic Bosch
 * @version 1.1 23.08.2012
 */
public class ClientSocketAcceptor extends ThreadHandler{
	private FilteringPool myListener;
	private ServerSocket myEventAcceptSocket;
	private int myPort;

	/**
	 * Instantiates an object of this class and calls the class {@see ThreadHandler} with
	 * this' class name 
	 * 
	 * @param newPort
	 * @param newListener
	 */
    public ClientSocketAcceptor(int newPort, FilteringPool newListener){
    	myEventAcceptSocket = null;
	    myListener = newListener;
	    myPort = newPort;
	    super.start(this.getClass().getSimpleName());
    }

    /**
     * Listening on the predefined port for incoming sockets. Waiting 10 seconds if the port is used already.  
     */
	@Override
	public void doTask() {
        Socket dummySocket;
        Debg.print("Listening for inbound sockets on port: " + myPort);
        try{
            if(myEventAcceptSocket == null) myEventAcceptSocket = new ServerSocket(myPort);
            dummySocket = myEventAcceptSocket.accept();
            myListener.newSocket(dummySocket, myPort);
            Debg.print("Added new socket on port " + myPort + " for remote " 
						+ dummySocket.getInetAddress().getHostAddress() + ":" + dummySocket.getPort());
        }  catch(IOException e){
			try {Thread.sleep(10000);} catch (InterruptedException e1) {Debg.err("Couldn't sleep...");}
	        Debg.err("Error: port " + myPort + " already in use, please free the resource, trying again in 10 seconds...");
        }
	}

	/**
	 * Calling stopThread method, trying to close the ServerSocket and then unlinking all resources. 
	 */
	@Override
	public void shutDown() {
		stopThread();
		if(myEventAcceptSocket != null){
			try {myEventAcceptSocket.close();} catch (IOException e) {Debg.err("Error on shut down");}
			myEventAcceptSocket = null; 
		}
		myListener = null;
	}
}
