package nano.remexp.client.modules;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import nano.remexp.client.ClientGUI;
import nano.remexp.net.NanoComm;

/**
 * A GUI module that holds the movement arrows for movements on the sample.
 * 
 * @author Dominic Bosch
 * @version 1.1 29.08.2012 
 */

public class GMMovementArrows implements GUIModule{
	public ClientGUI gui = null;
	
	private JLabel labelArrowUp;
	private JLabel labelArrowDown;
	private JLabel labelArrowLeft;
	private JLabel labelArrowRight;
	private ImageIcon imgArrowUp;
	private ImageIcon imgArrowLeft;
	private ImageIcon imgArrowDown;
	private ImageIcon imgArrowRight;
	private ImageIcon imgArrowUpInactive;
	private ImageIcon imgArrowDownInactive;
	private ImageIcon imgArrowLeftInactive;
	private ImageIcon imgArrowRightInactive;
	private Dimension size;
	private boolean isInitialized = false;

	@Override public JPanel init(ClientGUI chief){
		gui = chief;
		imgArrowUp = ClientGUI.getImage("arrow-up.jpg");
		imgArrowLeft = ClientGUI.getImage("arrow-left.jpg");
		imgArrowDown = ClientGUI.getImage("arrow-down.jpg");
		imgArrowRight = ClientGUI.getImage("arrow-right.jpg");
		imgArrowUpInactive = ClientGUI.getImage("arrow-up_inactive.jpg");
		imgArrowLeftInactive = ClientGUI.getImage("arrow-left_inactive.jpg"); 
		imgArrowDownInactive = ClientGUI.getImage("arrow-down_inactive.jpg");
		imgArrowRightInactive = ClientGUI.getImage("arrow-right_inactive.jpg");
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		JPanel panelArrows = new JPanel();
		JLabel labelArrows = new JLabel();

		size = new Dimension(110, 100);
		ClientGUI.setupComponent(panel, size, Color.gray);
		ClientGUI.setupComponent(panelArrows, new Dimension(75, 75), Color.green);
		ClientGUI.setupComponent(labelArrows, new Dimension(75, 23), Color.magenta);
		
		panelArrows.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		labelArrows.setText("Adjust Position:");
		ClientGUI.setupComponent(labelArrows, new Dimension(100, 23));
		
		labelArrowUp = new JLabel();
		labelArrowDown = new JLabel();
		labelArrowLeft = new JLabel();
		labelArrowRight = new JLabel();
		
		labelArrowUp.setBackground(Color.WHITE);
		labelArrowUp.setIcon(imgArrowUpInactive);
		labelArrowUp.addMouseListener(new MouseAdapter() {  
			public void mouseReleased(MouseEvent evt){  
				gui.sendCommand(NanoComm.CMD_MOVETIP + " value=up");
			}  
		});
		
		labelArrowLeft.setBackground(Color.WHITE);
		labelArrowLeft.setIcon(imgArrowLeftInactive);
		labelArrowLeft.addMouseListener(new MouseAdapter() {  
			public void mouseReleased(MouseEvent evt){
				gui.sendCommand(NanoComm.CMD_MOVETIP + " value=left");
			}
		});
		
		labelArrowRight.setBackground(Color.WHITE);
		labelArrowRight.setIcon(imgArrowRightInactive);
		labelArrowRight.addMouseListener(new MouseAdapter() {  
			public void mouseReleased(MouseEvent evt){  
				gui.sendCommand(NanoComm.CMD_MOVETIP + " value=right");
			}
		});
		
		labelArrowDown.setBackground(Color.WHITE);
		labelArrowDown.setIcon(imgArrowDownInactive);
		labelArrowDown.addMouseListener(new MouseAdapter() {  
			public void mouseReleased(MouseEvent evt){  
				gui.sendCommand(NanoComm.CMD_MOVETIP + " value=down");
			}
		});
		
		panelArrows.add(createBlankArrow());
		panelArrows.add(labelArrowUp);
		panelArrows.add(createBlankArrow());
		panelArrows.add(labelArrowLeft);
		panelArrows.add(createBlankArrow());
		panelArrows.add(labelArrowRight);
		panelArrows.add(createBlankArrow());
		panelArrows.add(labelArrowDown);
		panelArrows.add(createBlankArrow());
		
		panel.add(labelArrows);
		panel.add(panelArrows);
		isInitialized = true;
		return panel;
	}
	@Override public Dimension getSize() {return size;}
	@Override public void setState(String state){
		try{
			int val = Integer.parseInt(state.substring(NanoComm.COMMAND_STATE.length() + 1));
			if(isInitialized) switch(val){
				case NanoComm.STATE_STAGEREADY:
					labelArrowUp.setIcon(imgArrowUp);
					labelArrowLeft.setIcon(imgArrowLeft);
					labelArrowDown.setIcon(imgArrowDown);
					labelArrowRight.setIcon(imgArrowRight);
					break;
				default:
					labelArrowUp.setIcon(imgArrowUpInactive);
					labelArrowLeft.setIcon(imgArrowLeftInactive);
					labelArrowDown.setIcon(imgArrowDownInactive);
					labelArrowRight.setIcon(imgArrowRightInactive);
			}
		} catch (NumberFormatException e){}
	}
	@Override public void setInfo(String info){}
	@Override public void setParameter(String param){}
	
	/**
	 * A blank arrow label to fill the arrow box.
	 * 
	 * @return A JLabel holding the blank box.
	 */
	private JLabel createBlankArrow(){
		JLabel arrow = new JLabel();
		arrow.setBackground(Color.WHITE);
		arrow.setIcon(ClientGUI.getImage("blank25x25.jpg"));
		return arrow;
	}
	@Override public void terminate() {
		gui = null;
		size = null;
		labelArrowUp = null;
		labelArrowDown = null;
		labelArrowLeft = null;
		labelArrowRight = null;
		imgArrowUp = null;
		imgArrowLeft = null;
		imgArrowDown = null;
		imgArrowRight = null;
		imgArrowUpInactive = null;
		imgArrowDownInactive = null;
		imgArrowLeftInactive = null;
		imgArrowRightInactive = null;
		isInitialized = false;
	}
}
