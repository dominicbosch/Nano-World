package nano.remexp.broadcaster.locks;

import nano.debugger.Debg;
import nano.remexp.broadcaster.RemoteExperimentMonitor;
import nano.remexp.net.NanoComm;

/**
 * The lock for the withdraw command that lifts the tip
 * off from the sample after it was approached.
 * 
 * @author Dominic Bosch
 * @version 1.1 29.8.2012
 */
public class WithdrawLock extends LockHolder {
	public WithdrawLock(RemoteExperimentMonitor mon) {super(mon);}
	
	@Override public long getLockTimeoutInSeconds() {return 20;}
	@Override public synchronized LockHolder setLockAction(String event) {
		monitor.sendToRemExp(NanoComm.strCmd(NanoComm.CMD_WITHDRAW));
		monitor.setCurrentState(NanoComm.STATE_WITHDRAWING);
		monitor.broadcastInfoToClients("Withdrawing from the sample, please be patient");
		return this;
	}
	@Override protected String releaseLockCommand() {return "Withdrawn";}
	@Override protected LockHolder releaseThisLock() {
		monitor.setCurrentState(NanoComm.STATE_STAGEREADY);
		monitor.broadcastInfoToClients("Tip has successfully been withdrawn!");
		Debg.print("Lock has been released for withdraw");
		return null;
	}
	@Override public String[] getAbortCommands() {return null;}
	@Override public LockHolder takeAbortActions(String reason) {
		monitor.setCurrentState(NanoComm.STATE_STAGEREADY);
		monitor.broadcastInfoToClients("Withdraw has been aborted due to reason: " + reason);
		Debg.print("Withdraw has been aborted due to reason: " + reason);
		return null;
	}
	@Override public int[] getAllowedPrivileges() {
		return new int[]{NanoComm.PRIV_ADMIN, NanoComm.PRIV_ADVANCED, NanoComm.PRIV_CONTROLLER};
	}
}
