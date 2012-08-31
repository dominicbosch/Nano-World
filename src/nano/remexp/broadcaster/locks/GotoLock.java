package nano.remexp.broadcaster.locks;

import nano.debugger.Debg;
import nano.remexp.broadcaster.RemoteExperimentMonitor;
import nano.remexp.broadcaster.Sample;
import nano.remexp.net.NanoComm;

/**
 * The lock for the goto# commands that move the robotic stage to a sample.
 * These locks are associated with one sample that is read from the configuration file.
 * 
 * @author Dominic Bosch
 * @version 1.1 29.8.2012
 */
public class GotoLock extends LockHolder {
	Sample sample;
	
	public GotoLock(RemoteExperimentMonitor mon) {super(mon);}
	
	public void init(Sample smpl){sample = smpl;}
	@Override public long getLockTimeoutInSeconds() {return 60;}
	@Override public synchronized LockHolder setLockAction(String event) {
		monitor.sendToRemExp(NanoComm.strCmd(sample.getLockCommand()));
		monitor.setCurrentState(NanoComm.STATE_STAGEMOVING);
		monitor.broadcastInfoToClients("Moving to " + sample.getSampleName() + ", please be patient");
		return this;
	}
	@Override protected String releaseLockCommand() {return sample.getReleaseCommand();}
	@Override protected LockHolder releaseThisLock() {
		monitor.setSample(sample);
		monitor.setTipPosition(sample.getPosX(), sample.getPosY());
		sendAndSetStageReadyState();
		monitor.broadcastInfoToClients("Finished moving to " + sample.getSampleName());
		Debg.print("Lock has been released for movement to " + sample.getSampleName());
		return null;
	}
	@Override public String[] getAbortCommands() {return null;}
	@Override public LockHolder takeAbortActions(String reason) {
		monitor.broadcastInfoToClients("Aborted moving to " + sample.getSampleName() + " due to reason: " + reason);
		Debg.print("Lock has been released for movement to " + sample.getSampleName() + " due to reason: " + reason);
		LockHolder lock = monitor.getLockForCommand(NanoComm.strCmd(NanoComm.CMD_CALIBRATESTAGE));
		lock.setLockAction("");
		return lock;
	}
	private void sendAndSetStageReadyState(){
		monitor.broadcastToClients(NanoComm.strParam(NanoComm.PARAM_STAGEPOSITION + " value=" + sample.getSampleID()));
		monitor.setCurrentState(NanoComm.STATE_STAGEREADY);
	}
	@Override public int[] getAllowedPrivileges() {
		return new int[]{NanoComm.PRIV_ADMIN, NanoComm.PRIV_ADVANCED, NanoComm.PRIV_CONTROLLER};
	}
}
