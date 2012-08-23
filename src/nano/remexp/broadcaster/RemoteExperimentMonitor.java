package nano.remexp.broadcaster;

import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import nano.debugger.Debg;
import nano.remexp.Parser;
import nano.remexp.ThreadHandler;
import nano.remexp.broadcaster.locks.AutoApproachLock;
import nano.remexp.broadcaster.locks.CalibrateStageLock;
import nano.remexp.broadcaster.locks.CamAngleLock;
import nano.remexp.broadcaster.locks.GotoLock;
import nano.remexp.broadcaster.locks.LockHolder;
import nano.remexp.broadcaster.locks.MoveTipLock;
import nano.remexp.broadcaster.locks.NoLock;
import nano.remexp.broadcaster.locks.ScanRangeLock;
import nano.remexp.broadcaster.locks.ScanningLock;
import nano.remexp.broadcaster.locks.WithdrawLock;
import nano.remexp.net.NanoComm;
import nano.remexp.net.EventSocket;

/**
 * This class is a monitor state of the remote experiment. It handles incomming client requests
 * and checks whether they are valid. It establishes locks according to the current state of
 * the remote experiment and releases them either because of information that is sent from the
 * remote experiment or the clients, or if the lock times out.
 * 
 * @author Dominic Bosch
 * @version 1.1 23.08.2012
 */
public class RemoteExperimentMonitor extends ThreadHandler {

