package nano.remexp.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;

import nano.debugger.Debg;
import nano.remexp.ThreadHandler;

/**
 * The lowest layer of the communication between the different architectural parts.
 * Through this we are able to observe all currently running sockets and close them
 * if they aren't useful anymore.
 * 
 * @author Dominic Bosch
 * @version 1.1 23.08.2012
 */
public abstract class NanoSocket extends ThreadHandler{
	protected Socket clientSocket = null;
    protected BufferedOutputStream bos;
    protected BufferedInputStream bis;
	private long lastSignOfLife;
	private String remoteHost = "[NONE]";
	private int remotePort = -1;
	private boolean isActive; // this is especially used for the stream while there are no measurments happening
	private boolean isDead; // this is especially used for the stream while there are no measurments happening

	public NanoSocket(String className, Socket sock){
		isDead = false;
		clientSocket = sock;
		try {
			bos = new BufferedOutputStream(sock.getOutputStream(), 1024);
			bis = new BufferedInputStream(sock.getInputStream());
		} catch (Exception e) {
			Debg.err("Socket Init Error");
		}
        remoteHost = clientSocket.getInetAddress().getHostAddress();
        remotePort = clientSocket.getPort();
        gotLifeSign();
	    super.start(className + ": " + remoteHost);
	    isActive = false;
	}

	/**
	 * Returns the remote host's address in the format [url]:[port]
	 * 
	 * @return a string containing the host address and port, e.g.: "127.0.0.1:12345"
	 */
	public String getRemoteID() {
		return remoteHost + ":" + remotePort;
	}
	
	/**
	 * Get the last time stamp when we heard from this socket.
	 * 
	 * @return the time stamp in number of milliseconds of when this socket last has been active. 
	 */
	public long getLastSignOfLife(){
		return lastSignOfLife;
	}

	/**
	 * Since we heard from this socket, the time stamp is updated.
	 */
	public void gotLifeSign(){
        lastSignOfLife = new Date().getTime();
	}

	/**
	 * Returns the information whether this socket should be active
	 * @return	true if it is in a an active state (e.g. the stream socket during scanning), else false
	 * FIXME this active state in the stream socket needs to be checked during measurements
	 */
	public boolean isActiveSocket(){
		return isActive;
	}
	
	protected void setActiveSocket(boolean theTruth){
		isActive = theTruth;
	}
	
    /**
     * Tries to get information about the socket.
     * 
     * @return The url of the remote host if the socket existed, else '[NONE]'
     */
    public String getRemoteHost(){
    	return remoteHost;
    }

    /**
     * Retrieves the remote port of this socket.
     * 
     * @return The port to which this socket is connected.
     */
    protected int getRemotePort(){
    	return remotePort;
    }

    /**
     * Writes a number of bytes onto the socket.
     * 
     * @param b the byte array to be sent.
     */
    public void put(byte[] b){
        if(bos != null) try{
	        bos.write(b,0,256);
	        bos.flush();
	        gotLifeSign();
        } catch(IOException ev){
        	Debg.err("This socket is dead:" + getRemoteID());
        	isDead = true;
        }
    }

	/**
	 * This method should be overwritten by the classes that extend this basic class.
	 * @see nano.remexp.ThreadHandler#doTask()
	 */
	@Override public void doTask() {}
	
    /**
     * Should implement the ping logic for this socket in order to check it for connectivity.
     */
    public abstract void ping();
    
	/**
	 * This method needs to implement the logic if the socket should remove itself.
	 */
	protected abstract void removeYourself();
	
	@Override public synchronized void shutDown() {
		stopThread();
        try{
        	if(bos != null) bos.close();
        	if(bis != null) bis.close();
        	if(clientSocket != null) clientSocket.close();
        } catch(IOException e){
        	Debg.err("Unable to close socket");
        }
    	bos = null;
    	bis = null;
    	clientSocket = null;
		removeYourself();
	}
}
