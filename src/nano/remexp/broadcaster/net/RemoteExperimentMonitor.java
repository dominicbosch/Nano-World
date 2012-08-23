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

import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import nano.debugger.Debg;
import nano.remexp.Parser;
import nano.remexp.ThreadHandler;
import nano.remexp.net.NanoComm;
import nano.remexp.net.EventSocket;

/**
 * This class handles the events coming from the remote experiments server
 * 
 * @author Dominic Bosch
 * @version 1.0 21.10.2011
 */
public class RemoteExperimentMonitor extends ThreadHandler {

	private FilteringPool pool;
    private ScanStopChecker scanObserver;
    private LockHolder lockHolder;
    private Hashtable<String, LockHolder> commandLocks;
    private long scanStart;
    private int scanRange;
    private int currentState;
    private Sample currentSample;
    private Vector<Sample> allSamples;
    private Date lastStreamData;
    private String remExpName;
    private long lockStarted;
	private int tipPositionX, tipPositionY;
	private int camAngle;
	private boolean isRemExpConnected;

	/**
	 * The constructor for this command processing class. It links to the FilteringPool instance
	 * to pass actions towards the remote experiment server.
	 * 
	 * @param chief The restricted server instance that runs as middleware between the clients
	 * and the remote experiments server.
	 */
	public RemoteExperimentMonitor(FilteringPool chief) {
	    remExpName = "RemExp";
		pool = chief;
		initParser();
		allSamples = new Vector<Sample>();
		initVars();
        setLock(null);
        scanObserver = null;
        lastStreamData = null;
        camAngle = 0;
	    super.start(this.getClass().getSimpleName());
		Debg.print("Remote experiments monitor initialized");
	}

	/**
	 * This command initiates monitor. All known commands that are being sent further to the
	 * remote experiment need to be implemented here. If they don't necessarily lock the
	 * remote experiment server they will still have to be associated with the NoLock class
	 * in order to ensure the command is being passed to the remote experiment.
	 */
	private void initParser() {
		LockHolder notLocking = new NoLock();
		commandLocks = new Hashtable<String, LockHolder>();
		commandLocks.put(NanoComm.CMD_CALIBRATESTAGE, new CalibrateStageLock());
		commandLocks.put(NanoComm.CMD_AUTOAPPROACH, new AutoApproachLock());
		commandLocks.put(NanoComm.CMD_START, new ScanningLock());
		commandLocks.put(NanoComm.CMD_WITHDRAW, new WithdrawLock());
		commandLocks.put(NanoComm.CMD_STOP, notLocking);
		commandLocks.put(NanoComm.CMD_STOPAPPROACH, notLocking);
		commandLocks.put(NanoComm.CMD_SCANRANGE, new ScanRangeLock());
		commandLocks.put(NanoComm.CMD_MOVETIP, new MoveTipLock());
		commandLocks.put(NanoComm.CMD_CAMANGLE, new CamAngleLock());
	}
	
	private void initVars(){
		currentState = NanoComm.STATE_STAGEREADY;
		scanStart = 0;
	    scanRange = 10;
	    currentState = -1;
	    currentSample = null;
		tipPositionX = 0;
		tipPositionY = 0;
		isRemExpConnected = false;
	}
	
	protected void initRemExp(){
		initVars();
		if(lockHolder != null) lockHolder.takeAbortActions("RemExp RESET");
		setLock(null);
		executeAndSetLock(NanoComm.strCmd(NanoComm.CMD_CALIBRATESTAGE));
	}
	
	protected void resetRemExp(){
		initVars();
		setLock(null);
	}

    public void setRemExpConnected(boolean isConnected){
    	Debg.print("Remote Experiment connected: " + isConnected);
    	isRemExpConnected = isConnected;
    	if(isConnected) poolShallBroadcast(NanoComm.strInfo(NanoComm.INFO_REMEXP_CONNECTED));
    	else poolShallBroadcast(NanoComm.strInfo(NanoComm.INFO_REMEXP_DISCONNECTED));
    }
    
