package nano.remexp;

import nano.debugger.Debg;

/**
 * 
 */
public abstract class ThreadHandler implements Runnable{
	private Thread thisThread = null;
	private String threadName = "";
	private boolean isRunning = false;
	private boolean reachedEnd = false;

	public void start(String name){
		isRunning = true;
		threadName = name;
		thisThread = new Thread(this, name);
	    thisThread.start();
	    Debg.print(" [+]  new Thread: " + name);
	    Debg.addThread(this);
	}

	public void restart(){
		if(!isRunning){
			while(!reachedEnd){
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					Debg.err("Sleep was interrupted for '" + threadName + "'!");
				}
			}
			start(threadName);
		    Debg.print("  ThreadHandler restart for " + threadName);
		}
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
	 * 
	 * @return true if the thread is still allowed to run, else false. Normally this only
	 * returns false if the stopThread method has been called but the thread is locked in the
	 * doTask method.  
	 */
	public boolean isAliveThread(){
		return isRunning;
	}

	public boolean didExitRunMethod(){
		return reachedEnd;
	}

	public String getThreadName(){
		return threadName;
	}
	
	/**
	 * The abstract method that is being repeated during lifetime of the thread.
	 */
	public abstract void doTask();
	
	/**
	 * Important! every overwriting method should call stopThread in order to be
	 * sure the thread is being shut down! or otherwise set thisThread=null
	 * Else the doTask method will be called over and over again.
	 */
	public abstract void shutDown();
	
}
