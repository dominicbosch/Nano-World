package nano.remexp.broadcaster.locks;

import nano.remexp.broadcaster.RemoteExperimentMonitor;
import nano.remexp.net.NanoComm;

/**
 * The NoLock class that is used for commands that are allowed
 * but do not need a lock.
 * 
 * @author Dominic Bosch
 * @version 1.1 29.8.2012
 */
public class NoLock extends LockHolder {
	public NoLock(RemoteExperimentMonitor mon) {super(mon);}
	
	@Override public long getLockTimeoutInSeconds() {return 0;}
	@Override public LockHolder setLockAction(String event) {
		monitor.sendToRemExp(event);
		return null;
	}
	@Override protected String releaseLockCommand() {return null;}
	@Override protected LockHolder releaseThisLock() {return null;}
	@Override public String[] getAbortCommands() {return null;}
	@Override public LockHolder takeAbortActions(String reason) {return null;}
	@Override public int[] getAllowedPrivileges() {
		return new int[]{NanoComm.PRIV_ADMIN, NanoComm.PRIV_ADVANCED, NanoComm.PRIV_CONTROLLER};
	}
}