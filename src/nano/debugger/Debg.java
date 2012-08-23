package nano.debugger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import nano.remexp.CommandExecutor;
import nano.remexp.ThreadHandler;
import nano.remexp.net.EventSocket;

/**
 * A static Debugger to generally decide between System output, or log file, or both, or none.
 * 
 * @author Dominic Bosch
 * @version 1.1 23.08.2012
 */
public class Debg {
	// This will generate a continuous data stream from the CBR to the clients which helps debugging:
	public static final boolean GENERATE_DATA = false;
	private static final boolean DEBUG_STREAM = false; // set true if you want to see some stream data information

	public static final int DEBUGMODE_NODEBUG = -1;
	public static final int DEBUGMODE_STDOUT = 1;
	public static final int DEBUGMODE_LOGFILE = 2;
	public static final int DEBUGMODE_BOTH = 3;

	public static final int MSG_NORMAL = 1;
	public static final int MSG_ERROR = 2;

	public static int debugMode = DEBUGMODE_STDOUT;
	private static LogFile log;

	/**
	 * This allows the different running instances of the remexp package to choose their debug mode.
	 * 
	 * @param logFileName 	The string that goes between the date and the .log extension. Null if no logfile is used. 
	 * @param dbgMode 		The desired debug mode:
	 * 							DEBUGMODE_NODEBUG: no debug messages are displayed or stored.
	 * 							DEBUGMODE_STDOUT: debug messages are printed to the standard output.
	 * 							DEBUGMODE_LOGFILE: debug messages are stored in the logfile.
	 * 							DEBUGMODE_NODEBUG: debug messages are printed to the standard output and stored in the logfile.
	 */
	public static void setDebugMode(String logFileName, int dbgMode){
		debugMode = dbgMode;
		if(logFileName == null){ // reduce debug mode if no log file was defined
			if(debugMode == DEBUGMODE_LOGFILE) debugMode = DEBUGMODE_NODEBUG;
			else if(debugMode == DEBUGMODE_BOTH) debugMode = DEBUGMODE_STDOUT; 
		} else if(debugMode == DEBUGMODE_LOGFILE || debugMode == DEBUGMODE_BOTH) {
			log = new LogFile(logFileName);
		}
		print("Setting debug mode to: [" + getDebugModeInString(dbgMode) + "]");
	}
	
	/**
	 * Return a string that describes the debug mode passed to this function.
	 * 
	 * @param mode 		the debug mode that shall be explained
	 * @return			a string describging the debug mode
	 */
	private static String getDebugModeInString(int mode){
		switch(mode){
			case DEBUGMODE_NODEBUG:
				return "NO DEBUG MESSAGES";
			case DEBUGMODE_STDOUT:
				return "DEBUG ON STDOUT";
			case DEBUGMODE_LOGFILE:
				return "DEBUG INTO LOGFILE";
			case DEBUGMODE_BOTH:
				return "DEBUG TO STDOUT AND LOGFILE";
		}
		return "[ERR] DEBUG MODE NOT HANDLED";
	}

	/**
	 * Processes a normal debug message. 
	 * 
	 * @param msg	the message to be processed
	 */
	synchronized public static void print(String msg){
		spreadDebugMessage(msg, MSG_NORMAL);
	}
	
	/**
	 * Processes an error debug message.
	 * 
	 * @param msg The error message to be processed.
	 */
	synchronized public static void err(String msg){
		spreadDebugMessage(msg, MSG_ERROR);
	}
	
