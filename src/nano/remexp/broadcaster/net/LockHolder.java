/*
 * Copyright (c) 2011 by Tibor Gyalog, Raoul Schneider, Dino Keller,
 * Christian Wattinger, Martin Guggisberg, Dominic Bosch and The 
 * Regents of the University of Basel. All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software and its
 * documentation for any purpose, without fee, and without written agreement is
 * hereby granted, provided that the above copyright notice and the following
 * two paragraphs appear in all copies of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF BASEL BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * BASEL HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE UNIVERSITY OF BASEL SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE.  THE SOFTWARE PROVIDED HEREUNDER IS
 * ON AN "AS IS" BASIS, AND THE UNIVERSITY OF BASEL HAS NO OBLIGATION TO
 * PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 * Authors: Tibor Gyalog, Raoul Schneider, Dino Keller,
 * Christian Wattinger, Martin Guggisberg, Dominic Bosch <vexp@nano-world.net>
 * 
 */ 

package nano.remexp.broadcaster.net;

/**
 * The interface for the locks that are being linked with commands to the remote experiment.
 * As soon as the broadcaster receives such a valid command it sets the lock for it and waits
 * for the appropriate reply of the remote experiment.
 * 
 * @author Dominic Bosch
 * @version 1.0 21.10.2011
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
