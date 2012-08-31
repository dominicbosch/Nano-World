package nano.remexp;

import java.util.Hashtable;
import java.util.NoSuchElementException;

import nano.debugger.Debg;
import nano.remexp.net.NanoComm;


/**
 * This Parser handles events and executes the CommandExecutors on demand.
 * 
 * @author Dominic Bosch
 * @version 1.1 23.08.2012
 */
public class Parser{
	private Hashtable<String, CommandExecutor> validCommands;

	/**
	 * Initializes an object of this class and stores the hash table that was passed.
	 * 
	 * @param newCommands The commands-to-CommandExecutor hash table.
	 */
	public Parser(Hashtable<String, CommandExecutor> newCommands){
		validCommands = newCommands;
	}

	/**
	 * Splits a string up and stores it in a Hashtable as key value pairs.
	 * The whole string is split by the standard delimiters (" \t\n\r\f").
	 * Each of these tokens is split by '=' and stored in a hash table as key=value pair.
	 * 
	 * @param commandLine The string to be parsed (e.g. "command=adjustaxis name=x value=100").
	 * @return A hash table containing key=value pairs.
	 */
	public static Hashtable<String, String> extractTags(String commandLine){
		Hashtable<String, String> myHashtable = new Hashtable<String, String>();
		String[] pair;
		String[] parts = splitString(commandLine);
		for(String s: parts) {
			pair = s.split(NanoComm.DELIMITER);
			if(pair.length > 1) myHashtable.put(pair[0], pair[1]);
		}
		return myHashtable;
	}
    
	/**
	 * Tries to find the value for the key "command" in the given string and tries
	 * to execute the CommandExecutor for the appropriate command.
	 * 
	 * @param commandLine the string containing the command, e.g. "command=start".
	 */
	//TODO clean up this method
	public boolean executedCommand(String commandLine) {
		Hashtable<String, String> commandTags = null;
		CommandExecutor ce = null;
		try{
			commandTags = extractTags(commandLine);
			String command = commandTags.get(NanoComm.COMMAND_CMD);
			ce = validCommands.get(command);
			try{
				if(ce != null && commandTags != null) {
					ce.execute(commandTags);
					return true;
				}
			} catch(Exception gev){
				gev.printStackTrace();
			}
		} catch(NullPointerException e){
			Debg.explainParserError(commandLine, validCommands);
		} catch(NoSuchElementException ev){
			Debg.explainParserError(commandLine, validCommands);
		} catch(Exception gev){
			Debg.explainParserError(commandLine, validCommands);
			gev.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Returns true if the command is recognized by this parser, false if not.
	 * 
	 * @param command The command we want to check for validity, e.g. goto2, calibratestage,...
	 * @return true if the command is valid for this parser, else false.
	 */
	public boolean isValidCommand(String command){
		return validCommands.get(command) != null;
	}
	
	/**
	 * Searches for the value for the command tag.
	 * 
	 * @param msg The string holding the command=value pair.
	 * @return The value if the command exists, else null.
	 */
	public static synchronized String getValue(String msg, String cmd){
		String[] parts = splitString(msg);
		String[] pair;
		for(String s: parts) {
			pair = s.split(NanoComm.DELIMITER);
			if(pair.length > 1 && pair[0].equals(cmd)) {
				return pair[1];
			}
		}
		return null;
	}
	
	/**
	 * Splits a string that contains new lines, tabs, carriage returns, form feeds and blanks.
	 * @param str the string to be splitted.
	 * @return an array containing all strings that were separated in the string.
	 */
	private static String[] splitString(String str){
		return str.split("\n|\t|\r|\f| ");
	}
}