	protected void setRemExpName(String name){
		remExpName = name;
		poolShallBroadcast(NanoComm.strParam(NanoComm.PARAM_REMEXPNAME + " value=" + remExpName));
	}
	
    protected void setStreamTimestamp(){
    	lastStreamData = new Date();
    }
	
	private void poolShallBroadcast(String msg){
    	if(pool != null){
    		pool.broadcast(msg);
    	}
	}
	
	private void poolShallSendToRemExp(String msg){
		Debg.print("sending to remexp: " + msg);
    	if(pool != null){
    		pool.sendToRemExp(msg);
    	}
	}
	
	protected void addSample(int sid, String sampleName, String cmd, String rel, int tipX, int tipY, int dX, int dY){
		Sample s = new Sample(sid, sampleName, cmd, rel, tipX, tipY, dX, dY);
		allSamples.add(s);
		commandLocks.put(s.getLockCommand(), new GotoLock(s));
	}
	
	private void setScanStart(long tme){
		scanStart = tme;
	}

	protected long getScanStart(){
		return scanStart;
	}

	/**
	 * This method is used to set and broadcast the actual state of the remote experiment.
	 * 
	 * @param state The state the remote experiment is in now.
	 */
	protected void setCurrentState(int state){
		currentState = state;
		poolShallBroadcast(NanoComm.strState(currentState));
		Debg.print("Setting state " + state);
	}
	
	/**
	 * Informs the new client about the current state of the remote experiment. Sets the correct
	 * parameters in the gui when a new client connects.
	 * 
	 * @param sock The socket that needs to be updated.
	 */
	protected void sendCurrentStateTo(EventSocket sock){
    	if(isRemExpConnected) poolShallBroadcast(NanoComm.strInfo(NanoComm.INFO_REMEXP_CONNECTED));
    	else poolShallBroadcast(NanoComm.strInfo(NanoComm.INFO_REMEXP_DISCONNECTED));
		sock.put(NanoComm.strState(currentState));
		sock.put(NanoComm.strParam(NanoComm.PARAM_SAMPLESCLEAR + " value=all"));
		for(Sample s: allSamples){
			sock.put(NanoComm.strParam(NanoComm.PARAM_SAMPLEINFO 
					+ " value=" + s.getSampleID() 
					+ " name=" + s.getSampleName() 
					+ " command=" + s.getLockCommand()));
		}
		sock.put(NanoComm.strParam(NanoComm.PARAM_SCANRANGE + " value=" + scanRange));
		int smpl;
		if(currentSample == null) smpl = -1;
		else smpl = currentSample.getSampleID();
		
		sock.put(NanoComm.strParam(NanoComm.PARAM_STAGEPOSITION + " value=" + smpl));
		sock.put(NanoComm.strParam(NanoComm.PARAM_SCANSTART + " value=" + scanStart));
		sock.put(NanoComm.strParam(NanoComm.PARAM_REMEXPNAME + " value=" + remExpName));
		sock.put(NanoComm.strPriv(sock.getPrivilege()));
	}
	
	
	/**
	 * Informs the new client about the current state of the remote experiment. Sets the correct
	 * parameters in the gui when a new client connects.
	 * 
	 * @param sock The socket that needs to be updated.
	 */
	protected void printCurrentState(){
    	if(isRemExpConnected) Debg.print(NanoComm.strInfo(NanoComm.INFO_REMEXP_CONNECTED));
    	else Debg.print(NanoComm.strInfo(NanoComm.INFO_REMEXP_DISCONNECTED));
    	Debg.print(NanoComm.strState(currentState));
    	Debg.print(NanoComm.strParam(NanoComm.PARAM_SAMPLESCLEAR + " value=all"));
		for(Sample s: allSamples){
			Debg.print(NanoComm.strParam(NanoComm.PARAM_SAMPLEINFO 
					+ " value=" + s.getSampleID() 
					+ " name=" + s.getSampleName() 
					+ " command=" + s.getLockCommand()));
		}
		Debg.print(NanoComm.strParam(NanoComm.PARAM_SCANRANGE + " value=" + scanRange));
		int smpl;
		if(currentSample == null) smpl = -1;
		else smpl = currentSample.getSampleID();
		
		Debg.print(NanoComm.strParam(NanoComm.PARAM_STAGEPOSITION + " value=" + smpl));
		Debg.print(NanoComm.strParam(NanoComm.PARAM_SCANSTART + " value=" + scanStart));
		Debg.print(NanoComm.strParam(NanoComm.PARAM_REMEXPNAME + " value=" + remExpName));
	}
	

	
	/**
	 * Checks whether the command is known for the remote experiment.
	 * 
	 * @param msg The string containing the command=value pair.
	 * @return True if the command is valid for the remote experiment, else false.
	 */
	protected synchronized boolean isValidRemExpCommand(String msg){
		if(getLockForCommand(msg) != null) return true;
		else return false;
	}

