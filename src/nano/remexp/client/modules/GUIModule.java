package nano.remexp.client.modules;

import java.awt.Dimension;
import javax.swing.JPanel;

import nano.remexp.client.ClientGUI;

/**
 * GUI modules can be placed in GUI layouts. they show specific behaviour for certain
 * commands or informations received by the server. 
 * 
 * @author Dominic Bosch
 * @version 1.1 29.08.2012 
 */
public interface GUIModule {
	/**
	 * Initializes the GUI module.
	 * @param gui	the GUI that is used for function calls by the module.
	 * @return		the JPanel holding all module components
	 */
	public JPanel init(ClientGUI gui);
	
	/**
	 * Processes state information that is sent to the module.
	 * @param state		the state information
	 */
	public void setState(String state);
	
	/**
	 * Returns the dimensions of the module.
	 * @return	the dimensions of the module.
	 */
	public Dimension getSize();
	
	/**
	 * Processes parameter information that is sent to the module.
	 * @param params the parameter string
	 */
	public void setParameter(String params);
	
	/**
	 * Processes general informations that are sent to the module.
	 * @param info		the information string
	 */
	public void setInfo(String info);
	
	/**
	 * Terminates the GUI module.
	 */
	public void terminate();
}
