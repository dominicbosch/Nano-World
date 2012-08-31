package nano.remexp.broadcaster;

import nano.debugger.Debg;

/**
 * This class reflects the samples that can be approached by the remote experiment.
 * 
 * @author Dominic Bosch
 * @version 1.1 23.08.2012
 */
public class Sample{
	private RemoteExperimentMonitor monitor;
	private String name, command, release;
	private int id, sourcePosX, sourcePosY, deltaX, deltaY;
	
	/**
	 * Instantiates an object reflecting a sample.
	 * 
	 * @param mon			the link to the remote experiment monitor for function calls
	 * @param sid			sample ID
	 * @param sampleName	sample name
	 * @param cmd			the command used to go to this sample
	 * @param rel			the command the remote experiment sends after finished moving to this sample 
	 * @param tipX			the initial tip position in x direction
	 * @param tipY			the initial tip position in y direction
	 * @param dX			the allowed movement delta in x direction
	 * @param dY			the allowed movement delta in y direction
	 */
	public Sample(RemoteExperimentMonitor mon, int sid, String sampleName, String cmd, String rel, int tipX, int tipY, int dX, int dY){
		monitor = mon;
		name = sampleName;
		command = cmd;
		release = rel;
		id = sid;
	    sourcePosX = tipX;
	    sourcePosY = tipY;
		deltaX = dX;
		deltaY = dY;
	}

	/**
	 * Requests the actual tip position and calculates a new position 
	 * by taking the allowed delta into account.
	 * 
	 * @param x		desired number of steps in x direction
	 * @param y		desired number of steps in y direction
	 * @return		true if movement is allowed, else false
	 */
	public boolean updateTipPosition(int x, int y){
		int tipX = monitor.getTipPositionX();
		int tipY = monitor.getTipPositionY();
		if(Math.abs(sourcePosX - (tipX + x)) < deltaX && Math.abs(sourcePosY - (tipY + y)) < deltaY){
			tipX += x;
			tipY += y;
			monitor.setTipPosition(tipX, tipY);
			Debg.print("Changing position to:" + tipX + "/" + tipY);
			return true;
		} else {
			monitor.broadcastInfoToClients("Tip not moving, position is out of boundaries: " + (tipX + x) + "/" + (tipY + y));
			return false;
		}
	}
	
	/**
	 * Returns the command that activates this lock.
	 * 
	 * @return	the command string
	 */
	public String getLockCommand(){return command;}
	
	/**
	 * Returns the command sent by the remote experiment that releases this lock.
	 * 	
	 * @return	the command coming from the remote experiment that releases this lock
	 */
	public String getReleaseCommand(){return release;}

	/**
	 * Returns the sample name.
	 * 
	 * @return	the name of this sample
	 */
	public String getSampleName(){return name;}
	
	/**
	 * Returns the sample ID.
	 * @return	the sample ID
	 */
	public int getSampleID(){return id;}
	
	/**
	 * Returns the initial tip position in x direction.
	 * 
	 * @return	the initial tip position in x direction on this sample
	 */
	public int getPosX(){return sourcePosX;}

	/**
	 * Returns the initial tip position in y direction.
	 * 
	 * @return	the initial tip position in y direction on this sample
	 */
	public int getPosY(){return sourcePosY;}
}
