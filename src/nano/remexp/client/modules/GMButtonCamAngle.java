package nano.remexp.client.modules;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import nano.remexp.client.ClientGUI;
import nano.remexp.net.NanoComm;

/**
 * A GUI module that holds one button to choose the camera
 * angle of the built in remote experiment camera.
 * 
 * @author Dominic Bosch
 * @version 1.1 29.08.2012 
 */
public class GMButtonCamAngle implements GUIModule{
	public ClientGUI gui = null;
	private JButton buttonCamAngle;//TODO this command can always be sent, implement on cbr that this command is being interpreted even if a lock is set
	private Dimension size;

	@Override public JPanel init(ClientGUI chief){
		gui = chief;
		JPanel panel = new JPanel();
		panel.setBackground(Color.white);
		ClientGUI.setupButton(panel);
		size = panel.getSize();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		buttonCamAngle = new JButton();
		ClientGUI.setupButton(buttonCamAngle);
		buttonCamAngle.setText("Cam angle");
		buttonCamAngle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				buttonCamAngle.setVisible(false);
				gui.sendCommand(NanoComm.CMD_CAMANGLE);
				try {Thread.sleep(1500);} catch (InterruptedException e) {}
				buttonCamAngle.setVisible(true);
			}
		});
		buttonCamAngle.setEnabled(false);
		panel.add(buttonCamAngle);
		return panel;
	}
	@Override public Dimension getSize() {return size;}
	@Override public void setState(String state){}
	@Override public void setInfo(String info){
		try{
			int val = Integer.parseInt(info.substring(NanoComm.COMMAND_INFO.length() + 1));
			switch(val){
			case NanoComm.INFO_SERVER_DISCONNECTED:
				buttonCamAngle.setEnabled(false);
				break;
			case NanoComm.INFO_REMEXP_CONNECTED:
				buttonCamAngle.setEnabled(true);
				break;
			case NanoComm.INFO_REMEXP_DISCONNECTED:
				buttonCamAngle.setEnabled(false);
			}
		} catch(NumberFormatException e){}
	}
	@Override public void setParameter(String param){}
	@Override public void terminate() {
		gui = null;
		buttonCamAngle = null;
		size = null;
	}
}