	/**
	 * Searches for the LockHolder associated with the value for the command tag.
	 * 
	 * @param msg The string holding the command=value pair.
	 * @return The LockHolder if the command exists, or null.
	 */
	private synchronized LockHolder getLockForCommand(String msg){
		String cmd = Parser.getValue(msg, NanoComm.COMMAND_CMD);
		if(cmd != null) return commandLocks.get(cmd);
		else return null;
	}
	
	protected synchronized void handleClientRequest(String message, EventSocket sock){
		Debg.print("client event: " + message);
		if(lockHolder == null) executeAndSetLock(message); // if no lock is set we try to execute the command
		else { //if a lock is already set the command might contain an abort message
			String cmd = Parser.getValue(message, NanoComm.COMMAND_CMD);
			String[] abortCmds = lockHolder.getAbortCommands();
			boolean isAbortable = false;
			if(abortCmds != null) for(int i = 0; i < abortCmds.length; i++) {
				if(cmd.equals(abortCmds[i])) isAbortable = true;
			}
			if(isAbortable){
				setLock(lockHolder.takeAbortActions("Overwriting command has been sent"));
			} else sock.put("Please be patient, the remote experiment is still busy");
		}
	}
	
	/**
	 * Receives a client event and searches for the command=value value in order to determine
	 * whether this command is allowed to be passed to the remote experiment and whether a
	 * lock needs to be set until the action is finished and the remote experiment answered
	 * to be ready again.
	 * 
	 * @param msg Has the form 'command=adjustaxis name=x value=500'
	 * @return A lock if the client action requires the remote experiment to be locked until
	 * the action is completed, or null if no lock is needed.
	 */
	private void executeAndSetLock(String msg){
		LockHolder lock = getLockForCommand(msg);
		if(lock != null) {
	    	poolShallSendToRemExp(msg);
			setLock(lock.setLockAction(msg));
		} else setLock(null);
		Debg.print("lock set and executed");
	}

    /**
     * Handles an event coming from the remote experiment. It tests for a 
     * present lock that needs to be released and passes it the message that
     * maybe removes this lock.
     * 
     * @param msg The plain message coming from the remote experiment.
     */
    public synchronized void handleRemExpEvent(String msg){
    	if(lockHolder != null) setLock(lockHolder.resolveLock(msg));
    }
    
    /**
     * This function is used by the commandProcessor of the remote experiment
     * to lock or unlock the access to the remote experiment by the clients.
     * 
     * @param lock The lock that needs to be set, or null if the lock is empty.
     */
    //TODO implement wait/noify on lock for this thread so we only have this thread running when a lock is set.
    private synchronized void setLock(LockHolder lock){
    	if(lock!=lockHolder){
    		/* 
    		 * if the same lock is set we don't update the timeout. this should be improved in the future.
    		 * since it might be possible we want to set the same lock again and renew the timeout.
    		 * This case can't be handled by the current architecture
    		 */
	    	if(lock != null) lockStarted = new Date().getTime();
	    	else lockStarted = 0;
    	}
    	lockHolder = lock;
    }
    
