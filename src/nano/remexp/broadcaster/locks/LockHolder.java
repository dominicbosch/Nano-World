package nano.remexp.broadcaster.locks;

import nano.remexp.broadcaster.RemoteExperimentMonitor;

/**
 * The interface for the locks that are being linked with commands to the remote experiment.
 * As soon as the broadcaster receives such a valid command it sets the lock for it and waits
 * for the appropriate reply of the remote experiment. In any case there is also a time out on each lock
 * after which the lock will be released anyways. Abort actions can be defined in case a locking action
 * should be able to be aborted.
 * 
 * @author Dominic Bosch
 * @version 1.1 23.08.2012
 */
abstract public class LockHolder {
	RemoteExperimentMonitor monitor;
	
	public LockHolder(RemoteExperimentMonitor mon){monitor = mon;}
	
	/**
	 * Returns the time this lock is valid in order to remove it on timeout.
	 * 
	 * @return The time in seconds this lock is valid;
	 */
	public abstract long getLockTimeoutInSeconds();
	
	/**
	 * This method will be invoked when the lock is set.
	 * 
	 * @return The command to be sent to all clients when this lock is set.
	 */
	public abstract LockHolder setLockAction(String event);
	
	/**
	 * If the remote experiment sends messages and there is a LockHolder locking the remote experiment,
	 * those messages are being parsed through this method of the appropriate LockHolder.
	 * Through defining another Lock in the resolveLock method of the first one, it is possible
	 * to have lock chains, waiting for different commands in a row to arrive.
	 * 
	 * @param msg The message sent by the remote experiment
	 * @return The LockHolder that now holds the lock or null if the lock is released again.
	 */
	public synchronized LockHolder resolveLock(String msg) {
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
	public abstract String[] getAbortCommands();
	
	/**
	 * After this lock has been aborted, this method is invoked in order 
	 * to take actions such as informing the user.
	 * 
	 * @param reason The reason why this lock has been aborted.
	 */
	public abstract LockHolder takeAbortActions(String reason);
	
	/**
	 * Returns an array containing all the priviliges that are 
	 * allowed to execute this command.
	 * 
	 * @return an array with integers determining the privileges
	 */
	public abstract int[] getAllowedPrivileges();
}
