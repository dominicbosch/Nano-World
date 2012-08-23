package nano.remexp;

import java.util.Hashtable;

/**
 * The interface for the command executing classes that react on command=value [...] strings.
 * 
 * @author Dominic Bosch
 * @version 1.1 23.08.2012
 */
abstract public class CommandExecutor{
	/**
	 * The method that executes the appropriate actions on a command.
	 * 
	 * @param tags The tags contained in the message.
	 */
    abstract public void execute(Hashtable<String, String> tags);
}