    /**
     * Starts the scan observer that checks whether no stream data is being retrieved
     * from the remote experiment anymore.
     */
    private void startScanObserver(){
        lastStreamData = new Date();
        scanObserver = new ScanStopChecker();
        scanObserver.start("Observer for Scan end");
    }
    
    /**
     * Shuts down the scan observer if no scanning is happening at the moment.
     */
    private void stopScanObserver(){
    	lastStreamData = null;
    	scanObserver.shutDown();
    	scanObserver = null;
    }

    /**
     * Checks the lockHolder for timeout on the lock.
     */
	@Override
	public void doTask() {
		if(lockHolder != null){
			long now = new Date().getTime();
			if(lockStarted + lockHolder.getLockTimeoutInSeconds() * 1000 < now){
				setLock(lockHolder.takeAbortActions("Lock timed out"));
				Debg.print("Lock timed out, took abort actions");
				lockStarted = 0;
			}
		}
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {Debg.err("Failed sleeping");}
	
	}
	
	private class ScanStopChecker extends ThreadHandler{
		@Override
		public void doTask() {
			if(lastStreamData != null && lockHolder != null){
				if(lastStreamData.getTime() + 5000 < new Date().getTime()){
					lockHolder.takeAbortActions("Stream ended broadcasting");
				}
			}
		}
		@Override
		public void shutDown() {
			this.stopThread();
		}
	}
	
	
	private class Sample{
		private String name, command, release;
		private int id, sourcePosX, sourcePosY, deltaX, deltaY;
		
		private Sample(int sid, String sampleName, String cmd, String rel, int tipX, int tipY, int dX, int dY){
			name = sampleName;
			command = cmd;
			release = rel;
			id = sid;
		    sourcePosX = tipX;
		    sourcePosY = tipY;
			deltaX = dX;
			deltaY = dY;
		}
	
		private boolean updateTipPosition(int x, int y){
			if(Math.abs(sourcePosX - (tipPositionX + x)) < deltaX && Math.abs(sourcePosY - (tipPositionY + y)) < deltaY){
				tipPositionX += x;
				tipPositionY += y;
				Debg.print("Changing position to:" + tipPositionX + "/" + tipPositionY);
				return true;
			} else {
				poolShallBroadcast("Tip not moving, position is out of boundaries: " + (tipPositionX + x) + "/" + (tipPositionY + y));
				return false;
			}
		}
		
		private String getLockCommand(){return command;}
		private String getReleaseCommand(){return release;}
		private String getSampleName(){return name;}
		private int getSampleID(){return id;}
	}

//FIXME only allow actions that are not harmful in the current state! at the moment one could move to a custom position while approached!
	/**
	 * The lock for the calibratestage command that moves the robotic stage to its initial position
	 * 
	 * @author Dominic Bosch
	 * @version 1.0 21.10.2011
	 */
	private class CalibrateStageLock extends LockHolder {
		@Override protected long getLockTimeoutInSeconds() {return 120;}
		@Override
		protected synchronized LockHolder setLockAction(String event) {
			setCurrentState(NanoComm.STATE_STAGEMOVING);
			poolShallBroadcast("Calibrating the stage, please be patient");
			return this;
		}
		@Override protected String releaseLockCommand() {
			Debg.print("was asked to release lock");
			return "Instrument Calibrated";
		}
		protected LockHolder releaseThisLock() {
			setState();
			poolShallBroadcast("Calibration of stage successful");
			Debg.print("Lock has been released for stage calibration");
			return null;
		}
		@Override protected String[] getAbortCommands() {return null;}
		@Override protected LockHolder takeAbortActions(String reason) {
			setState();
			poolShallBroadcast("Calibration of stage aborted due to reason: " + reason);
			Debg.print("Lock has been aborted for stage calibration due to reason: " + reason);
			return null;
		}
		private void setState(){
			currentSample = null;
			poolShallBroadcast(NanoComm.strParam(NanoComm.PARAM_STAGEPOSITION + " value=-1"));
			setCurrentState(NanoComm.STATE_STAGECALIBRATED);
		}
	}
	
