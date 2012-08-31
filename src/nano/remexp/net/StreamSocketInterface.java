package nano.remexp.net;

/**
 * The interface for stream socket holders to allow them to be
 * informed about a removed stream socket.
 * 
 * @author Dominic Bosch
 * @version 1.1 23.08.2012
 */
public interface StreamSocketInterface {
	/**
	 * The stream socket that is removed from the stream socket holder
	 * 
	 * @param sock	the stream socket that is removed
	 */
	public abstract void removeStreamSocket(StreamSocket sock);
}
