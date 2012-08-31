package nano.remexp.broadcaster.locks;

import nano.debugger.Debg;
import nano.remexp.Parser;
import nano.remexp.broadcaster.RemoteExperimentMonitor;
import nano.remexp.broadcaster.Sample;
import nano.remexp.net.NanoComm;

/**
 * The lock for the movetip command that moves the tip on the sample into one direction.
 * 
 * @author Dominic Bosch
 * @version 1.1 29.8.2012
 */
public class MoveTipLock extends LockHolder {
	public MoveTipLock(RemoteExperimentMonitor mon) {super(mon);}
	
	@Override public long getLockTimeoutInSeconds() {return 20;}
	@Override public synchronized LockHolder setLockAction(String event) {
		boolean validMove = false;
		String val = Parser.getValue(event, "value");
		if(val != null) {
			int steps = 20; //TODO check correct scaling on move steps
			String axis = " ";
			String direction = "";
			//TODO check correct direction
			if(val.equals("up")){
				axis = "y";
				steps *= -1;
				direction = "negative";
			} else if(val.equals("down")){
				axis = "y";
				direction = "positive";
			} else if(val.equals("left")){
				axis = "x";
				steps *= -1;
				direction = "negative";
			} else if(val.equals("right")){
				axis = "x";
				direction = "positive";
			}
			Sample sample = monitor.getSample();
			if(sample!=null){
				if(axis.equals("x")) validMove = sample.updateTipPosition(steps, 0);
				else validMove = sample.updateTipPosition(0, steps);
				if(validMove) {
					monitor.setCurrentState(NanoComm.STATE_STAGEMOVING);
					monitor.broadcastInfoToClients("Moving on sample in " + direction + " " + axis + " direction");
					monitor.sendToRemExp(NanoComm.strCmd("adjustaxis name=" + axis + " value=" + steps));
					return this;
				}
			}
		}
		return null;
	}
	@Override protected String releaseLockCommand() {return "Customposition : ";}
	@Override protected LockHolder releaseThisLock() {
		monitor.setCurrentState(NanoComm.STATE_STAGEREADY);
		monitor.broadcastToClients("Reached custom position");
		return null;
	}
	@Override public String[] getAbortCommands() {return null;}
	@Override public LockHolder takeAbortActions(String reason) {
		monitor.setCurrentState(NanoComm.STATE_STAGEREADY);
		monitor.broadcastToClients("Moving on sample has been aborted due to the reason: " + reason);
		Debg.print("Moving on sample has been aborted due to the reason: " + reason);
		return null;
	}
	@Override public int[] getAllowedPrivileges() {
		return new int[]{NanoComm.PRIV_ADMIN, NanoComm.PRIV_ADVANCED};
	}
}