	/**
	 * The lock for the goto# commands that move the robotic stage.
	 * 
	 * @author Dominic Bosch
	 * @version 1.0 21.10.2011
	 */
	private class GotoLock extends LockHolder {
		Sample sample;
		GotoLock(Sample smpl){sample = smpl;}
		@Override protected long getLockTimeoutInSeconds() {return 60;}
		@Override
		protected synchronized LockHolder setLockAction(String event) {
			setCurrentState(NanoComm.STATE_STAGEMOVING);
			poolShallBroadcast("Moving to " + sample.getSampleName() + ", please be patient");
			return this;
		}
		@Override protected String releaseLockCommand() {return sample.getReleaseCommand();}
		@Override
		protected LockHolder releaseThisLock() {
			currentSample = sample;
		    tipPositionX = sample.sourcePosX;
		    tipPositionY = sample.sourcePosY;
			sendAndSetStageReadyState();
			poolShallBroadcast("Finished moving to " + sample.getSampleName());
			Debg.print("Lock has been released for movement to " + sample.getSampleName());
			return null;
		}
		@Override protected String[] getAbortCommands() {return null;}
		@Override
		protected LockHolder takeAbortActions(String reason) {
			poolShallBroadcast("Aborted moving to " + sample.getSampleName() + " due to reason: " + reason);
			Debg.print("Lock has been released for movement to " + sample.getSampleName() + " due to reason: " + reason);
			poolShallSendToRemExp(sample.command);
			return setLockAction(sample.command); // TODO check whether the experiment moves back into initial position after abort and whether this command is consistent
		}
		private void sendAndSetStageReadyState(){
			poolShallBroadcast(NanoComm.strParam(NanoComm.PARAM_STAGEPOSITION + " value=" + sample.getSampleID()));
			setCurrentState(NanoComm.STATE_STAGEREADY);
		}
	}

	/**
	 * The lock for the autoapproach command that moves the tip onto the sample.
	 * 
	 * @author Dominic Bosch
	 * @version 1.0 21.10.2011
	 */
	private class AutoApproachLock extends LockHolder {
		@Override protected long getLockTimeoutInSeconds() {return 300;}
		@Override
		protected synchronized LockHolder setLockAction(String event) {
			setCurrentState(NanoComm.STATE_APPROACHING);
			poolShallBroadcast("Approaching the sample, please be patient");
			return this;
		}
		@Override protected String releaseLockCommand() {return "StatusApproached";}
		@Override protected LockHolder releaseThisLock() {
			setCurrentState(NanoComm.STATE_APPROACHED);
			poolShallBroadcast(NanoComm.strParam(NanoComm.PARAM_SCANSTART + " value=0"));
			poolShallBroadcast("Tip has successfully approached the sample!");
			Debg.print("Lock has been released for auto approach");
			return null;
		}
		@Override protected String[] getAbortCommands() {return new String[]{NanoComm.CMD_STOPAPPROACH};}

		@Override
		protected LockHolder takeAbortActions(String reason) {
			poolShallSendToRemExp(NanoComm.strCmd(NanoComm.CMD_STOPAPPROACH));
			setCurrentState(NanoComm.STATE_STAGEREADY);
			poolShallBroadcast("Tip approach has been aborted due to the reason: " + reason);
			Debg.print("Lock has been aborted for auto approach due to the reason: " + reason);
			//TODO if approach is aborted in the exact moment when the tip approaches, the lock is removed and stage is ready
			// but the stage isn't ready since the tip is approached... the command below should fix this issue but still needs to be checked$	
			poolShallSendToRemExp(NanoComm.strCmd(NanoComm.CMD_WITHDRAW));
			return null;
		}
	}

