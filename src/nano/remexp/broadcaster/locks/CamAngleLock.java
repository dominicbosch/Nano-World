package nano.remexp.broadcaster.locks;

import nano.debugger.Debg;
import nano.remexp.broadcaster.RemoteExperimentMonitor;
import nano.remexp.net.NanoComm;

/**
 * Sets the camera angle in the AFM to another position.
 * 
 * @author Dominic Bosch
 * @version 1.1 29.8.2012
 */
public class CamAngleLock extends LockHolder {
	private int camAngle;

	public CamAngleLock(RemoteExperimentMonitor mon) {
		super(mon);
        camAngle = 0;
	}
	@Override public long getLockTimeoutInSeconds() {return 5;}
	@Override public LockHolder setLockAction(String event) {
		switch(camAngle++){
			case 0:
				monitor.sendToRemExp(NanoComm.strCmd("videoa"));
				break;
			case 1:
			default:
				monitor.sendToRemExp(NanoComm.strCmd("videob"));
				camAngle = 0;
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {}
		return this;
	}
	@Override protected String releaseLockCommand() {return "StatusPrompt";}
	@Override protected LockHolder releaseThisLock() {
		Debg.print("released");
		return null;
	}
	@Override public String[] getAbortCommands() {return null;}
	@Override public LockHolder takeAbortActions(String reason) {return null;}
	@Override public int[] getAllowedPrivileges() {
		return new int[]{NanoComm.PRIV_ADMIN, NanoComm.PRIV_ADVANCED, NanoComm.PRIV_CONTROLLER};
	}
}

