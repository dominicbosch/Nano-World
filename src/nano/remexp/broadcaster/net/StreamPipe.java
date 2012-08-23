package nano.remexp.broadcaster.net;

import java.io.IOException;
import java.io.PipedInputStream;
import java.util.Vector;

import nano.debugger.Debg;
import nano.remexp.ThreadHandler;
import nano.remexp.net.StreamSocket;


/**
 * The StreamPipe is the stream broadcaster of the input from the remote experiment
 * to the client sockets that receive the stream data.
 * 
 * @author Dominic Bosch
 * @version 1.1 23.08.2012
 */
public class StreamPipe extends ThreadHandler {
	private PipedInputStream datastream;
	private Vector<StreamSocket> arrOutStreams;

	/**
	 * Instantiates an object of this class and starts a thread that reads the
	 * input of the pipe periodically.
	 * 
	 * @param src The pipe on which this object should listen for stream data.
	 */
	public StreamPipe(PipedInputStream src) {
		datastream = src;
		arrOutStreams = new Vector<StreamSocket>();
	    super.start(this.getClass().getSimpleName());
	}

	/**
	 * When a new stream socket is being connected it is added to the internal storage. 
	 * 
	 * @param newNetStream The new stream socket.
	 */
	protected void plugStreamSocket(StreamSocket sock) {
		if(arrOutStreams != null) arrOutStreams.add(sock);
	}


	/**
	 * When a new stream socket is being connected it is added to the internal storage. 
	 * 
	 * @param newNetStream The new stream socket.
	 */
	protected void removeStreamSocket(StreamSocket sock) {
		if(arrOutStreams != null) arrOutStreams.remove(sock);
	}

	/**
	 * The loop that passes stream data from the pipe to the client stream sockets
	 * FIXME implement wait and notify on streampipe
	 */
	public void doTask() {
		//FIXME implement wait notify and check when broken stream pipe happens
		byte currentBytes[] = new byte[256];
		try {
			while (datastream.read(currentBytes, 0, 256) != -1) {
		    	for(StreamSocket ss: arrOutStreams){
					ss.put(currentBytes);
					//if(ss.isDead()) ss.shutDown();
				}
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					Debg.err("Interrupted");
				}
			}
		} catch (IOException e) {
			Debg.err("No data from Foreign Source. Waiting...");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException ie) {}
		}
	}

	/**
	 * Shutting down all stream sockets and this thread.
	 */
	@Override
	public void shutDown() {
		stopThread();
		try {
			if(datastream!=null) datastream.close();
		} catch (IOException e) {
			Debg.err("error close sourcepipestream "+e.getMessage());
		}
		//TODO if we don't use the tmp vector we get a concurrencymodificationexception
		Vector<StreamSocket> tmp = new Vector<StreamSocket>();
		if(arrOutStreams!=null){
	    	for(StreamSocket ss: arrOutStreams) tmp.add(ss);
	    	arrOutStreams.clear();
	    	arrOutStreams = null;
		}
    	for(StreamSocket ss: tmp) ss.shutDown();
    	tmp.clear();
    	tmp = null;
		datastream = null;
        Debg.print("Stopped running!");
	}

}