	/**
	 * The lock for the scanning command that initiates the scan of the sample.
	 * 
	 * @author Dominic Bosch
	 * @version 1.0 21.10.2011
	 */
	private class ScanningLock extends LockHolder {
		@Override protected long getLockTimeoutInSeconds() {return 10*60;}
		@Override
		protected synchronized LockHolder setLockAction(String event) {
			setCurrentState(NanoComm.STATE_SCANNING);
			long now = new Date().getTime() / 1000;
			setScanStart(now);
			startScanObserver();
			poolShallBroadcast(NanoComm.strParam(NanoComm.PARAM_SCANSTART + " value=" + now));
			poolShallBroadcast("Started scanning the sample");
			return this;
		}
		@Override protected String releaseLockCommand() {return "Scan finished";}
		@Override
		protected LockHolder releaseThisLock() {
			setCurrentState(NanoComm.STATE_APPROACHED);
			setScanStart(0);
			stopScanObserver();
			poolShallBroadcast("Finished scanning the sample!");
			Debg.print("Lock has been released for scanning");
			return null;
		}
		@Override protected synchronized String[] getAbortCommands() {return new String[]{NanoComm.CMD_STOP};}

		@Override
		protected LockHolder takeAbortActions(String reason) {
			poolShallSendToRemExp(NanoComm.strCmd(NanoComm.CMD_STOP));
			setCurrentState(NanoComm.STATE_APPROACHED);
			setScanStart(0);
			stopScanObserver();
			poolShallBroadcast(NanoComm.strParam(NanoComm.PARAM_SCANSTART + " value=0"));
			poolShallBroadcast("Scanning aborted du to the reason: " + reason);
			return null;
		}
	}
	
	/**
	 * The lock for the withdraw command that removes the tip from the sample.
	 * 
	 * @author Dominic Bosch
	 * @version 1.0 21.10.2011
	 */
	private class WithdrawLock extends LockHolder {
		@Override protected long getLockTimeoutInSeconds() {return 20;}
		@Override
		protected synchronized LockHolder setLockAction(String event) {
			setCurrentState(NanoComm.STATE_WITHDRAWING);
			poolShallBroadcast("Withdrawing from the sample, please be patient");
			return this;
		}
		@Override
		protected String releaseLockCommand() {return "Withdrawn";}
		@Override
		protected LockHolder releaseThisLock() {
			setCurrentState(NanoComm.STATE_STAGEREADY);
			poolShallBroadcast("Tip has successfully been withdrawn!");
			Debg.print("Lock has been released for withdraw");
			return null;
		}
		@Override protected String[] getAbortCommands() {return null;}
		@Override
		protected LockHolder takeAbortActions(String reason) {
			setCurrentState(NanoComm.STATE_STAGEREADY);
			poolShallBroadcast("Withdraw has been aborted due to reason: " + reason);
			Debg.print("Withdraw has been aborted due to reason: " + reason);
			return null;
		}
	}
	
	/**
	 * The lock for the withdraw command that removes the tip from the sample.
	 * 
	 * @author Dominic Bosch
	 * @version 1.0 21.10.2011
	 */
	private class ScanRangeLock extends LockHolder {
		@Override protected long getLockTimeoutInSeconds() {return 20;}
		@Override
		protected synchronized LockHolder setLockAction(String event) {
			String val = Parser.getValue(event, "value");
			if(val != null) {
				scanRange = Integer.parseInt(val);
				poolShallBroadcast(NanoComm.strParam(NanoComm.PARAM_SCANRANGE + " value=" + val));
				poolShallBroadcast("Scan range set to " + val);
			}
			return null;
		}
		@Override protected String releaseLockCommand() {return null;}
		@Override protected LockHolder releaseThisLock() {return null;}
		@Override protected String[] getAbortCommands() {return null;}
		@Override protected LockHolder takeAbortActions(String reason) {return null;}
	}

