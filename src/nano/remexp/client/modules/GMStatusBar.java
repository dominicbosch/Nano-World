package nano.remexp.client.modules;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import nano.remexp.Parser;
import nano.remexp.ThreadHandler;
import nano.remexp.client.ClientGUI;
import nano.remexp.net.NanoComm;

/**
 * A GUI module that holds status information about the 
 * connectivity and the remote experiment state.
 * 
 * @author Dominic Bosch
 * @version 1.1 29.08.2012 
 */
public class GMStatusBar implements GUIModule{
	public ClientGUI gui = null;
	private Blinker blinkerThread;
	private JLabel labelRemExpTitle;
	private JLabel labelRemExpPlug;
	private JLabel labelServerPlug;
	private JLabel labelMovingDot;
	private JLabel labelApproachedDot;
	private ImageIcon imgPlugGreen;
	private ImageIcon imgPlugRedZero;
	private ImageIcon imgPlugRedOne;
	private ImageIcon imgPlugRedTwo;
	private ImageIcon imgPlugRedThree;
	private ImageIcon imgDotGreen;
	private ImageIcon imgDotRed;
	private ImageIcon imgDotDarkRed;
	private Vector<JLabel> arrBlinkingLabel;
	private Vector<ImageIcon> arrStateImg;
	private Dimension size;
	private boolean isServerConnected;
	private boolean isRemExpConnected;
	private boolean isInitialized = false;

