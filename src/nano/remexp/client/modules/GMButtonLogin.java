package nano.remexp.client.modules;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import nano.remexp.client.ClientGUI;

/**
 * A GUI module that holds the login button.
 * 
 * @author Dominic Bosch
 * @version 1.1 29.08.2012 
 */
public class GMButtonLogin implements GUIModule{
	public ClientGUI gui = null;
	private Dimension size;

	@Override public JPanel init(ClientGUI chief){
		gui = chief;
		JPanel panel = new JPanel();
		panel.setBackground(Color.white);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		ClientGUI.setupButton(panel);
		size = panel.getSize();
		JButton buttonLogin = new JButton();
		ClientGUI.setupButton(buttonLogin);
		buttonLogin.setText("Login");
		buttonLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				gui.openLoginDialog();
			}
		});
		panel.add(buttonLogin);
		return panel;
	}
	@Override public Dimension getSize() {return size;}
	@Override public void setState(String state){}
	@Override public void setInfo(String info){}
	@Override public void setParameter(String param){}
	@Override public void terminate() {
		gui = null;
		size = null;
	}
}
