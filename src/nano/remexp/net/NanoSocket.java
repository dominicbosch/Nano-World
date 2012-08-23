package nano.remexp.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;

import nano.debugger.Debg;
import nano.remexp.ThreadHandler;

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

    public boolean isDead(){
    	return isDead;
    }
    
    public abstract void ping();
    
	/**
	 * This method should be overwritten by the classes that extend this basic class.
	 * @see nano.remexp.ThreadHandler#doTask()
	 */
	@Override
	public void doTask() {}
	
	protected abstract void removeYourself();
	
	@Override
	public synchronized void shutDown() {
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
