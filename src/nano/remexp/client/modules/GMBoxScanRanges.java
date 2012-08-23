package nano.remexp.client.modules;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import nano.remexp.Parser;
import nano.remexp.client.ClientGUI;
import nano.remexp.net.NanoComm;

/**
 * A GUI module that holds a combo box for the client to choose scan ranges.
 * 
 * @author Dominic Bosch
 * @version 1.1 29.08.2012 
 */
public class GMBoxScanRanges implements GUIModule{
	public ClientGUI gui = null;
	private JComboBox comboBoxScanRange;
	private int currentScanRange;
	private Dimension size;
	private boolean isInitialized = false;

	@Override public JPanel init(ClientGUI chief){
		gui = chief;
		currentScanRange = 10;
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		size =  new Dimension(190, 22);
		ClientGUI.setupComponent(panel, size, Color.BLUE);

		JLabel labelScanRangeTitle = new JLabel();
		comboBoxScanRange = new JComboBox();
		ClientGUI.setupComponent(labelScanRangeTitle, new Dimension(80, 22));
		labelScanRangeTitle.setBackground(Color.white);
		labelScanRangeTitle.setText("Scan Range:");

		ClientGUI.setupButton(comboBoxScanRange);
		comboBoxScanRange.setModel(new DefaultComboBoxModel(new String[] { "1 um", "5 um", "10 um", "25 um", "50 um" }));
		comboBoxScanRange.setSelectedIndex(2);
		comboBoxScanRange.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				String cbEntry = (String) comboBoxScanRange.getSelectedItem();
				// only if a button has been pressed it was a user input. else it has been changed via the server
				if(evt.getModifiers() > 0) {
					gui.sendCommand(NanoComm.CMD_SCANRANGE + " value=" + cbEntry.substring(0, cbEntry.length() - 3));
				}
			}
		});
		panel.add(labelScanRangeTitle);
		comboBoxScanRange.setEnabled(false);
		panel.add(comboBoxScanRange);
		isInitialized = true;
		return panel;
	}
	@Override public Dimension getSize() {return size;}
	@Override public void setState(String state){
		try{
			int val = Integer.parseInt(state.substring(NanoComm.COMMAND_STATE.length() + 1));
			if(isInitialized) switch(val){
				case NanoComm.STATE_STAGECALIBRATED:
				case NanoComm.STATE_STAGEREADY:
				case NanoComm.STATE_APPROACHED:
					comboBoxScanRange.setEnabled(true);
					break;
				default:
					comboBoxScanRange.setEnabled(false);
			}
		} catch (NumberFormatException e){}
	}
	@Override public void setInfo(String info){}
	@Override public void setParameter(String param){
			int cmd = Integer.parseInt(Parser.getValue(param, NanoComm.COMMAND_PARAM));
			try{
				int val = Integer.parseInt(Parser.getValue(param, "value"));
				if(cmd == NanoComm.PARAM_SCANRANGE){
					if(isInitialized) setScanRange(val);
					else currentScanRange = val;
				}
			} catch (NumberFormatException e){}
	}
	
	/**
	 * sets the current scan range in the combo box after it has been changed and
	 * was propagated over the remote experiment broadcaster.
	 * @param range		the new scan range
	 */
	private void setScanRange(int range){
		Object entry;
		String seek = range + " um";
		currentScanRange = range;
		for (int i = 0; i < comboBoxScanRange.getItemCount(); i++) {
			entry = comboBoxScanRange.getItemAt(i);
		    if(entry.equals(seek)) {
		    	comboBoxScanRange.setSelectedItem(entry);
		    }
		}
	}
	@Override public void terminate() {
		gui = null;
		comboBoxScanRange = null;
		currentScanRange = -1;
		size = null;
		isInitialized = false;
	}
}