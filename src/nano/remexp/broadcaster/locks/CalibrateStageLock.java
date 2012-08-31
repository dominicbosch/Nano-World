package nano.remexp.broadcaster.locks;

import nano.debugger.Debg;
import nano.remexp.broadcaster.RemoteExperimentMonitor;
import nano.remexp.net.NanoComm;

/**
 * The lock for the calibratestage command that moves the robotic stage to its initial position.
 * 
 * @author Dominic Bosch
 * @version 1.1 29.8.2012
 */
public class CalibrateStageLock extends LockHolder {
	public CalibrateStageLock(RemoteExperimentMonitor mon) {super(mon);}
	
	@Override public long getLockTimeoutInSeconds() {return 120;}
	@Override public synchronized LockHolder setLockAction(String event) {
		monitor.sendToRemExp(NanoComm.strCmd(NanoComm.CMD_CALIBRATESTAGE));
		monitor.setCurrentState(NanoComm.STATE_STAGEMOVING);
		monitor.broadcastInfoToClients("Calibrating the stage, please be patient");
		return this;
	}
	@Override protected String releaseLockCommand() {
		Debg.print("was asked to release lock");
		return "Instrument Calibrated";
	}
	@Override protected LockHolder releaseThisLock() {
		setState();
		monitor.broadcastInfoToClients("Calibration of stage successful");
		Debg.print("Lock has been released for stage calibration");
		return null;
	}
	@Override public String[] getAbortCommands() {return null;}
	@Override public LockHolder takeAbortActions(String reason) {
		setState();
		monitor.broadcastInfoToClients("Calibration of stage aborted due to reason: " + reason);
		Debg.print("Lock has been aborted for stage calibration due to reason: " + reason);
		return null;
	}
	private void setState(){
		monitor.setSample(null);
		monitor.broadcastToClients(NanoComm.strParam(NanoComm.PARAM_STAGEPOSITION + " value=-1"));
		monitor.setCurrentState(NanoComm.STATE_STAGECALIBRATED);
	}
	@Override public int[] getAllowedPrivileges() {
		return new int[]{NanoComm.PRIV_ADMIN, NanoComm.PRIV_ADVANCED, NanoComm.PRIV_CONTROLLER};
	}
}