	@Override public JPanel init(ClientGUI chief){
		gui = chief;
        isServerConnected = false;
        isRemExpConnected = false;
		arrBlinkingLabel = new Vector<JLabel>();
		arrStateImg = new Vector<ImageIcon>();
		imgDotRed = ClientGUI.getImage("dotRed.jpg");
		imgDotDarkRed = ClientGUI.getImage("dotDarkRed.jpg");
		imgDotGreen = ClientGUI.getImage("dotGreen.jpg");
		imgPlugGreen = ClientGUI.getImage("plug_col_connected_small.jpg");
		imgPlugRedZero = ClientGUI.getImage("plug_col_0_small.jpg");
		imgPlugRedOne = ClientGUI.getImage("plug_col_1_small.jpg");
		imgPlugRedTwo = ClientGUI.getImage("plug_col_2_small.jpg");
		imgPlugRedThree = ClientGUI.getImage("plug_col_3_small.jpg");
		arrStateImg.add(imgPlugRedZero);
		arrStateImg.add(imgPlugRedOne);
		arrStateImg.add(imgPlugRedTwo);
		arrStateImg.add(imgPlugRedThree);
		JPanel panel = new JPanel();
		size = new Dimension(520, 18);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		ClientGUI.setupComponent(panel, size, Color.yellow);

		JPanel panelServer = new JPanel();
		ClientGUI.setupComponent(panelServer, new Dimension(80, 18), Color.cyan);
		panelServer.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		JLabel labelServerTitle = new JLabel();
		labelServerTitle.setBackground(Color.white);
		labelServerTitle.setText("Server");
		labelServerPlug = new JLabel();
		labelServerPlug.setBackground(Color.white);
		labelServerPlug.setIcon(imgPlugRedOne);
		labelServerPlug.setName("Server");
		panelServer.add(labelServerPlug);
		panelServer.add(labelServerTitle);

		JPanel panelRemExp = new JPanel();
		ClientGUI.setupComponent(panelRemExp, new Dimension(120, 18), Color.cyan);
		panelRemExp.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		labelRemExpTitle = new JLabel();
		labelRemExpTitle.setBackground(Color.white);
		labelRemExpTitle.setText("RemExp");
		labelRemExpPlug = new JLabel();
		labelRemExpPlug.setBackground(Color.white);
		labelRemExpPlug.setIcon(imgPlugRedOne);
		labelRemExpPlug.setName("RemExp");
		panelRemExp.add(labelRemExpPlug);
		panelRemExp.add(labelRemExpTitle);

		JPanel spacer = new JPanel();
		ClientGUI.setupComponent(spacer, new Dimension(80, 18), Color.red);
		
		JPanel panelApproach = new JPanel();
		ClientGUI.setupComponent(panelApproach, new Dimension(100, 18), Color.cyan);
		panelApproach.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		JLabel labelApproachedTitle = new JLabel();
		labelApproachedTitle.setBackground(Color.white);
		labelApproachedTitle.setText("Approached");
		labelApproachedDot = new JLabel();
		labelApproachedDot.setBackground(Color.white);
		labelApproachedDot.setIcon(imgDotDarkRed);
		panelApproach.add(labelApproachedDot);
		panelApproach.add(labelApproachedTitle);

		JPanel panelMoving = new JPanel();
		ClientGUI.setupComponent(panelMoving, new Dimension(80, 18), Color.YELLOW);
		panelMoving.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		JLabel labelMovingTitle = new JLabel();
		labelMovingTitle.setBackground(Color.white);
		labelMovingTitle.setText("Moving");
		labelMovingDot = new JLabel();
		labelMovingDot.setBackground(Color.white);
		labelMovingDot.setIcon(imgDotDarkRed);
		panelMoving.add(labelMovingDot);
		panelMoving.add(labelMovingTitle);

		panel.add(panelServer);
		panel.add(panelRemExp);
		panel.add(spacer);
		panel.add(panelMoving);
		panel.add(panelApproach);
		setLabelConnected(labelServerPlug, false);
		setLabelConnected(labelRemExpPlug, false);
		isInitialized = true;
		return panel;
	}
	@Override public Dimension getSize() {return size;}
	@Override public void setState(String state){
		try{
			int val = Integer.parseInt(state.substring(NanoComm.COMMAND_STATE.length() + 1));
			if(isInitialized) switch(val){
				case NanoComm.STATE_UNAVAILABLE:
					labelMovingDot.setIcon(imgDotDarkRed);
					labelApproachedDot.setIcon(imgDotDarkRed);
					break;
				case NanoComm.STATE_STAGECALIBRATED:
				case NanoComm.STATE_STAGEREADY:
						labelMovingDot.setIcon(imgDotRed);
						labelApproachedDot.setIcon(imgDotRed);
					break;
				case NanoComm.STATE_STAGEMOVING:
				case NanoComm.STATE_APPROACHING:
						labelMovingDot.setIcon(imgDotGreen);
						labelApproachedDot.setIcon(imgDotRed);
					break;
				case NanoComm.STATE_APPROACHED:
						labelMovingDot.setIcon(imgDotRed);
						labelApproachedDot.setIcon(imgDotGreen);
					break;
				case NanoComm.STATE_WITHDRAWING:
				case NanoComm.STATE_SCANNING:
						labelMovingDot.setIcon(imgDotGreen);
						labelApproachedDot.setIcon(imgDotGreen);
			}
		} catch (NumberFormatException e){}
	}
	@Override public void setInfo(String info){
		try{
			int val = Integer.parseInt(info.substring(NanoComm.COMMAND_INFO.length() + 1));
			if(isInitialized) switch(val){
				case NanoComm.INFO_SERVER_CONNECTED:
					setServerConnected(true);
					break;
				case NanoComm.INFO_SERVER_DISCONNECTED:
					setServerConnected(false);
					break;
				case NanoComm.INFO_REMEXP_CONNECTED:
					setRemExpConnected(true);
					break;
				case NanoComm.INFO_REMEXP_DISCONNECTED:
					setRemExpConnected(false);
			}
		} catch(NumberFormatException e){}
	}
	@Override public void setParameter(String param){
		int cmd = Integer.parseInt(Parser.getValue(param, NanoComm.COMMAND_PARAM));
		String val = Parser.getValue(param, "value");
		switch(cmd){
			case NanoComm.PARAM_REMEXPNAME:
				labelRemExpTitle.setText(val);
		}
	}
	
