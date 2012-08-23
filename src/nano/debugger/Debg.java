package nano.debugger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import nano.remexp.CommandExecutor;
import nano.remexp.ThreadHandler;
import nano.remexp.net.EventSocket;

/**
 * A static Debugger to ease error tracking.
 * 
 * @author Dominic Bosch
 * @version 1.1 23.08.2012
 */
public class Debg {

	public static final int DEBUGMODE_NODEBUG = -1;
	public static final int DEBUGMODE_REDUCED = 1;
	public static final int DEBUGMODE_FULL = 2;

	public static final int MSG_NORMAL = 1;
	public static final int MSG_ERROR = 2;
	
	// This will generate a continuous data stream from the CBR to the clients which helps debugging:
	public static final boolean GENERATE_DATA = false; 

	public static int debugModeConsole;
	public static int debugModeLogFile;
	private static LogFile log;

	public static void setDebugMode(String logFileName, int dbgmodeCons, int dbgmodeLog){
		debugModeConsole = dbgmodeCons;
		debugModeLogFile = dbgmodeLog;
		if(logFileName == null) debugModeLogFile = DEBUGMODE_NODEBUG;
		if(dbgmodeLog > -1) log = new LogFile(logFileName);
		String sc = getDebugModeInString(dbgmodeCons);
		String sl = getDebugModeInString(dbgmodeLog);
		print("Setting debug mode for Console: [" + sc + "] and for LogFile: [" + sl + "]");
	}
	
	private static String getDebugModeInString(int mode){
		switch(mode){
			case DEBUGMODE_NODEBUG:
				return "NO DEBUG MESSAGES";
			case DEBUGMODE_REDUCED:
				return "REDUCED DEBUG MESSAGES";
			case DEBUGMODE_FULL:
				return "FULL DEBUG MESSAGES";
		}
		return "[ERR] DEBUG MODE NOT HANDLED";
	}

	/**
	 * Prints a string to the console if debugging is turned on.
	 * 
	 * @param msg The message to be printed to the console.
	 */
	synchronized public static void print(String msg){
		spreadDebugMessage(msg, MSG_NORMAL);
	}

	/**
	 * Prints a string with a certain indent to the console if debugging is turned on.
	 * 
	 * @param indent The indent to be added to the message.
	 * @param msg The message to be printed to the console.
	 *//*
	synchronized public static void print(int indent, String msg){
		spreadDebugMessage(shiftString(indent, "[" + getSource() + "] " + msg), MSG_NORMAL);
	}*/
	
	synchronized private static void spreadDebugMessage(String msg, int msgType){
		String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()) + ": ";		
		msg = "[" + getSource() + "] " + msg;
		switch(debugModeConsole){ 
		case DEBUGMODE_FULL: case DEBUGMODE_REDUCED:
			if(msgType == MSG_NORMAL){
				System.out.println(timeStamp + msg);
				if(debugModeLogFile > -1 && log != null) log.addToLog(timeStamp + msg);
			}
			else if(msgType == MSG_ERROR) {
				System.err.println(timeStamp + "!ERROR! " + msg);
				if(debugModeLogFile > -1 && log != null) log.addToLog(timeStamp + "!ERROR! " + msg);
			}
		}
	}
	
	synchronized public static void addThread(ThreadHandler th){
		if(debugModeLogFile > -1 && log != null) log.addThread(th);
	}
	
	synchronized public static void removeThread(ThreadHandler th){
		if(debugModeLogFile > -1 && log != null) log.removeThread(th);
	}
	
	/**
	 * Adds preceding whitespaces to a string and returns it.
	 * 
	 * @param indent The number of whitespaces to be added.
	 * @param msg The message that is returned with the added preceding whitespaces.
	 * @return The resulting string.
	 */
	/*synchronized private static String shiftString(int indent, String msg){
		return String.format("%" + (indent + 2) + "s", "") + msg;
	}*/
	
	/**
	 * This method is used to print a message together with a rough overview of the stream data
	 * that is being passed to the method if debugging is turned on.
	 * 
	 * @param indent The number of preceding whitespaces for this message.
	 * @param msg The message that is also printed.
	 * @param b The byte array that is used to create a rough overview over the stream. 
	 */
	synchronized public static void print(int indent, String msg, byte[] b){
		if(b.length == 0) spreadDebugMessage("[" + getSource() + "] " + msg + "No data found", MSG_NORMAL);
		else {
			String bstr = Byte.toString(b[0]);
			int i, avg = 0;
			byte min = Byte.MAX_VALUE, max = Byte.MIN_VALUE;
			if(debugModeConsole > -1 || debugModeLogFile > -1) {
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
	 * This method is used to place an error message in the console if debugging is turned on. It adds
	 * information about the calling source of this method to the error message.
	 * 
	 * @param msg The error message to be placed in the console.
	 */
	synchronized public static void err(String msg){
		spreadDebugMessage(msg, MSG_ERROR);
	}
	
	/**
	 * Places a System.err.println, if debugging is turned on, with the message and preceding white spaces 
	 * according to indent. It adds information about the calling source of this method to the error message.
	 * 
	 * @param indent The number of preceding white spaces.
	 * @param msg The error message to be printed in the console. 
	 *//*
	synchronized public static void err(int indent, String msg){
		spreadDebugMessage("[" + getSource() + "] " + msg, MSG_ERROR);
	}*/
	
	/**
	 * If a parse error happened for the tokens that are being sent through the event sockets,
	 * this method allows to trace why the command wasn't recognized by the parser. 
	 * 
	 * @param cmd The command that was sent and has been tried to parse.
	 * @param cmds The commands that were valid in the appropriate parser.
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
	 * This function invokes an exception object and backtraces through this the caller of this method.
	 * 
	 * @return The caller class and the line on which the command has been invoked.
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
		 
		if(debugModeConsole == DEBUGMODE_FULL || debugModeLogFile == DEBUGMODE_FULL) return name + srcLine;
		else {
			String[] splittedName = name.split("\\.");
			return splittedName[splittedName.length - 1] + srcLine;
		}
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
	
	public static void closeLog(){
		log.closeLog();
	}
}
