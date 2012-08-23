package nano.debugger;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import nano.remexp.ThreadHandler;

/**
 * This class gives tools to maintain a log file and also keep track of running htreads.
 * 
 * @author Dominic Bosch
 * @version 1.1 23.08.2012
 */
public class LogFile {
	final static int FILE_SIZE = 1*1024*1024;
	final static int NUM_LOGS = 500;
	private Vector<ThreadHandler> threadList;
	public Logger log;
	FileHandler fh;

	/**
	 * Invokes an object that passes information to a log file. This function also adds 
	 * a simple format to the log file output.
	 * 
	 * @param logFileName	The extension of the log file. It is added to a date string and a
	 * 						sequential number that is used to increase if the log file grows too big. 
	 */
	public LogFile(String logFileName){
		threadList = new Vector<ThreadHandler>();
		try {
			fh = new FileHandler("log/" +  new SimpleDateFormat("yyyy_MM_dd_HHmm").format(new Date()) + "_%g_" + logFileName, FILE_SIZE, NUM_LOGS, true);
			fh.setFormatter(new Formatter() {
				public String format(LogRecord rec) {
					StringBuffer buf = new StringBuffer(1000);
					buf.append(formatMessage(rec));
					buf.append('\n');
					return buf.toString();
				}
			});
			log = Logger.getLogger("LogFilesegsdv");
			log.addHandler(fh);
		} catch (IOException e) {}
		Logger rootLogger = Logger.getLogger("");
		Handler[] handlers = rootLogger.getHandlers();
		if (handlers[0] instanceof ConsoleHandler) {
		    rootLogger.removeHandler(handlers[0]);
		}
	}
	
	/**
	 * Places information in the log file.
	 * 
	 * @param msg the string to be stored in the log file.
	 */
	public void addToLog(String msg){
		if(log!=null) log.info(msg);
	}
	
	/**
	 * Adds a string to the list of observed @see ThreadHandler.
	 * 
	 * @param th the thread to be observed
	 */
	public void addThread(ThreadHandler th){
		if(threadList!=null){
			threadList.add(th);
			printThreadNames();
		}
	}
	
	/**
	 * Removes a thread from the list of observed @see ThreadHandler.
	 * @param th the thread to be removed from the list
	 */
	public void removeThread(ThreadHandler th){
		if(threadList!=null){
			threadList.remove(th);
			printThreadNames();
		}
	}
	
	/**
	 * Sends a list of running threads to the debug module.
	 */
	public void printThreadNames(){
		String thds = "Running " + threadList.size() + " Threads: ";
		for(ThreadHandler th: threadList) thds += th.getThreadName() + ", ";
		Debg.print(thds);
	}
	
	/**
	 * Closes the log file and clean the list of observed threads.
	 */
	public void closeLog(){
		threadList.clear();
		threadList = null;
		fh.close();
		fh = null;
		log = null;
	}
	
}
