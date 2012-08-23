package nano.remexp;

import nano.debugger.Debg;

/**
 * The ThreadHandler wrapper lets us control and observe all threads.
 * It gives them also a certain standard behaviour.
 * 
 * @author Dominic Bosch
 * @version 1.1 23.08.2012
 */
public abstract class ThreadHandler implements Runnable{
	private Thread thisThread = null;
	private String threadName = "";
	private boolean isRunning = false;
	private boolean reachedEnd = false;

	/**
	 * Calling this method will start a thread that runs for this object.
	 * 
	 * @param name the name of the thread that helps to determine it later.
	 */
	public void start(String name){
		isRunning = true;
		threadName = name;
		thisThread = new Thread(this, name);
	    thisThread.start();
	    Debg.print(" [+]  new Thread: " + name);
	    Debg.addThread(this);
	}
	
	/**
	 * The run method of the threads. They call the abstract method doTask while the
	 * thread is supposed to be running. After stopThread is called on this object
	 * the while loop is exited and the abstract method shutDown is called.
	 * 
	 * Attention: the object might be locked in the doTask method and thus won't exit.
	 * Thus it might be necessary to call the shutDown method directly on the object.
	 */
	@Override
	public void run() {
		reachedEnd = false;
		while(isRunning){
			doTask();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				Debg.err("Sleep was interrupted for '" + threadName + "'!");
			}
		}
		Debg.print(" [X]  removed Thread '" + threadName + "'");
		Debg.removeThread(this);
		reachedEnd = true;
	}

	/**
	 * This method stops the thread softly, meaning it will not continue to run after the
	 * next time the doTask method is completed.
	 */
	public void stopThread(){
		isRunning = false;
	}
	
	/**
	 * Checks whether this object has received the stopThread command already.
	 * Normally this only returns false if the stopThread method has been called.
	 * It is possible the thread has been stopped but is still in or locked in the
	 * doTask method. Use the @see didExitRunMethod function to check whether the thread
	 * really stopped by now.
	 *  
	 * @return 		true if the thread is still ALLOWED to run, else false.  
	 */
	public boolean isAliveThread(){
		return isRunning;
	}

	/**
	 * Returns information about whether the thread ended the run method and thus died.
	 * 
	 * @return true if the thread dies, else false.
	 */
	public boolean didExitRunMethod(){
		return reachedEnd;
	}

	/**
	 * Returns the name of this thread.
	 * 
	 * @return the name of this thread
	 */
	public String getThreadName(){
		return threadName;
	}
	
	/**
	 * The abstract method that is being repeated during lifetime of the thread.
	 */
	public abstract void doTask();
	
	/**
	 * Important! every implementation of the shutDown method has to call stopThread in order to be
	 * sure the thread is being shut down! (or otherwise set thisThread=null)
	 * Else the doTask method will be called over and over again and the thread won't die.
	 */
	public abstract void shutDown();
	
}
