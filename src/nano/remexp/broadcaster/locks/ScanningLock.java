package nano.remexp.broadcaster.locks;

import java.util.Date;

import nano.debugger.Debg;
import nano.remexp.ThreadHandler;
import nano.remexp.broadcaster.RemoteExperimentMonitor;
import nano.remexp.net.NanoComm;

/**
 * The lock for the scanning command that initiates the scan of the sample.
 * it instantiates a thread that checks whether the stream stopped sending data yet.
 * 
 * @author Dominic Bosch
 * @version 1.1 29.8.2012
 */
public class ScanningLock extends LockHolder {
    private ScanStopChecker scanObserver;
    
	public ScanningLock(RemoteExperimentMonitor mon) {
		super(mon);
	    scanObserver = null;
	}
	@Override public long getLockTimeoutInSeconds() {return 10*60;}
	@Override public synchronized LockHolder setLockAction(String event) {
		monitor.sendToRemExp(NanoComm.strCmd(NanoComm.CMD_START));
		monitor.setCurrentState(NanoComm.STATE_SCANNING);
		long now = new Date().getTime() / 1000;
		monitor.setScanStart(now);
		startScanObserver();
		monitor.broadcastToClients(NanoComm.strParam(NanoComm.PARAM_SCANSTART + " value=" + now));
		monitor.broadcastInfoToClients("Started scanning the sample");
		return this;
	}
	@Override protected String releaseLockCommand() {return "Scan finished";}
	@Override protected LockHolder releaseThisLock() {
		monitor.setCurrentState(NanoComm.STATE_APPROACHED);
		monitor.setScanStart(0);
		stopScanObserver();
		monitor.broadcastInfoToClients("Finished scanning the sample!");
		Debg.print("Lock has been released for scanning");
		return null;
	}
	@Override public synchronized String[] getAbortCommands() {return new String[]{NanoComm.CMD_STOP};}
	@Override public LockHolder takeAbortActions(String reason) {
		monitor.sendToRemExp(NanoComm.strCmd(NanoComm.CMD_STOP));
		monitor.setCurrentState(NanoComm.STATE_APPROACHED);
		monitor.setScanStart(0);
		stopScanObserver();
		monitor.broadcastToClients(NanoComm.strParam(NanoComm.PARAM_SCANSTART + " value=0"));
		monitor.broadcastInfoToClients("Scanning aborted du to the reason: " + reason);
		return null;
	}
	
    /**
     * Starts the scan observer that checks whether no stream data is being retrieved
     * from the remote experiment anymore.
     */
    private void startScanObserver(){
        monitor.setStreamTimeStamp(new Date());
        scanObserver = new ScanStopChecker();
        scanObserver.start("Observer for Scan end");
    }
    
    /**
     * Shuts down the scan observer if no scanning is happening at the moment.
     */
    private void stopScanObserver(){
    	monitor.setStreamTimeStamp(null);
    	scanObserver.shutDown();
    	scanObserver = null;
    }
    
	/**
	 * A thread that checks whether the remote experiment stopped sending data.
	 * 
	 * @author Dominic Bosch
	 * @version 1.1 29.8.2012
	 */
    //TODO maybe it's worth extracting this scan stop checker logic into the monitor
	private class ScanStopChecker extends ThreadHandler{
		@Override
		public void doTask() {
			Date lastStreamData = monitor.getStreamTimeStamp();
			if(lastStreamData != null){
				if(lastStreamData.getTime() + 5000 < new Date().getTime()){
					monitor.setLock(takeAbortActions("Stream ended broadcasting"));
				}
			}
		}
		@Override
		public void shutDown() {
			this.stopThread();
		}
	}
	@Override public int[] getAllowedPrivileges() {
		return new int[]{NanoComm.PRIV_ADMIN, NanoComm.PRIV_ADVANCED, NanoComm.PRIV_CONTROLLER};
	}
}

