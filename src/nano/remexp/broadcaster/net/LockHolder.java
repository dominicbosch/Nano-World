package nano.remexp.broadcaster.net;

/**
 * The interface for the locks that are being linked with commands to the remote experiment.
 * As soon as the broadcaster receives such a valid command it sets the lock for it and waits
 * for the appropriate reply of the remote experiment.
 * 
 * @author Dominic Bosch
 * @version 1.1 23.08.2012
 */
abstract public class LockHolder {
	/**
	 * Returns the durance this lock is valid in order to remove it on timeout.
	 * 
	 * @return The durance in seconds this lock is valid;
	 */
	abstract protected long getLockTimeoutInSeconds();
	
	/**
	 * This method will be invoked when the lock is set.
	 * 
	 * @return The command to be sent to all clients when this lock is set.
	 */
	abstract protected LockHolder setLockAction(String event);
	
	/**
	 * If the remote experiment sends messages and there is a LockHolder locking the remote experiment,
	 * those messages are being parsed through this method of the appropriate LockHolder.
	 * Through defining another Lock in the resolveLock method of the first one, it is possible
	 * to have lock chains, waiting for different commands in a row to arrive.
	 * 
	 * @param msg The message sent by the remote experiment
	 * @return The LockHolder that now holds the lock or null if the lock is released again.
	 */
	protected synchronized LockHolder resolveLock(String msg) {
		String freeingCommand = releaseLockCommand();
		if(freeingCommand == null) return this;
		if(msg.indexOf(freeingCommand) > -1) {
			return releaseThisLock();
		} else return this;
	}

	/**
	 * This function returns the command that needs to be sent by the remote experiment
	 * in order to release this lock.
	 * 
	 * @return The command releasing this lock.
	 */
	abstract protected String releaseLockCommand();
	
	/**
	 * This is the releasing action, most likely an event broadcasted to all clients.
	 * 
	 * @return The lock that is now locking the experiment. This is implemented if locks
	 * build a row that the remote experiment needs to resolve.
	 */
	abstract protected LockHolder releaseThisLock();
	
	/**
	 * Returns the commands that are allowed to abort the current state.
	 * 
	 * @return An array of commands which are able to abort the current state.
	 */
	abstract protected String[] getAbortCommands();
	
	/**
	 * After this lock has been aborted, this method is invoked in order to take actions such as inform the user.
	 * 
	 * @param reason The reason why this lock has been aborted.
	 */
	abstract protected LockHolder takeAbortActions(String reason);
}