	/**
	 * The lock for the movetip command that moves the tip on the sample into one direction.
	 * 
	 * @author Dominic Bosch
	 * @version 1.0 21.10.2011
	 */
	private class MoveTipLock extends LockHolder {
		@Override protected long getLockTimeoutInSeconds() {return 20;}
		@Override
		protected synchronized LockHolder setLockAction(String event) {
			boolean validMove = false;
			String val = Parser.getValue(event, "value");
			if(val != null) {
				int steps = 10; //TODO check correct scaling on move steps
				String axis = " ";
				String direction = "";
				//TODO check correct direction
				//FIXME setpos command in labview
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
				if(axis.equals("x")) validMove = currentSample.updateTipPosition(steps, 0);
				else validMove = currentSample.updateTipPosition(0, steps);
				if(validMove) {
					setCurrentState(NanoComm.STATE_STAGEMOVING);
					poolShallBroadcast("Moving on sample in " + direction + " " + axis + " direction");
					poolShallSendToRemExp(NanoComm.strCmd("adjustaxis name=" + axis + " value=" + steps));
					return this;
				}
			}
			return null;
		}
		@Override protected String releaseLockCommand() {return "Customposition : ";}
		@Override protected LockHolder releaseThisLock() {
			setCurrentState(NanoComm.STATE_STAGEREADY);
			poolShallBroadcast("Reached custom position");
			return null;
		}
		@Override protected String[] getAbortCommands() {return null;}
		@Override
		protected LockHolder takeAbortActions(String reason) {
			setCurrentState(NanoComm.STATE_STAGEREADY);
			poolShallBroadcast("Moving on sample has been aborted due to the reason: " + reason);
			Debg.print("Moving on sample has been aborted due to the reason: " + reason);
			return null;
		}
	}

	private class CamAngleLock extends LockHolder {
		@Override protected long getLockTimeoutInSeconds() {return 5;}
		@Override protected LockHolder setLockAction(String event) {
			switch(camAngle++){
				case 0:
					pool.sendToRemExp(NanoComm.strCmd("videoa"));
					break;
				case 1:
				default:
					pool.sendToRemExp(NanoComm.strCmd("videob"));
					camAngle = 0;
			}
			return this;
		}
		@Override protected String releaseLockCommand() {return "StatusPrompt";}
		@Override protected LockHolder releaseThisLock() {
			Debg.print("released");
			return null;
		}
		@Override protected String[] getAbortCommands() {return null;}
		@Override protected LockHolder takeAbortActions(String reason) {return null;}
	}
	
	/**
	 * The NoLock class that is used to determine whether a command is allowed
	 * but doesn't need a lock.
	 * 
	 * @author Dominic Bosch
	 * @version 1.0 21.10.2011
	 */
	private class NoLock extends LockHolder {
		@Override protected long getLockTimeoutInSeconds() {return 0;}
		@Override protected LockHolder setLockAction(String event) {return null;}
		@Override protected String releaseLockCommand() {return null;}
		@Override protected LockHolder releaseThisLock() {return null;}
		@Override protected String[] getAbortCommands() {return null;}
		@Override protected LockHolder takeAbortActions(String reason) {return null;}
	}
	
	/**
	 * Clearing the commandLocks hash table and stopping the thread. 
	 */
	@Override
	public void shutDown() {
		stopThread();
		if(scanObserver!=null) scanObserver.shutDown();
	    lockStarted = -1;
	    lockHolder = null;
		commandLocks.clear();
		commandLocks = null;
		allSamples.clear();
		allSamples = null;
		pool = null;
	    lastStreamData = null;
	}

}