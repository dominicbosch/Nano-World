package nano.remexp.broadcaster.locks;

import nano.debugger.Debg;
import nano.remexp.broadcaster.RemoteExperimentMonitor;
import nano.remexp.net.NanoComm;

/**
 * The lock for the autoapproach command that moves the tip onto 
 * the sample until it is approached.
 * 
 * @author Dominic Bosch
 * @version 1.1 29.8.2012
 */
public class AutoApproachLock extends LockHolder {
	public AutoApproachLock(RemoteExperimentMonitor mon) {super(mon);}
	
	@Override public long getLockTimeoutInSeconds() {return 300;}
	@Override public synchronized LockHolder setLockAction(String event) {
		monitor.sendToRemExp(NanoComm.strCmd(NanoComm.CMD_AUTOAPPROACH));
		monitor.setCurrentState(NanoComm.STATE_APPROACHING);
		monitor.broadcastInfoToClients("Approaching the sample, please be patient");
		return this;
	}
	@Override protected String releaseLockCommand() {return "StatusApproached";}
	@Override protected LockHolder releaseThisLock() {
		monitor.setCurrentState(NanoComm.STATE_APPROACHED);
		monitor.broadcastToClients(NanoComm.strParam(NanoComm.PARAM_SCANSTART + " value=0"));
		monitor.broadcastInfoToClients("Tip has successfully approached the sample!");
		Debg.print("Lock has been released for auto approach");
		return null;
	}
	@Override public String[] getAbortCommands() {return new String[]{NanoComm.CMD_STOPAPPROACH};}
	@Override public LockHolder takeAbortActions(String reason) {
		monitor.sendToRemExp(NanoComm.strCmd(NanoComm.CMD_STOPAPPROACH));
		monitor.setCurrentState(NanoComm.STATE_STAGEREADY);
		monitor.broadcastInfoToClients("Tip approach has been aborted due to the reason: " + reason);
		Debg.print("Lock has been aborted for auto approach due to the reason: " + reason);
		//TODO if approach is aborted in the exact moment when the tip approaches, the lock is removed and stage is ready
		// but the stage isn't ready since the tip is approached... the command below should fix this issue but still needs to be checked$	
		monitor.sendToRemExp(NanoComm.strCmd(NanoComm.CMD_WITHDRAW));
		return null;
	}
	@Override public int[] getAllowedPrivileges() {
		return new int[]{NanoComm.PRIV_ADMIN, NanoComm.PRIV_ADVANCED, NanoComm.PRIV_CONTROLLER};
	}
}
