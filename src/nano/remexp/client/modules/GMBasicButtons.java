package nano.remexp.client.modules;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import nano.remexp.client.ClientGUI;
import nano.remexp.net.NanoComm;

/**
 * A GUI module that holds the baic control buttons for the remote experiment.
 * 
 * @author Dominic Bosch
 * @version 1.1 29.08.2012 
 */
public class GMBasicButtons implements GUIModule{
	private static final int N_BUT = 3; // max number of buttons in one column
	
	public ClientGUI gui = null;
	private JButton buttonApproach;
	private JButton buttonStart;
	private JButton buttonStopStage;
	private JButton buttonWithdraw;
	private JButton buttonStop;
	private Dimension size;
	private boolean isInitialized = false;

	@Override public JPanel init(ClientGUI chief){
		gui = chief;
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		size = new Dimension(190, (int)ClientGUI.DIM_BUTTON.getHeight()*N_BUT);
		ClientGUI.setupComponent(panel, size, Color.white);

		buttonApproach = new JButton();
		buttonStart = new JButton();
		buttonWithdraw = new JButton();
		
		ClientGUI.setupButton(buttonApproach);
		ClientGUI.setupButton(buttonStart);
		
		buttonApproach.setText("Approach");
		buttonApproach.setEnabled(false);
		buttonApproach.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {gui.sendCommand(NanoComm.CMD_AUTOAPPROACH);}
		});

		buttonStart.setText("Start Scan");
		buttonStart.setEnabled(false);
		buttonStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {gui.sendCommand(NanoComm.CMD_START);}
		});
		
		buttonWithdraw.setText("Withdraw");
		buttonWithdraw.setEnabled(false);
		buttonWithdraw.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {gui.sendCommand(NanoComm.CMD_WITHDRAW);}
		});

		buttonStopStage = new JButton();
		buttonStop = new JButton();

		ClientGUI.setupButton(buttonStopStage);
		ClientGUI.setupButton(buttonStop);
		ClientGUI.setupButton(buttonWithdraw);

		buttonStopStage.setText("Stop Appr.");
		buttonStopStage.setEnabled(false);
		buttonStopStage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {gui.sendCommand(NanoComm.CMD_STOPAPPROACH);}
		});
		
		buttonStop.setText("Stop Scan");
		buttonStop.setEnabled(false);
		buttonStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {gui.sendCommand(NanoComm.CMD_STOP);}
		});
		
		JLabel spacer = new JLabel();
		ClientGUI.setupButton(spacer);
		panel.add(buttonApproach);
		panel.add(buttonStopStage);
		panel.add(buttonWithdraw);;
		panel.add(spacer);
		panel.add(buttonStart);
		panel.add(buttonStop);

		isInitialized = true;
		return panel;
	}

	@Override public Dimension getSize() {return size;}
	@Override public void setState(String state){
		try{
			int val = Integer.parseInt(state.substring(NanoComm.COMMAND_STATE.length() + 1));
			if(isInitialized) {
				buttonApproach.setEnabled(false);
				buttonStart.setEnabled(false);
				buttonStopStage.setEnabled(false);
				buttonWithdraw.setEnabled(false);
				buttonStop.setEnabled(false);
				
				switch(val){
					case NanoComm.STATE_STAGEREADY:
						buttonApproach.setEnabled(true);
						break;
					case NanoComm.STATE_APPROACHING:
						buttonStopStage.setEnabled(true);
						break;
					case NanoComm.STATE_APPROACHED:
						buttonStart.setEnabled(true);
						buttonWithdraw.setEnabled(true);
						break;
					case NanoComm.STATE_SCANNING:
						buttonStop.setEnabled(true);
						break;
				}
			}	
		} catch (NumberFormatException e){}
	}
	@Override public void setInfo(String info){}
	@Override public void setParameter(String param){}
	@Override public void terminate() {
		gui = null;
		buttonApproach = null;
		buttonStart = null;
		buttonStopStage = null;
		buttonWithdraw = null;
		buttonStop = null;
		size = null;
		isInitialized = false;
	}
}
