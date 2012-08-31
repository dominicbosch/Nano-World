package nano.remexp.broadcaster.locks;

import nano.remexp.Parser;
import nano.remexp.broadcaster.RemoteExperimentMonitor;
import nano.remexp.net.NanoComm;

/**
 * Sets the scan range of the AFM.
 * 
 * @author Dominic Bosch
 * @version 1.1 29.8.2012
 */
public class ScanRangeLock extends LockHolder {
	public ScanRangeLock(RemoteExperimentMonitor mon) {super(mon);}
	
	@Override public long getLockTimeoutInSeconds() {return 20;}
	@Override public synchronized LockHolder setLockAction(String event) {
		String val = Parser.getValue(event, "value");
		if(val != null) {
			try{
				monitor.sendToRemExp(NanoComm.strCmd("set name=scanrange value=" + val));
				monitor.setScanRange(Integer.parseInt(val));
				monitor.broadcastToClients(NanoComm.strParam(NanoComm.PARAM_SCANRANGE + " value=" + val));
				monitor.broadcastInfoToClients("Scan range set to " + val);
			} catch(NumberFormatException e){}
		}
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
