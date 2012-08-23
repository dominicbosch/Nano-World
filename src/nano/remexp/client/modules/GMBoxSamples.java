package nano.remexp.client.modules;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import nano.debugger.Debg;
import nano.remexp.Parser;
import nano.remexp.client.ClientGUI;
import nano.remexp.net.NanoComm;

/**
 * A GUI module that holds the sample combo box for the client to choose from.
 * 
 * @author Dominic Bosch
 * @version 1.1 29.08.2012 
 */
public class GMBoxSamples implements GUIModule{
	public ClientGUI gui = null;
	private JComboBox comboBoxSample;
	private DefaultComboBoxModel comboBoxSampleModel;
	private Vector<Sample> samples;
	private Vector<String> listSampleNames;
	private Dimension size;
	private boolean isInitialized = false;

	@Override public JPanel init(ClientGUI chief){
		gui = chief;
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
		size =  new Dimension(190, 22);
		ClientGUI.setupComponent(panel, size, Color.gray);
		JLabel labelSample = new JLabel();
		ClientGUI.setupComponent(labelSample, new Dimension(60, 22));
		labelSample.setText("Position: ");
		panel.add(labelSample);
		
		samples = new Vector<Sample>();
		listSampleNames = new Vector<String>();
		addCalibrateCommand();
		comboBoxSampleModel = new DefaultComboBoxModel(listSampleNames);
		comboBoxSample = new JComboBox();
		ClientGUI.setupButton(comboBoxSample);
		comboBoxSample.setModel(comboBoxSampleModel);
		comboBoxSample.setSelectedIndex(0);
		comboBoxSample.addActionListener(new ActionListener() {
			private String lastPosition;
			public void actionPerformed(ActionEvent evt) {
				String choice = (String) comboBoxSample.getSelectedItem();
				// only if a button has been pressed it was a user input. else it has been changed via the server
				if(evt.getModifiers() > 0) {
					if(choice.equals("Calibrate")) {
						gui.sendCommand(NanoComm.CMD_CALIBRATESTAGE);
					} else {
						if(!choice.equals(lastPosition)){
							gui.sendCommand(getSampleCommand(choice));
						}
					}
				}
				lastPosition = choice;
			}
		});
		comboBoxSample.setEnabled(false);
		panel.add(comboBoxSample);
		isInitialized = true;
		return panel;
	}
	
	/**
	 * Adds the calibrate command to the drop down box.
	 */
	private void addCalibrateCommand(){
		samples.add(new Sample(-1, "Calibrate", "calibratestage"));
		listSampleNames.add("Calibrate");
	}

	@Override public Dimension getSize() {return size;}
	
	/**
	 * Returns the sample command for the remote experiment broadcaster.
	 * 
	 * @param name	the sample name that has been chosen in the combo box
	 * @return		the command that will cause the remote experiment
	 * 				broadcaster to move to the chosen sample
	 */
	protected synchronized String getSampleCommand(String name){
		Sample s;
		for(int i = 0; i < samples.size(); i++) {
			s = samples.get(i);
			if(name.equals(s.getName())) return s.getCommand();
		}
		return "";
	}
	
	@Override public void setState(String state){
		try{
			int val = Integer.parseInt(Parser.getValue(state, NanoComm.COMMAND_STATE));
			if(isInitialized) switch(val){
				case NanoComm.STATE_STAGECALIBRATED:
				case NanoComm.STATE_STAGEREADY:
					comboBoxSample.setEnabled(true);
					break;
				default:
					comboBoxSample.setEnabled(false);
			}
		} catch (NumberFormatException e){}
	}
	@Override public void setInfo(String info){}
	@Override public void setParameter(String param){
		try {
			int cmd = Integer.parseInt(Parser.getValue(param, NanoComm.COMMAND_PARAM));
			String val = Parser.getValue(param, "value");
			if(isInitialized) switch(cmd){
				case NanoComm.PARAM_STAGEPOSITION:
					try {
						setSample(Integer.parseInt(val));
					} catch (NumberFormatException e){}
					break;
				case NanoComm.PARAM_SAMPLEINFO:
					try{
						String name = Parser.getValue(param, "name");
						String samplecmd = Parser.getValue(param, NanoComm.COMMAND_CMD);
						samples.add(new Sample(Integer.parseInt(val), name, samplecmd));
						comboBoxSampleModel.addElement(name);
					} catch(Exception e){Debg.err("Unable to add sample!");}
					break;
				case NanoComm.PARAM_SAMPLESCLEAR:
					samples.clear();
					comboBoxSampleModel.removeAllElements();
					addCalibrateCommand();
					break;
			}
		} catch(Exception e){
			Debg.err("unable to parse parameter: " + param);
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets the sample, which was selected by the remote experiment, in the combo box.
	 * 
	 * @param sample	the id of the sample that has been
	 * 					selected by the remote experiment
	 */
	private synchronized void setSample(int sample){
		Object entry;
		String name = null;
		for(Sample s: samples) if(sample == s.getID()) name = s.getName();
		
		if(name == null) name = "Calibrate";
		for (int i = 0; i < comboBoxSample.getItemCount(); i++) {
			entry = comboBoxSample.getItemAt(i);
		    if(entry.equals(name)) comboBoxSample.setSelectedItem(entry);
		}
	}

	/**
	 * A GUI representation of a remote experiment sample.
	 * 
	 * @author Dominic Bosch
	 * @version 1.1 29.08.2012 
	 */
	private class Sample {
		private int id;
		private String name;
		private String command;
		
		/**
		 * Initializes a sample representation object.
		 * 
		 * @param id		the sample ID
		 * @param name		the sample name to be displayed
		 * @param command	the command that will make the remote experiment move
		 */
		private Sample(int id, String name, String command){
			this.id = id;
			this.name = name;
			this.command = command;
		}
		
		/**
		 * Returns the sample ID.
		 * 
		 * @return	the sample ID
		 */
		private int getID(){
			return id;
		}
		
		/**
		 * Returns the name of the sample.
		 * 
		 * @return	the sample name
		 */
		private String getName(){
			return name;
		}
		
		/**
		 * Returns the command used to make the remote experiment move.
		 * 
		 * @return	the move command
		 */
		private String getCommand(){
			return command;
		}
	}
	@Override public void terminate() {
		gui = null;
		comboBoxSample = null;
		comboBoxSampleModel = null;
		samples.clear();
		samples = null;
		listSampleNames.clear();
		listSampleNames = null;
		size = null;
		isInitialized = false;
		
	}
}
