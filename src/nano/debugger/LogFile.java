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

public class LogFile {
	final static int FILE_SIZE = 1*1024*1024;
	final static int NUM_LOGS = 500;
	private Vector<ThreadHandler> threadList;
	public Logger log;
	FileHandler fh;

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
	
	public void addToLog(String msg){
		log.info(msg);
	}
	
	public void addThread(ThreadHandler th){
		threadList.add(th);
		printThreadNames();
	}
	
	public void removeThread(ThreadHandler th){
		threadList.remove(th);
		printThreadNames();
	}
	
	public void printThreadNames(){
		String thds = "Running " + threadList.size() + " Threads: ";
		for(ThreadHandler th: threadList) thds += th.getThreadName() + ", ";
		Debg.print(thds);
	}
	
	public void closeLog(){
		fh.close();
	}
	
}
