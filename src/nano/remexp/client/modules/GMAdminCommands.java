package nano.remexp.client.modules;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import nano.remexp.client.ClientGUI;

/**
 * A GUI module that holds an input text field for the administrator commands.
 * 
 * @author Dominic Bosch
 * @version 1.1 29.08.2012 
 */
public class GMAdminCommands implements GUIModule{
	public ClientGUI gui = null;
	private JTextField userInput;
	
	@Override public void setState(String state){}
	@Override public void setInfo(String info){}
	@Override public void setParameter(String param){}
	@Override public JPanel init(ClientGUI chief){
		gui = chief;
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		JLabel cmdLabel = new JLabel("Command: ");
		userInput = new JTextField();
		ClientGUI.setupComponent(panel, new Dimension(520, 23), Color.blue);
		ClientGUI.setupComponent(cmdLabel, new Dimension(70, 23), Color.green);
		ClientGUI.setupComponent(userInput, new Dimension(440, 23), Color.red);
		panel.add(cmdLabel);
		panel.add(userInput);
		userInput.addKeyListener(new KeyListener(){
			@Override public void keyPressed(KeyEvent arg0) {
				if(arg0.getKeyCode() == KeyEvent.VK_ENTER){
					gui.sendCommand(userInput.getText());
					userInput.setText("");
				}
			}
			@Override public void keyReleased(KeyEvent arg0) {}
			@Override public void keyTyped(KeyEvent arg0) {}
		});
		return panel;
	}
	@Override public Dimension getSize() {return null;}
	@Override public void terminate() {
		gui = null;
		userInput = null;
	}
}
