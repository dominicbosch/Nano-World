package nano.remexp.client.layouts;

import java.util.Vector;
import javax.swing.JPanel;

import nano.remexp.client.ClientGUI;
import nano.remexp.client.modules.GUIModule;

/**
 * The super class for the GUI layouts. It handles the GUI module registration
 * and information passing.
 * 
 * @author Dominic Bosch
 * @version 1.1 29.08.2012 
 */
public abstract class GUILayout {
	protected Vector<GUIModule> guiModules;
	protected ClientGUI gui;
	protected JPanel panelControls;
	
	/**
	 * The super constructor that prepares this object for modules.
	 * 
	 * @param g the GUI that is needed by the modules for function calls
	 */
	protected GUILayout(ClientGUI g){
		gui = g;
		guiModules = new Vector<GUIModule>();
	}

	/**
	 * Returns the JPanel with the controls on this layout.
	 * 
	 * @return	the panel containing the controls
	 */
	public JPanel getPanel(){
		return panelControls;
	}

	/**
	 * The height of the controls panel.
	 * 
	 * @return	the height of the controls
	 */
	public int getHeight(){
		return panelControls.getHeight();
	}

	/**
	 * Registers a module in this layout for further message passing.
	 * 
	 * @param gm		the GUI module to be registered
	 * @param panel		the panel to which this module should be added
	 */
	protected void addModule(GUIModule gm, JPanel panel){
		guiModules.add(gm);
		panel.add(gm.init(gui));
	}
	
	/**
	 * Propagates state messages to the gui modules
	 * 	
	 * @param state		the state to be forwarded
	 */
	public void setState(String state){
		if(guiModules != null) for(GUIModule gm: guiModules) gm.setState(state);
	}

	/**
	 * Propagates info messages to the gui modules
	 * 	
	 * @param info		the info to be forwarded
	 */
	public void setInfo(String info){
		if(guiModules != null) for(GUIModule gm: guiModules) gm.setInfo(info);
	}

	/**
	 * Propagates parameter messages to the gui modules
	 * 	
	 * @param param		the parameter to be forwarded
	 */
	public void setParameter(String param){
		if(guiModules != null) for(GUIModule gm: guiModules) gm.setParameter(param);
	}
	
	/**
	 * Terminates all GUI modules and clears them from the list.
	 */
	public void terminate(){
		if(guiModules != null) for(GUIModule gm: guiModules) gm.terminate();
		guiModules.clear();
		panelControls.removeAll();
		guiModules = null;
		gui = null;
	}
}