	private FilteringPool pool;
    private LockHolder lockHolder;
    private Hashtable<String, LockHolder> commandLocks;
    ConcurrentLinkedQueue qClientRequests;
    private int scanRange;
    private int currentState;
    private long scanStart;
    private Date lastStreamData;
    private Sample currentSample;
    private Vector<Sample> allSamples;
    private String remExpName;
    private long lockStarted;
	private int tipPositionX, tipPositionY;
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
		qClientRequests = new ConcurrentLinkedQueue();
		initParser();
		allSamples = new Vector<Sample>();
		initMonitorVars();
        setLock(null);
	    lastStreamData = null;
	    scanStart = -1;
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
		//TODO implement modular approach which reads allowed commands from the configuration file and assigns it to user roles(privileges)
		LockHolder notLocking = new NoLock(this);
		commandLocks = new Hashtable<String, LockHolder>();
		commandLocks.put(NanoComm.CMD_CALIBRATESTAGE, new CalibrateStageLock(this));
		commandLocks.put(NanoComm.CMD_AUTOAPPROACH, new AutoApproachLock(this));
		commandLocks.put(NanoComm.CMD_START, new ScanningLock(this));
		commandLocks.put(NanoComm.CMD_WITHDRAW, new WithdrawLock(this));
		/*TODO check whether this is really needed since this command stops the scan, 
		 * thus is an abort command for the scan lock. but this is also a command that
		 * should always be passed through if the appropriate privileges are present.
		 * Thus I suggest we implement another command possibility: lock independent commands
		 * On the other hand the monitor reflects the state of the remote experiment.
		 * in the worst case of the current architecture the client would have to wait for a 
		 * lock time out until he is able to use the notLocking commands.
		 */
		commandLocks.put(NanoComm.CMD_STOP, notLocking);
		//TODO check whether this is really needed since this command stops the approach, thus is an abort command for the approach lock
		commandLocks.put(NanoComm.CMD_STOPAPPROACH, notLocking);
		commandLocks.put(NanoComm.CMD_SCANRANGE, new ScanRangeLock(this));
		commandLocks.put(NanoComm.CMD_MOVETIP, new MoveTipLock(this));
		commandLocks.put(NanoComm.CMD_CAMANGLE, new CamAngleLock(this));
	}
	
	/**
	 * Initializes all monitoring variables
	 */
	private void initMonitorVars(){
		currentState = NanoComm.STATE_UNAVAILABLE;
		scanStart = 0;
	    scanRange = 10;
	    currentSample = null;
		tipPositionX = 0;
		tipPositionY = 0;
		isRemExpConnected = false;
	}
	
	/**
	 * Initializes the remote experiment by aborting eventual locks and setting the
	 * remote experiment into a calibrated stage.
	 */
	protected void initRemExp(){
		String msg;
		initMonitorVars();
		if(lockHolder != null) lockHolder.takeAbortActions("RemExp RESET");
		msg = NanoComm.strCmd(NanoComm.CMD_CALIBRATESTAGE);
		LockHolder lock = getLockForCommand(msg);
    	sendToRemExp(msg);
		setLock(lock.setLockAction(msg));
	}

	/**
	 * Sets and stores the name of the remote experiment
	 * @param name	the name of the remote experiment
	 */
	protected void setRemExpName(String name){
		remExpName = name;
		broadcastToClients(NanoComm.strParam(NanoComm.PARAM_REMEXPNAME + " value=" + remExpName));
	}

	/**
	 * Stores sample information in a GotoLock and makes it ready to be called by the client.
	 * 
	 * @param sid			the sample id
	 * @param sampleName	the sample name
	 * @param cmd			the command to the remote experiment moving to the sample
	 * @param rel			the command sent by the remote experiment, indicating the movement to the sample succeeded
	 * @param tipX			the initial x position of the tip at this sample
	 * @param tipY			the initial y position of the tip at this sample 
	 * @param dX			the maximum distance allowed away from the initial tip position in x direction
	 * @param dY			the maximum distance allowed away from the initial tip position in y direction
	 */
	protected void addSample(int sid, String sampleName, String cmd, String rel, int tipX, int tipY, int dX, int dY){
		Sample s = new Sample(this, sid, sampleName, cmd, rel, tipX, tipY, dX, dY);
		allSamples.add(s);
		GotoLock gl = new GotoLock(this);
		gl.init(s);
		commandLocks.put(s.getLockCommand(), gl);
	}

	/**
	 * Sets and broadcasts information about the connectivity and state of the remote experiment.
	 * 
	 * @param isConnected	true if connected, else false
	 */
    protected void setRemExpConnected(boolean isConnected){
    	Debg.print("Remote Experiment connected: " + isConnected);
    	isRemExpConnected = isConnected;
    	if(isConnected){
    		currentState = NanoComm.STATE_STAGECALIBRATED;
    		broadcastToClients(NanoComm.strState(currentState));
    		broadcastToClients(NanoComm.strInfo(NanoComm.INFO_REMEXP_CONNECTED));
    	} else {
    		initMonitorVars();
    		currentState = NanoComm.STATE_UNAVAILABLE;
    		broadcastToClients(NanoComm.strState(currentState));
    	}
    }

    /**
     * Sets the scan start time
     * 
     * @param tme	the time the measurement started, null if not measuring
     */
	public void setScanStart(long tme){
		scanStart = tme;
	}

	/**
	 * Returns the time stamp of the scan start.
	 * 
	 * @return the time in milliseconds when the measurment started
	 */
	public long getScanStart(){
		return scanStart;
	}

	/**
	 * Sets the time stamp of when the last stream data has been received.
	 * 
	 * @param date	the date time stamp
	 */
	public void setStreamTimeStamp(Date date) {
		lastStreamData = date;
	}

	/**
	 * Returns the time stamp of when the last stream data has been received.
	 * 
	 * @return	the time stamp in Date format
	 */
	public Date getStreamTimeStamp() {
		return lastStreamData;	
	}

	/**
	 * returns the current sample the remote experiment is at.
	 * 
	 * @return the sample where the remote experiment resides, null if it's not at a sample position.
	 */
	public Sample getSample() {
		return currentSample;
	}

	/**
	 * Set the sample which the remote experiment moved to.
	 * 
	 * @param smpl	the sample where the remote experiment moved to
	 */
	public void setSample(Sample smpl) {
		currentSample = smpl;
	}

	/**
	 * Sets the current scan range of the remote experiment.
	 * 
	 * @param scanR	the user defined scan range
	 */
	public void setScanRange(int scanR) {
		scanRange = scanR;
	}
	
	/**
	 * Passes a message to the pool which will then broadcast it to all connected clients.
	 * 
	 * @param msg	the message to be broadcasted
	 */
	public void broadcastToClients(String msg){
    	if(pool != null){
    		pool.broadcast(msg);
    	}
	}
	
	/**
	 * Passes a message to the pool which will then broadcast it to all connected clients.
	 * 
	 * @param msg	the message to be broadcasted
	 */
	public void broadcastInfoToClients(String msg){
    	if(pool != null){
    		pool.broadcast(NanoComm.strInfo(NanoComm.INFO_MSG_TO_CLIENT) + " " + msg);
    	}
	}
	
	/**
	 * Passes a message to the pool which will apss it further to the remote experiment.
	 * 
	 * @param msg
	 */
	public void sendToRemExp(String msg){
		Debg.print("sending to remexp: " + msg);
    	if(pool != null){
    		pool.sendToRemExp(msg);
    	}
	}

	/**
	 * Returns the tip position of the remote experiment in X direction.
	 * 
	 * @return	the tip position in x direction
	 */
	public int getTipPositionX() {return tipPositionX;}

	/**
	 * Returns the tip position of the remote experiment in Y direction.
	 * 
	 * @return	the tip position in y direction
	 */
	public int getTipPositionY() {return tipPositionY;}
	
	/**
	 * Sets the tip position of the remote experiment in X and Y direction.
	 * 
	 * @param x		the tip position in x direction
	 * @param y		the tip position in y direction
	 */
	public void setTipPosition(int x, int y) {
		tipPositionX = x;
		tipPositionY = y;
	}

	/**
	 * This method is used to set and broadcast the actual state of the remote experiment.
	 * 
	 * @param state The state the remote experiment monitor is in now.
	 */
	public void setCurrentState(int state){
		currentState = state;
		broadcastToClients(NanoComm.strState(currentState));
		Debg.print("Setting state " + state);
	}
	
	/**
	 * Informs the new client about the current state of the remote experiment. Sets the correct
	 * parameters in the gui when a new client connects or changes his privileges.
	 * 
	 * @param sock The socket that needs to be informed.
	 */
	protected void sendCurrentStateTo(EventSocket sock){
		sock.put(NanoComm.strPriv(sock.getPrivilege()));
		try {Thread.sleep(2000);} catch (InterruptedException e) {}
    	if(isRemExpConnected) sock.put(NanoComm.strInfo(NanoComm.INFO_REMEXP_CONNECTED));
    	else sock.put(NanoComm.strInfo(NanoComm.INFO_REMEXP_DISCONNECTED));
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
	    sock.sendClientInfo("Welcome to the RAFM of the University of Basel!");
	}
	
	
	/**
	 * Prints the current state of the remote experiment.
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
	public synchronized LockHolder getLockForCommand(String msg){
		String cmd = Parser.getValue(msg, NanoComm.COMMAND_CMD);
		if(cmd != null) return commandLocks.get(cmd);
		else return null;
	}

	/**
	 * Receives a client event and searches for the command=value value in order to determine
	 * whether this command is allowed to be passed to the remote experiment and whether a
	 * lock needs to be set until the action is finished and the remote experiment answered
	 * to be ready again.
	 * 
	 * @param message	Has the form 'command=adjustaxis name=x value=500'
	 * @param sock		the client socket that sent the message
	 */
	//TODO we should just store the client requests in a queue and let them be processed y the thread running on this object.
	//it will then check periodically for an outdated lock and for client commands to be processed.
	//This suits the desired architecture more than risking in between states if several clients access the locking mechanism.
	protected synchronized void addClientRequest(String message, EventSocket sock){
		qClientRequests.add(new QueueEntry(sock, message));
	}

	private void processNextClientRequest(){
		QueueEntry qe = (QueueEntry) qClientRequests.poll();
		if(qe != null){
			EventSocket sock = qe.sock;
			String message = qe.message;
			boolean isAllowed = false;
			LockHolder lock = getLockForCommand(message);
			if(lock != null) {
				// if the lock exists check whether the user is allowed to use this command
				int[] privs = lock.getAllowedPrivileges();
				for(int priv: privs) if(priv == sock.getPrivilege()) isAllowed = true;
				if(isAllowed){
					if(lockHolder == null){ // if no lock is set we try to execute the command
						setLock(lock.setLockAction(message));
						Debg.print("lock set and executed");
					} else { //if a lock is already set the command might contain an abort message
						String cmd = Parser.getValue(message, NanoComm.COMMAND_CMD);
						String[] abortCmds = lockHolder.getAbortCommands();
						boolean isAbortable = false;
						if(abortCmds != null) for(int i = 0; i < abortCmds.length; i++) {
							if(cmd.equals(abortCmds[i])) isAbortable = true;
						}
						if(isAbortable){
							setLock(lockHolder.takeAbortActions("Overwriting command has been sent"));
						} else sock.sendClientInfo("Please be patient, the remote experiment is still busy");
					}
				} else sock.sendClientInfo("Sorry, you are not allowed to use this command!");
			} else Debg.print("No lock found for: " + message);
		}
		if(qClientRequests.size() > 10) {
			Debg.err("I have been flooded by " + qClientRequests.size() + " client requests! I dropped everything except for the oldest one");
			qe = (QueueEntry) qClientRequests.poll();
			qClientRequests.clear();
			qClientRequests.add(qe);
		}
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
    public synchronized void setLock(LockHolder lock){
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
		processNextClientRequest();
		try {Thread.sleep(500);} catch (InterruptedException e) {Debg.err("Failed sleeping");}
	
	}
	
	private class QueueEntry{
		EventSocket sock;
		String message;
		private QueueEntry(EventSocket s, String m){
			sock = s;
			message = m;
		}
	}
//FIXME only allow actions that are not harmful in the current state! at the moment one could move to a custom position while approached!

	/**
	 * Clearing the commandLocks hash table and stopping the thread. 
	 */
	@Override
	public void shutDown() {
		stopThread();
	    lockStarted = -1;
	    lastStreamData = null;
	    lockHolder = null;
		commandLocks.clear();
		commandLocks = null;
		allSamples.clear();
		allSamples = null;
		pool = null;
	}
}