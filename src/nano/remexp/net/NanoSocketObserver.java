package nano.remexp.net;

import java.util.Date;
import java.util.Vector;

import nano.debugger.Debg;
import nano.remexp.ThreadHandler;

/**
 * 
 * 
 * @author Dominic Bosch
 * @version 1.1 23.08.2012
 */
public class NanoSocketObserver extends ThreadHandler{
	private Vector<NanoSocket> tmpRemove;
	private Vector<NanoSocket> listSocks;
	private long timeAllowedToLastSignOfLife;
	private boolean doShutdown;

	/**
	 * Calls the constructor NanoSocketObserver(10000).
	 */
	public NanoSocketObserver(){
		this(10000);
	}
	
	/**
	 * NanoSocketObserver is a class that enables you to check your NanoSockets states.
	 * If a socket is flagged alive and the last time stamp of received data is older than
	 * the defined number of milliseconds, the NanoSocket receives the shutDown command.
	 * 
	 * @param timeDelayAllowed the time in milliseconds a socket is allowed not to respond.
	 */
	public NanoSocketObserver(long timeDelayAllowed){
		tmpRemove = new Vector<NanoSocket>();
		listSocks = new Vector<NanoSocket>();
		timeAllowedToLastSignOfLife = timeDelayAllowed;
		doShutdown = false;
	}

	/**
	 * Inform this observer about a new socket that has been created.
	 * This socket will be added to the list of observed sockets in the beginning of the doTask method.
	 * 
	 * @param ns the new socket.
	 */
	public void addSocket(NanoSocket ns){
		if(listSocks!=null) listSocks.add(ns);
	}

	/**
	 * Move a socket into the temporary list of sockets that need to be removed from this observer.
	 * the list of observed sockets will be updated in the beginning of the doTask method.
	 * 
	 * @param ns the socket that is not used anymore.
	 */
	public void removeSocket(NanoSocket ns){
		if(listSocks!=null) listSocks.remove(ns);
	}

	/**
	 * The NanoSocketObserver's task is to check whether a valid connection 
	 * valid means it should be busy at the moment. This counts for the event sockets all the time
	 * and for the stream sockets when the remote experiment is scanning.
	 */
	@Override
	public void doTask() {
		long now = new Date().getTime(); // we are conservative about the time since the socket answered last, thus initializing 'now' here already
		synchronized (listSocks){
			for(NanoSocket sock: listSocks) if(sock.isActiveSocket()) sock.ping();
		}
		try {Thread.sleep(2000);} catch (InterruptedException e) {Debg.err("Couldn't sleep...");}
		for(NanoSocket sock: listSocks){
			if(now - sock.getLastSignOfLife() > timeAllowedToLastSignOfLife && sock.isActiveSocket()){
				tmpRemove.add(sock);
			}
		}
		for(NanoSocket sock: tmpRemove){
			Debg.print("found socket that didn't answer for more than 10 seconds: " + sock.getRemoteID());
			sock.shutDown();
		}
		tmpRemove.clear();
		try {Thread.sleep(2000);} catch (InterruptedException e) {Debg.err("Couldn't sleep...");}
		if(doShutdown){
			Debg.print("Shutting down NanoSocketObserver");
			if(listSocks!=null){
				Vector<NanoSocket> tmp = new Vector<NanoSocket>();
				for(NanoSocket sock: listSocks) tmp.add(sock);
				listSocks.clear();
				listSocks = null;
				for(NanoSocket sock: tmp) sock.shutDown();
				tmp.clear();
				tmp = null;
			}
		}
	}

	@Override
	public void shutDown() {
		stopThread();
		doShutdown = true;
	}
	
}