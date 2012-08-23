package nano.remexp.net;


/**
 * An interface that enables classes to act as event socket listeners and
 * perform actions on events.
 * 
 * @author Dominic Bosch
 * @version 1.1 23.08.2012
 */
public interface EventSocketListener {
	/**
	 * Takes actions if there is an incoming event on the socket.
	 * 
	 * @param sock The socket on which the event has been detected.
	 * @param msg The message that has been sent.
	 */
	public abstract void performSocketEvent(EventSocket sock, String msg);
	
	/**
	 * After the socket hasn't reacted for a while it is removed.
	 * 
	 * @param sock The socket to be removed.
	 */
	public abstract void removeEventSocket(EventSocket sock);

	/**
	 * Used to login the client connection with a username and password.
	 * 
	 * @param sock The socket that tries to gain privileges.
	 * @param user The username used to gain privileges.
	 * @param pass The password passed as a MD-5 hash in case of a non-LiLa login, empty if LiLa login shall be attempted.
	 */
	public abstract void login(EventSocket sock, String user, String pass);
}
