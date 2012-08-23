package nano.remexp;


/**
 * An interface for all the classes that hold a stream
 * 
 * @author Dominic Bosch
 * @version 1.1 23.08.2012
 */
public interface StreamReceiver{
	/**
	 * Writes an array of bytes to the object holding one end of the pipe.
	 * 
	 * @param b The byte array to be written.
	 */
	public abstract void write(byte[] b);
	
}