	/**
	 * Main procedure of this abstract class. We determine between normal and error messages and the debug mode and 
	 * react accordingly. This method also adds a time stamp and the caller object information.
	 * 
	 * @param msg 		the message that has been sent for debugging
	 * @param msgType 	either @see MSG_NORMAL or @see MSG_ERROR
	 */
	synchronized private static void spreadDebugMessage(String msg, int msgType){
		String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()) + ": ";		
		msg = "[" + getSource() + "] " + msg;
		switch(debugMode){ 
			case DEBUGMODE_STDOUT:
			case DEBUGMODE_BOTH:
				if(msgType == MSG_NORMAL) System.out.println(timeStamp + msg);
				else if(msgType == MSG_ERROR) System.err.println(timeStamp + "!ERROR! " + msg);
				if(debugMode == DEBUGMODE_STDOUT) break; //only break when we don't want to write to the log file
			case DEBUGMODE_LOGFILE:
				if(debugMode> -1 && log != null){
					if(msgType == MSG_NORMAL) log.addToLog(timeStamp + msg);
					else if(msgType == MSG_ERROR) log.addToLog(timeStamp + "!ERROR! " + msg);
				}
		}
	}
	
	/**
	 * This class also allows thread tracking.
	 * Every started @see ThreadHandler thread will be stored and kept track of until it dies.
	 * This method adds a thread to the list of observed threads.
	 * 
	 * @param th	The new thread to be tracked
	 */
	synchronized public static void addThread(ThreadHandler th){
		if(debugMode >= DEBUGMODE_LOGFILE && log != null) log.addThread(th);
	}
	
	/**
	 * Removes a thread from the list of observed threads during its death.
	 * 
	 * @param th	the dying thread
	 */
	synchronized public static void removeThread(ThreadHandler th){
		if(debugMode >= DEBUGMODE_LOGFILE && log != null) log.removeThread(th);
	}

	/**
	 * This method is used to print a message together with a rough overview of the stream data
	 * that is being passed to the method if debugging is turned on.
	 * 
	 * @param msg The message that is also printed.
	 * @param b The byte array that is used to create a rough overview over the stream. 
	 */
	synchronized public static void print(String msg, byte[] b){
		if(DEBUG_STREAM){
			if(b.length == 0) spreadDebugMessage("[" + getSource() + "] " + msg + "No data found", MSG_NORMAL);
			else {
				String bstr = Byte.toString(b[0]);
				int i, avg = 0;
				byte min = Byte.MAX_VALUE, max = Byte.MIN_VALUE;
				for(i = 1; i < b.length; i++){
					bstr += "|" + b[i];
					if(b[i] < min) min = b[i];
					if(b[i] > max) max = b[i];
					avg += b[i];
				}
				if(i < 2) avg = 0;
				else avg = avg/(i-1);
				spreadDebugMessage("[" + getSource() + "] " + msg + " length=" + b.length + " first=" + b[0] + ", min=" 
						+ min + ", max=" + max + ", avg=" + avg + "\n" + String.format("%8s", "") + bstr, MSG_NORMAL);
			}
		}
	}
	
	/**
	 * If a parse error happened for the tokens that are being sent through the event sockets,
	 * this method allows to trace why the command wasn't recognized by the parser. 
	 * 
	 * @param cmd	the command that was sent and has been tried to parse
	 * @param cmds	the commands that were valid in the appropriate parser
	 */
	synchronized public static void explainParserError(String cmd, Hashtable<String, CommandExecutor> cmds){
		String msg;
		StackTraceElement[] stack = new Exception().getStackTrace();
		String caller = stack[2].getClassName();
		String[] splittedName = caller.split("\\.");
		if(!caller.equals(EventSocket.class.getName())){
			msg = "Parser in " + splittedName[splittedName.length - 1] + 
				" couldn't find the command: " + cmd + ", it knows only: ";
			for (Enumeration<String> e = cmds.keys(); e.hasMoreElements();) msg += e.nextElement() + ", ";
			print("  " + msg);
		}
	}
	
	/**
	 * This function invokes an exception object and finds the caller of this method.
	 * 
	 * @return The caller object and the line on which the command has been invoked.
	 */
	synchronized public static String getSource(){
		String[] arr;
		String name, srcLine;
		int id, line;
		StackTraceElement[] stack = new Exception().getStackTrace();
		id = findCaller(stack);
		name = stack[id].getClassName();
		arr = name.split("\\.");
		if(arr.length > 0) name = arr[arr.length-1];
		line = stack[id].getLineNumber();
		if(line == -1) srcLine = "";
		else srcLine = ":" + line;
		return name + srcLine;
	}
	
	/**
	 * This method searches in the stack array for this class' name in order to determine from there
	 * the next class which is the caller of a function or method within this class.
	 *  
	 * @param stack The exception stack array holding the trace.
	 * @return The id in the array of the calling class.
	 */
	synchronized private static int findCaller(StackTraceElement[] stack){
		int stackEntryID = -1;
		for(int i = 0; i < stack.length; i++){
			if(stack[i].getClassName().equals(Debg.class.getName())) stackEntryID = i; 
		}
		if(stackEntryID == -1) return 1;
		else return stackEntryID + 1;
	}
	
	/**
	 * This method allows a clean end of the log file.
	 */
	public static void closeLog(){
		log.closeLog();
	}
}