	/**
	 * Processes a label and the connectivity information.
	 * 
	 * @param label			the label that reflects either connected or disconnected
	 * @param isConnected	true if connected, else false
	 */
	private void setLabelConnected(JLabel label, boolean isConnected){
		if(isConnected){
			removeLabel(label);
		} else {
			addLabel(label);
			if(blinkerThread == null) blinkerThread = new Blinker();
		}
	}

	/**
	 * Adds a label to the list of blinking labels.
	 * 
	 * @param lbl	the label to be added
	 */
	private void addLabel(JLabel lbl){
		if(!arrBlinkingLabel.contains(lbl))	arrBlinkingLabel.add(lbl);
	}
	
	/**
	 * Removes a label from the list of blinking labels.
	 * 
	 * @param lbl	the label to be removed
	 */
	private void removeLabel(JLabel lbl){
		arrBlinkingLabel.remove(lbl);
		lbl.setIcon(imgPlugGreen);
	}
	
	/**
	 * Checks whether the server and remote experiment are connected.
	 */
	protected void checkConnection(){
		if(isServerConnected) setServerConnected(true);
		if(isRemExpConnected) setRemExpConnected(true);
	}
	
	/**
	 * This function is used to set the information about
	 * the remote experiment broadcaster connection.
	 *  
	 * @param isConnected	true if it is connected, else false
	 */
	protected void setServerConnected(boolean isConnected){
		isServerConnected = isConnected;
		setLabelConnected(labelServerPlug, isConnected);
		if(!isConnected) setRemExpConnected(false);
	}
	
	/**
	 * This function is used to set the information about
	 * the remote experiment connection.
	 *  
	 * @param isConnected	true if it is connected, else false
	 */
	private void setRemExpConnected(boolean isConnected){
		isRemExpConnected = isConnected;
		setLabelConnected(labelRemExpPlug, isConnected);
		if(!isRemExpConnected){
			labelMovingDot.setIcon(imgDotRed);
			labelApproachedDot.setIcon(imgDotRed);
		}
	}
	
	/**
	 * A thread that is used to make the connectivity icons blink.
	 * 
	 * @author Dominic Bosch
	 * @version 1.1 29.08.2012 
	 */
	private class Blinker extends ThreadHandler{
		private boolean terminationFlag = false;
		
		private Blinker(){super.start(this.getClass().getSimpleName());}
		
		@Override public void doTask() {
			int i = 0;
			int size = arrStateImg.size();
			if(isInitialized){
				if(arrBlinkingLabel.size() == 0) this.shutDown();
				else {
					for(ImageIcon stateIco: arrStateImg){
						synchronized(arrBlinkingLabel){
							for(JLabel label: arrBlinkingLabel) label.setIcon(stateIco);
						}
						if(++i < size){
							try{
								Thread.sleep(400);
							} catch (InterruptedException e) {}
						}
					}
				}
				checkConnection();
			}
			if(terminationFlag) doTerminationActions();
		}
		
		@Override public void shutDown() {
			stopThread();
			blinkerThread = null;
		}
	
	}
	@Override public void terminate() {
		if(blinkerThread==null) doTerminationActions();
		else blinkerThread.terminationFlag = true;
	}
	
	private void doTerminationActions(){
		gui = null;
		size = null;
		if(blinkerThread != null) blinkerThread.shutDown();
		labelRemExpTitle = null;
		labelRemExpPlug = null;
		labelServerPlug = null;
		labelMovingDot = null;
		labelApproachedDot = null;
		imgPlugGreen = null;
		imgPlugRedZero = null;
		imgPlugRedOne = null;
		imgPlugRedTwo = null;
		imgPlugRedThree = null;
		imgDotGreen = null;
		imgDotRed = null;
		if(arrBlinkingLabel != null) arrBlinkingLabel.clear();
		arrBlinkingLabel = null;
		if(arrStateImg != null) arrStateImg.clear();
		arrStateImg = null;
	}
}

