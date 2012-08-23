package nano.remexp.net;

import java.io.IOException;
import java.net.Socket;
import java.util.Vector;

import nano.debugger.Debg;
import nano.remexp.StreamReceiver;

/**
 * This class handles the stream sockets that receive stream data from the
 * remote experiment.
 * 
 * @author Dominic Bosch
 * @version 1.1 23.08.2012
 */
public class StreamSocket extends NanoSocket{
    private Vector<StreamReceiver> myDisplays = new Vector<StreamReceiver>();
    private StreamSocketInterface chief;
    /**
     * The constructor creates an in and out buffer on the socket.
     * 
     * @param sock The socket over which the data streaming happens.
     */
    public StreamSocket(Socket sock, StreamSocketInterface ssi){
    	super(StreamSocket.class.getSimpleName(), sock);
    	chief = ssi;
    }

    /**
     * The lifecycle loop of this thread that checks the socket for incoming
     * data and sends it to its connected displays. 
     */
    public void doTask(){
		byte theLine[] = new byte[256];
		try{
			while(bis.read(theLine, 0, 256)!=-1){
			    for(StreamReceiver sr: myDisplays) sr.write(theLine);
			}
			Debg.err("bis.read was -1: " + getRemoteID());
		} catch(IOException e) {
			Debg.err("Connection lost, shutting this socket down for " + getRemoteID());
    	}
		theLine = null;
		shutDown();
    }

    /**
     * Adds a display in order for it to treceive the stream too.
     * 
     * @param newDisplay The new display that also wants to receive the streamed data.
     */
    public void plugDisplay(StreamReceiver newDisplay){
    	if(myDisplays != null) myDisplays.add(newDisplay);
    }

	@Override public void ping() {} // Ping is not necessary for the data stream
	@Override protected void removeYourself() {
		if(chief != null) chief.removeStreamSocket(this);
	}

    /**
     * Shuts down the connection and sets all references to null.
     */
    public void shutDown(){
    	if(myDisplays != null) myDisplays.clear();
    	myDisplays = null;
		super.shutDown();
		chief = null;
    }

}
