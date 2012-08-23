/*
 * Copyright (c) 2011 by Tibor Gyalog, Raoul Schneider, Dino Keller,
 * Christian Wattinger, Martin Guggisberg, Dominic Bosch and The 
 * Regents of the University of Basel. All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this software and its
 * documentation for any purpose, without fee, and without written agreement is
 * hereby granted, provided that the above copyright notice and the following
 * two paragraphs appear in all copies of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF BASEL BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * BASEL HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE UNIVERSITY OF BASEL SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE.  THE SOFTWARE PROVIDED HEREUNDER IS
 * ON AN "AS IS" BASIS, AND THE UNIVERSITY OF BASEL HAS NO OBLIGATION TO
 * PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 * Authors: Tibor Gyalog, Raoul Schneider, Dino Keller,
 * Christian Wattinger, Martin Guggisberg, Dominic Bosch <vexp@nano-world.net>
 * 
 */ 

package nano.remexp.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import Jama.Matrix;

import nano.debugger.Debg;
import nano.remexp.Parser;
import nano.remexp.ThreadHandler;
import nano.remexp.client.awt.LineSection;
import nano.remexp.client.awt.NetObserver;
import nano.remexp.net.NanoComm;


/**
 * This class generates the graphical user interface and handles events coming from
 * user inputs. Those are passed to the main applet class for further processing.
 * 
 * @author Dominic Bosch
 * @version 2.0 26.10.2011
 */
public class ClientGUI {
	public static final int APPROX_SCANTIME = 495;
	public static final int SCANSIZE = 256;
	public static final int MATRIX_SIZE = 20;
	public static final int NUM_OF_FIXPOINTS = 8;// Can't exceed MATRIX_SIZE
	
	private static final boolean ENABLE_DEBUG = false;
	private static final int H_BUT = 23; // height of a button
	private static final int N_BUT = 4; // max number of buttons in one column
	
	private ClientApplet actionListener;
	private ProgressUpdater progressBarUpdater;
	private Blinker blinkerThread;
	private Vector<JLabel> arrBlinkingLabel;
	private Vector<ImageIcon> arrStateImg;
	private JPanel panelAdmin;
	private JPanel panelControls;
	private JPanel panelStatus;
	private JPanel panelButtonsLeft;
	private JButton buttonApproach;
	private JButton buttonStart;
	private JButton buttonStopStage;
	private JButton buttonWithdraw;
	private JButton buttonStop;
	private JButton buttonLogin;
	private JButton buttonCamAngle;
	private JTextField userInput;
	private JTextPane statusLine;
	private JScrollPane scrollPane;
	private JProgressBar progressBar;
	private JComboBox comboBoxScanRange;
	private JComboBox comboBoxSample;
	private DefaultComboBoxModel comboBoxSampleModel;
	private JLabel labelRemExpTitle;
	private JLabel labelRemExpDot;
	private JLabel labelServerDot;
	private JLabel labelMovingDot;
	private JLabel labelApproachedDot;
	private JLabel labelArrowUp;
	private JLabel labelArrowDown;
	private JLabel labelArrowLeft;
	private JLabel labelArrowRight;
	private ImageIcon imgPlugGreen;
	private ImageIcon imgPlugRedZero;
	private ImageIcon imgPlugRedOne;
	private ImageIcon imgPlugRedTwo;
	private ImageIcon imgPlugRedThree;
	private ImageIcon imgDotGreen;
	private ImageIcon imgDotRed;
	private ImageIcon imgDotDarkRed;
	private ImageIcon imgArrowUp;
	private ImageIcon imgArrowLeft;
	private ImageIcon imgArrowDown;
	private ImageIcon imgArrowRight;
	private ImageIcon imgArrowUpInactive;
	private ImageIcon imgArrowLeftInactive;
	private ImageIcon imgArrowDownInactive;
	private ImageIcon imgArrowRightInactive;
	private Dimension dimStatus;
	private Dimension dimStatusLine;
	private Dimension dimControls;

	private Vector<Sample> samples;
	private Vector<String> listSampleNames;
	private long scanStarted;
	private Date lastChange;
	private int currentState;
	private int currentScanRange;
	private int currentSample;
	private int oldLine;
	private Matrix matrixX;
	private Matrix matrixY;
	private double m;
	private boolean isInitialized;
	private boolean isServerConnected;
	private boolean isRemExpConnected;
	private boolean mayShowControls;
	
	/**
	 * The constructor for this object that links the action listener.
	 * 
	 * @param boss The main applet object that needs to be informed about user inputs.
	 */
	protected ClientGUI(ClientApplet boss){
		arrBlinkingLabel = new Vector<JLabel>();
		arrStateImg = new Vector<ImageIcon>();
		imgDotRed = new ImageIcon(getClass().getResource(ClientApplet.IMAGE_DIR + "dotRed.jpg"));
		imgDotDarkRed = new ImageIcon(getClass().getResource(ClientApplet.IMAGE_DIR + "dotDarkRed.jpg"));
		imgDotGreen = new ImageIcon(getClass().getResource(ClientApplet.IMAGE_DIR + "dotGreen.jpg"));
		imgPlugGreen = new ImageIcon(getClass().getResource(ClientApplet.IMAGE_DIR + "plug_col_connected_small.jpg"));
		imgPlugRedZero = new ImageIcon(getClass().getResource(ClientApplet.IMAGE_DIR + "plug_col_0_small.jpg"));
		imgPlugRedOne = new ImageIcon(getClass().getResource(ClientApplet.IMAGE_DIR + "plug_col_1_small.jpg"));
		imgPlugRedTwo = new ImageIcon(getClass().getResource(ClientApplet.IMAGE_DIR + "plug_col_2_small.jpg"));
		imgPlugRedThree = new ImageIcon(getClass().getResource(ClientApplet.IMAGE_DIR + "plug_col_3_small.jpg"));
		arrStateImg.add(imgPlugRedZero);
		arrStateImg.add(imgPlugRedOne);
		arrStateImg.add(imgPlugRedTwo);
		arrStateImg.add(imgPlugRedThree);
		mayShowControls = false;
        isServerConnected = false;
        isRemExpConnected = false;
		lastChange = null;
		oldLine = 0;
		isInitialized = false;
		scanStarted = 0;
		currentState = NanoComm.STATE_STAGECALIBRATED;
		currentScanRange = 10;
		currentSample = -1;
		actionListener = boss;
		matrixX = new Matrix(MATRIX_SIZE, 1);
		matrixY = new Matrix(MATRIX_SIZE, 1);
		m = 2;
		samples = new Vector<Sample>();
		listSampleNames = new Vector<String>();
		addCalibrateCommand();
		comboBoxSampleModel = new DefaultComboBoxModel(listSampleNames);
		Debg.print("init GUI finished");
	}
	
	/**
	 * Adds the calibrate command to the drop down box.
	 */
	private void addCalibrateCommand(){
		samples.add(new Sample(-1, "Calibrate", "calibratestage"));
		listSampleNames.add("Calibrate");
	}
	
	/**
	 * Initializes the panel containing both graphs, the whole scan image and the line scan. 
	 * 
	 * @param myImage Where the whole image is displayed.
	 * @param myLiner Where the last scanned line is displayed.
	 * @return The panel containing the two displays.
	 */
	protected JPanel setupGraphs(NetObserver myImage, LineSection myLiner){
		JPanel panelGraphs = new JPanel();
		setupComponent(panelGraphs, new Dimension(540, 280), Color.black);
		progressBar = new JProgressBar(0, SCANSIZE);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		setupComponent(progressBar, new Dimension(530, 20), Color.cyan);
		progressBarUpdater = new ProgressUpdater();
		progressBarUpdater.start("ProgressBarUpdater");
		progressBarUpdater.doWait(true);

		panelGraphs.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		panelGraphs.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		panelGraphs.add(myImage);
		panelGraphs.add(myLiner);
		panelGraphs.add(progressBar);
		return panelGraphs;
	}

	/**
	 * Initializes the controls panel which is divided in a left and a right section.
	 * 
	 * @return The panel holding a left and a right panel, both filled with controls.
	 */
	protected JPanel setupControls(){
		panelControls = new JPanel();
		JPanel panelControlsTop = new JPanel();
		JPanel panelControlsRight = new JPanel();
		JPanel panelControlsLeft = new JPanel();
		dimControls = new Dimension(540, 165);
		setupComponent(panelControls, dimControls, Color.black);
		panelControls.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		panelControls.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
		setupComponent(panelControlsTop, new Dimension(520, 20), Color.yellow);
		setupComponent(panelControlsRight, new Dimension(260, 135), Color.red);
		setupComponent(panelControlsLeft, new Dimension(260, 135), Color.green);

		setupTopControls(panelControlsTop);
		panelControls.add(panelControlsTop);
		setupLeftControls(panelControlsLeft);
		panelControls.add(panelControlsLeft);
		setupRightControls(panelControlsRight);
		panelControls.add(panelControlsRight);
		setupAdminControls();
		return panelControls;
	}
	
	protected void setupAdminControls(){
		panelAdmin = new JPanel();
		JLabel cmdLabel = new JLabel("Command: ");
		userInput = new JTextField();
		panelAdmin.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		setupComponent(panelAdmin, new Dimension(520, 25), Color.blue);
		setupComponent(cmdLabel, new Dimension(70, 25), Color.blue);
		setupComponent(userInput, new Dimension(440, 25), Color.blue);
		panelAdmin.add(cmdLabel);
		panelAdmin.add(userInput);
		userInput.addKeyListener(new KeyListener(){
			@Override public void keyPressed(KeyEvent arg0) {
				if(arg0.getKeyCode() == KeyEvent.VK_ENTER){
					actionListener.sendAdminCommand("command=" + userInput.getText());
					userInput.setText("");
				}
			}
			@Override public void keyReleased(KeyEvent arg0) {}
			@Override public void keyTyped(KeyEvent arg0) {}
		});
	}
	
	/**
	 * Initializes the status line that displays outputs from the remote experiment.
	 * 
	 * @return The panel containing the status line textbox.
	 */
	protected JPanel setupStatusLine(){
		// Background picture tried...
		//PatientPanel panelStatus = new PatientPanel();
		/*try {
			panelStatus.setImage(ImageIO.read(getClass().getResource("nano_logo.jpg")));
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		panelStatus = new JPanel();
		dimStatus = new Dimension(540, 65);
		setupComponent(panelStatus, dimStatus, Color.magenta);
		this.setupStatusLine(panelStatus);
		return panelStatus;
	}
	
	protected void setState(int state){
		boolean isValidState = true;
		switch(state){
			case NanoComm.STATE_STAGECALIBRATED:
				if(isInitialized) {
					setControlsEnabled(false);
					if(mayShowControls){
						buttonCamAngle.setEnabled(true);
						comboBoxScanRange.setEnabled(true);
						comboBoxSample.setEnabled(true); //TODO check scan range, when can we give this command, reflect it in the gui
					}
					labelMovingDot.setIcon(imgDotRed);
					labelApproachedDot.setIcon(imgDotRed);
				}
				break;
			case NanoComm.STATE_STAGEREADY:
				if(isInitialized) {
					setControlsEnabled(false);
					if(mayShowControls){
						buttonApproach.setEnabled(true);
						buttonCamAngle.setEnabled(true);
						comboBoxScanRange.setEnabled(true);
						comboBoxSample.setEnabled(true);
						setArrowsVisible(true);
					}
					labelMovingDot.setIcon(imgDotRed);
					labelApproachedDot.setIcon(imgDotRed);
				}
				break;
			case NanoComm.STATE_STAGEMOVING:
				if(isInitialized) {
					setControlsEnabled(false);
					labelMovingDot.setIcon(imgDotGreen);
					labelApproachedDot.setIcon(imgDotRed);
				}
				break;
			case NanoComm.STATE_APPROACHING:
				if(isInitialized) {
					setControlsEnabled(false);
					if(mayShowControls){
						buttonStopStage.setEnabled(true);
						buttonCamAngle.setEnabled(true);
					}
					labelMovingDot.setIcon(imgDotGreen);
					labelApproachedDot.setIcon(imgDotRed);
				}
				break;
			case NanoComm.STATE_APPROACHED:
				if(isInitialized) {
					progressBarUpdater.doWait(true);
					setControlsEnabled(false);
					if(mayShowControls){
						buttonStart.setEnabled(true);
						buttonWithdraw.setEnabled(true);
						buttonCamAngle.setEnabled(true);
						comboBoxScanRange.setEnabled(true);
					}
					labelMovingDot.setIcon(imgDotRed);
					labelApproachedDot.setIcon(imgDotGreen);
				}
				scanStarted = 0;
				lastChange = null;
				oldLine = 0;
				break;
			case NanoComm.STATE_WITHDRAWING:
				if(isInitialized) {
					setControlsEnabled(false);
					labelMovingDot.setIcon(imgDotGreen);
					labelApproachedDot.setIcon(imgDotGreen);
				}
				break;
			case NanoComm.STATE_SCANNING:
				if(isInitialized) {
					progressBarUpdater.doWait(false);
					synchronized(actionListener){
						actionListener.notify();
					}
					setControlsEnabled(false);
					if(mayShowControls){//TODO move cam angle button into state independent solution
						buttonCamAngle.setEnabled(true);
						buttonStop.setEnabled(true);// was not enabled, maybe nt broadcasted?
					}
					labelMovingDot.setIcon(imgDotGreen);
					labelApproachedDot.setIcon(imgDotGreen);
				}
				initLeastSquaresCalc();
				break;
			default:
				isValidState = false;
		}
		if(isValidState) currentState = state;
	}
	
	protected void setInfo(int info){
		switch(info){
			case NanoComm.INFO_REMEXP_CONNECTED:
				setRemExpConnected(true);
				break;
			case NanoComm.INFO_REMEXP_DISCONNECTED:
				setRemExpConnected(false);
				break;
		}
	}

	protected synchronized void setParameter(String event){
		try {
			int cmd = Integer.parseInt(Parser.getValue(event, NanoComm.COMMAND_PARAM));
			String val = Parser.getValue(event, "value");
			switch(cmd){
				case NanoComm.PARAM_REMEXPNAME:
					labelRemExpTitle.setText(val);
					break;
				case  NanoComm.PARAM_SCANRANGE:
					if(isInitialized) setScanRange(Integer.parseInt(val));
					else currentScanRange = Integer.parseInt(val);
					break;
				/* there is yet no information about the camera angle of the microscope
				 * case  NanoComm.PARAM_CAMANGLE:
				 *
				 *	if(isInitialized) setCamAngle(Integer.parseInt(val));
				 *	else currentCamAngle = Integer.parseInt(val);
				 *	break;
				 */
				case NanoComm.PARAM_STAGEPOSITION:
					if(isInitialized) setSample(Integer.parseInt(val));
					else currentSample = Integer.parseInt(val);
					break;
				case NanoComm.PARAM_SCANSTART:
					scanStarted = Long.parseLong(val);
					break;
				case NanoComm.PARAM_SAMPLEINFO:
					try{
						String name = Parser.getValue(event, "name");
						String samplecmd = Parser.getValue(event, NanoComm.COMMAND_CMD);
						samples.add(new Sample(Integer.parseInt(val), name, samplecmd));
						comboBoxSampleModel.addElement(name);
					} catch(Exception e){Debg.err("Unable to add sample!");}
					break;
				case NanoComm.PARAM_SAMPLESCLEAR:
					samples.clear();
					comboBoxSampleModel.removeAllElements();
					addCalibrateCommand();
					break;
				default:
					Debg.err("Parameters not handled");
			}
		} catch(Exception e){
			Debg.err("unable to parse parameter: " + event);
		}
	}
	
	protected void setPrivilege(int priv){
		switch(priv){
			case NanoComm.PRIV_ADMIN:
				Debg.print("received admin privilege");
				if(isInitialized) {
					mayShowControls = true;
					setAdminVisibility(true);
				}
				break;
			case NanoComm.PRIV_CONTROLLER:
				Debg.print("received controller privilege");
				if(isInitialized) {
					mayShowControls = true;
					setAdminVisibility(false);
				}
				break;
			case NanoComm.PRIV_OBSERVER:
				Debg.print("received observer privilege");
				if(isInitialized) {
					mayShowControls = false;
					setAdminVisibility(false);
				}
				break;
			default:
				Debg.err("Privilege not handled");
		}
	}

	private void initLeastSquaresCalc(){
		for(int i = 0; i < matrixX.getRowDimension(); i++){
			if(i % 2 == 0){
				matrixX.set(i, 0, 0);
				matrixY.set(i, 0, 0);
			} else {
				matrixX.set(i, 0, SCANSIZE);
				matrixY.set(i, 0, APPROX_SCANTIME);
			}
		}
	}
	
	private void updateProgress(int currLine){
		if(isInitialized){
			if(scanStarted == 0) {
				progressBar.setString("0:00");
				progressBar.setValue(0);
			} else {
				progressBar.setValue(currLine);
				if(currLine != oldLine) {
					// Keeping NUM_OF_FIXPOINTS init values since we trust them more
					// than the inaccurate intermediate reports,
					// except for the last six lines where we come to the end. 
					int len = NUM_OF_FIXPOINTS;
					if(SCANSIZE - currLine < len) len = SCANSIZE - currLine;
					for(int i = len; i < matrixX.getRowDimension() - 1; i ++){
						matrixX.set(i, 0, matrixX.get(i + 1, 0));
						matrixY.set(i, 0, matrixY.get(i + 1, 0));
					}
					matrixX.set(matrixX.getRowDimension() - 1, 0, currLine);
					matrixY.set(matrixY.getRowDimension() - 1, 0, (new Date().getTime() / 1000) - scanStarted);
					calculateLeastSquares();
					oldLine = currLine;
					lastChange = new Date();
				}
				if(lastChange != null){
					long now = new Date().getTime();
					long diff = now - lastChange.getTime(); 
					if(diff < 10000){
						long seconds = (long) (m * SCANSIZE - (now / 1000) + scanStarted);
						int mins = (int) (seconds / 60);
						seconds = seconds % 60;
						String secs = "";
						if(seconds < 0) secs = "00";
						else if(seconds < 10) secs = "0" + seconds;
						else secs += seconds;
						progressBar.setString(mins + ":" + secs);
					} else {
						progressBar.setString("0:00");
						progressBar.setValue(0);
					}
				}
			}
		}
	}
	
	private void calculateLeastSquares(){
		Matrix aTrans = matrixX.transpose();
		Matrix x = (aTrans.times(matrixX)).inverse().times(aTrans).times(matrixY);
		m = x.get(0, 0);
	}
	
	/**
	 * This method is important since only by setting the gui to initialized it will be fully functional.
	 * 
	 * @param init true or false whether the gui is ready.
	 */
	protected void setInitialized(boolean init){
		isInitialized = init;
		if(init){
			setState(currentState);
			setScanRange(currentScanRange);
			setSample(currentSample);
			updateProgress(0);
		}
	}
	
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
	
	private synchronized void setSample(int sample){
		Object entry;
		currentSample = sample;
		String name = null;
		for(Sample s: samples) if(sample == s.getID()) name = s.getName();
		
		if(name == null) name = "Calibrate";
		for (int i = 0; i < comboBoxSample.getItemCount(); i++) {
			entry = comboBoxSample.getItemAt(i);
		    if(entry.equals(name)) {
		    	comboBoxSample.setSelectedItem(entry);
		    }
		}
	}
	
	protected synchronized String getSampleCommand(String name){
		Sample s;
		for(int i = 0; i < samples.size(); i++) {
			s = samples.get(i);
			if(name.equals(s.getName())) return s.getCommand();
		}
		return "";
	}
	
	private void setControlsEnabled(boolean visible){
		buttonApproach.setEnabled(visible);
		buttonStart.setEnabled(visible);
		buttonStopStage.setEnabled(visible);
		buttonWithdraw.setEnabled(visible);
		buttonStop.setEnabled(visible);
		buttonCamAngle.setEnabled(visible);
		comboBoxScanRange.setEnabled(visible);
		comboBoxSample.setEnabled(visible);
		setArrowsVisible(visible);
	}
		
	private void setArrowsVisible(boolean visible){
		if(visible){
			labelArrowUp.setIcon(imgArrowUp);
			labelArrowLeft.setIcon(imgArrowLeft);
			labelArrowDown.setIcon(imgArrowDown);
			labelArrowRight.setIcon(imgArrowRight);
		} else {
			labelArrowUp.setIcon(imgArrowUpInactive);
			labelArrowLeft.setIcon(imgArrowLeftInactive);
			labelArrowDown.setIcon(imgArrowDownInactive);
			labelArrowRight.setIcon(imgArrowRightInactive);
		}
	}
	
	private void setAdminVisibility(boolean isVisible){
		if(isVisible){
			setupComponent(panelControls, new Dimension(dimControls.width, dimControls.height + 25), Color.black);
			panelControls.add(panelAdmin);
		} else {
			setupComponent(panelControls, dimControls, Color.black);
			panelControls.remove(panelAdmin);
		}
		panelControls.revalidate();
		panelControls.repaint();
		//setupComponent(panelStatus, dimStatus);
		setupComponent(scrollPane, dimStatusLine);
		panelStatus.repaint();
	}

	/**
	 * setting up the top controls.
	 * 
	 * @param topPanel The panel onto which the controls are added.
	 */
	private void setupTopControls(JPanel topPanel){
		topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		JPanel panelServer = new JPanel();
		setupComponent(panelServer, new Dimension(80, 18), Color.cyan);
		panelServer.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		JLabel labelServerTitle = new JLabel();
		labelServerTitle.setBackground(Color.white);
		labelServerTitle.setText("Server");
		labelServerDot = new JLabel();
		labelServerDot.setBackground(Color.white);
		labelServerDot.setIcon(imgPlugRedOne);
		labelServerDot.setName("Server");
		panelServer.add(labelServerDot);
		panelServer.add(labelServerTitle);

		JPanel panelRemExp = new JPanel();
		setupComponent(panelRemExp, new Dimension(120, 18), Color.cyan);
		panelRemExp.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		labelRemExpTitle = new JLabel();
		labelRemExpTitle.setBackground(Color.white);
		labelRemExpTitle.setText("RemExp");
		labelRemExpDot = new JLabel();
		labelRemExpDot.setBackground(Color.white);
		labelRemExpDot.setIcon(imgPlugRedOne);
		labelRemExpDot.setName("RemExp");
		panelRemExp.add(labelRemExpDot);
		panelRemExp.add(labelRemExpTitle);
		
		JPanel panelApproach = new JPanel();
		setupComponent(panelApproach, new Dimension(100, 18), Color.cyan);
		panelApproach.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		JLabel labelApproachedTitle = new JLabel();
		labelApproachedTitle.setBackground(Color.white);
		labelApproachedTitle.setText("Approached");
		labelApproachedDot = new JLabel();
		labelApproachedDot.setBackground(Color.white);
		labelApproachedDot.setIcon(imgDotRed);
		panelApproach.add(labelApproachedDot);
		panelApproach.add(labelApproachedTitle);

		JPanel panelMoving = new JPanel();
		setupComponent(panelMoving, new Dimension(80, 18), Color.YELLOW);
		panelMoving.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		JLabel labelMovingTitle = new JLabel();
		labelMovingTitle.setBackground(Color.white);
		labelMovingTitle.setText("Moving");
		labelMovingDot = new JLabel();
		labelMovingDot.setBackground(Color.white);
		labelMovingDot.setIcon(imgDotRed);
		panelMoving.add(labelMovingDot);
		panelMoving.add(labelMovingTitle);

		topPanel.add(panelServer);
		topPanel.add(panelRemExp);
		topPanel.add(panelMoving);
		topPanel.add(panelApproach);
		if(!isServerConnected) setServerConnected(false);
	}
	
	/**
	 * Setting up the controls on the left side and adding them to the according panel
	 * 
	 * @param topPanel The panel on which the controls are being fitted.
	 */
	private void setupLeftControls(JPanel topPanel){
		topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
		panelButtonsLeft = new JPanel();
		JPanel panelButtonsRight = new JPanel();

		setupComponent(panelButtonsLeft, new Dimension(120, (H_BUT + 2) * N_BUT), Color.DARK_GRAY);
		setupComponent(panelButtonsRight, new Dimension(120, (H_BUT + 2) * N_BUT), Color.magenta);
		
		Dimension buttonDim = new Dimension(100, H_BUT);

/*
 * Setup Top controls
 */
		JPanel panelScanRange = new JPanel();
		JLabel labelScanRangeTitle = new JLabel();
		comboBoxScanRange = new JComboBox();

		panelScanRange.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 2));
		setupComponent(panelScanRange, new Dimension(240, H_BUT + 7), Color.BLUE);

		setupComponent(labelScanRangeTitle, new Dimension(110, H_BUT));
		labelScanRangeTitle.setBackground(Color.white);
		labelScanRangeTitle.setText("Scan Range:");

		setupComponent(comboBoxScanRange, new Dimension(100, H_BUT));
		comboBoxScanRange.setModel(new DefaultComboBoxModel(new String[] { "1 um", "5 um", "10 um", "25 um", "50 um" }));
		comboBoxScanRange.setSelectedIndex(2);
		comboBoxScanRange.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				actionListener.comboBoxScanRangeActionPerformed(evt);
			}
		});
		
		panelScanRange.add(labelScanRangeTitle);
		panelScanRange.add(comboBoxScanRange);
		actionListener.setScanRangeComboBox(comboBoxScanRange);

		topPanel.add(panelScanRange);

/*
 * Setup Bottom controls
 */
	/*
	 * Setup Left Controls
	 */
		buttonApproach = new JButton();
		buttonStart = new JButton();
		JButton buttonSave = new JButton();
		buttonLogin = new JButton();
		
		setupComponent(buttonApproach, buttonDim);
		setupComponent(buttonStart, buttonDim);
		setupComponent(buttonSave, buttonDim);
		setupComponent(buttonLogin, buttonDim);
		
		//buttonApproach.setBackground(Color.white);
		buttonApproach.setText("Approach");
		buttonApproach.setEnabled(false);
		buttonApproach.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				actionListener.buttonApproachActionPerformed(evt);
			}
		});

		//buttonStart.setBackground(Color.white);
		buttonStart.setText("Start Scan");
		buttonStart.setEnabled(false);
		buttonStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				actionListener.buttonStartActionPerformed(evt);
			}
		});

		//buttonSave.setBackground(Color.white);
		buttonSave.setText("Save Scan");
		buttonSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				actionListener.buttonSaveActionPerformed(evt);
			}
		});
		
		buttonLogin.setText("Login");
		buttonLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				actionListener.buttonLoginActionPerformed(evt);
			}
		});
		
		panelButtonsLeft.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		panelButtonsLeft.add(buttonApproach);
		panelButtonsLeft.add(buttonStart);
		panelButtonsLeft.add(buttonSave);
		panelButtonsLeft.add(buttonLogin);

	/*
	 * Setup Right Controls
	 */
		buttonStopStage = new JButton();
		buttonWithdraw = new JButton();
		buttonStop = new JButton();
		buttonCamAngle = new JButton();

		setupComponent(buttonStopStage, buttonDim);
		setupComponent(buttonStop, buttonDim);
		setupComponent(buttonWithdraw, buttonDim);
		setupComponent(buttonCamAngle, buttonDim);

		//buttonStop.setBackground(Color.white);
		buttonStopStage.setText("Stop Appr.");
		buttonStopStage.setEnabled(false);
		buttonStopStage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				actionListener.buttonStopStageActionPerformed(evt);
			}
		});
		
		//buttonWithdraw.setBackground(Color.white);
		buttonWithdraw.setText("Withdraw");
		buttonWithdraw.setEnabled(false);
		buttonWithdraw.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				actionListener.buttonWithdrawActionPerformed(evt);
			}
		});

		//buttonStop.setBackground(Color.white);
		buttonStop.setText("Stop Scan");
		buttonStop.setEnabled(false);
		buttonStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				actionListener.buttonStopActionPerformed(evt);
			}
		});
		
		buttonCamAngle.setText("Cam angle");
		buttonCamAngle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				actionListener.buttonCamAngleActionPerformed(evt);
			}
		});
		
		panelButtonsRight.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		panelButtonsRight.add(buttonStopStage);
		panelButtonsRight.add(buttonStop);
		panelButtonsRight.add(buttonWithdraw);
		panelButtonsRight.add(buttonCamAngle);

	/*
	 * Combine all elements
	 */
		topPanel.add(panelButtonsLeft);
		topPanel.add(panelButtonsRight);
	}

	/**
	 * Sets up the controls on the right side. The sample chooser and the arrows
	 * that allow the user to move on the sample.
	 * 
	 * @param topPanel The panel containing the controls on the right side.
	 */
	private void setupRightControls(JPanel topPanel){
		JPanel panelSample = new JPanel();
		JLabel labelSample = new JLabel();
		comboBoxSample = new JComboBox();
		JPanel panelPosition = new JPanel();
		JPanel panelArrows = new JPanel();
		JLabel labelArrows = new JLabel();
		JPanel panelLowerControls = new JPanel();
		//JPanel panelLowerRightControls = new JPanel();
		
		topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		setupComponent(panelSample, new Dimension(250, H_BUT + 7), Color.blue);
		setupComponent(panelPosition, new Dimension(110, 100), Color.gray);
		setupComponent(panelArrows, new Dimension(75, 75), Color.magenta);
		setupComponent(panelLowerControls, new Dimension(250, 120), Color.green);
		//setupComponent(panelLowerRightControls, new Dimension(130, 100), Color.yellow);
		//panelLowerRightControls.setBorder(BorderFactory.createLineBorder(Color.black));
		setupComponent(comboBoxSample, new Dimension(100, H_BUT));
		comboBoxSample.setModel(comboBoxSampleModel);
		comboBoxSample.setSelectedIndex(0);
		comboBoxSample.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				actionListener.comboBoxSampleActionPerformed(evt);
			}
		});
		actionListener.setSampleComboBox(comboBoxSample);
		panelSample.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 2));
		labelSample.setText("Position:    ");
		panelSample.add(labelSample);
		panelSample.add(comboBoxSample);
		panelLowerControls.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
		panelPosition.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		//SimpleLine sl = new SimpleLine(100, 100);
		labelArrows.setText("Adjust Position:");
		setupComponent(labelArrows, new Dimension(100, H_BUT));
		setupArrows(panelArrows);
		
		topPanel.add(panelSample);
		panelPosition.add(labelArrows);
		panelPosition.add(panelArrows);
		panelLowerControls.add(panelPosition);
		//panelLowerControls.add(sl);
		//panelLowerControls.add(panelLowerRightControls);
		topPanel.add(panelLowerControls);
	}
	
	/**
	 * Setting up the direction arrows box that enables the user
	 * to change the position on the sample.
	 * 
	 * @param topPanel The panel on which the controls are being fitted.
	 */
	private void setupArrows(JPanel topPanel){
		imgArrowUp = new ImageIcon(getClass().getResource(ClientApplet.IMAGE_DIR + "arrow-up.jpg"));
		imgArrowLeft = new ImageIcon(getClass().getResource(ClientApplet.IMAGE_DIR + "arrow-left.jpg"));
		imgArrowDown = new ImageIcon(getClass().getResource(ClientApplet.IMAGE_DIR + "arrow-down.jpg"));
		imgArrowRight = new ImageIcon(getClass().getResource(ClientApplet.IMAGE_DIR + "arrow-right.jpg"));
		imgArrowUpInactive = new ImageIcon(getClass().getResource(ClientApplet.IMAGE_DIR + "arrow-up_inactive.jpg"));
		imgArrowLeftInactive = new ImageIcon(getClass().getResource(ClientApplet.IMAGE_DIR + "arrow-left_inactive.jpg")); 
		imgArrowDownInactive = new ImageIcon(getClass().getResource(ClientApplet.IMAGE_DIR + "arrow-down_inactive.jpg"));
		imgArrowRightInactive = new ImageIcon(getClass().getResource(ClientApplet.IMAGE_DIR + "arrow-right_inactive.jpg"));
		labelArrowUp = new JLabel();
		labelArrowDown = new JLabel();
		labelArrowLeft = new JLabel();
		labelArrowRight = new JLabel();
		
		labelArrowUp.setBackground(Color.WHITE);
		labelArrowUp.setIcon(imgArrowUpInactive);
		labelArrowUp.addMouseListener(new MouseAdapter() {  
			public void mouseReleased(MouseEvent evt){  
				actionListener.labelArrowUpActionPerformed(evt);
			}  
		});
		
		labelArrowLeft.setBackground(Color.WHITE);
		labelArrowLeft.setIcon(imgArrowLeftInactive);
		labelArrowLeft.addMouseListener(new MouseAdapter() {  
			public void mouseReleased(MouseEvent evt){  
				actionListener.labelArrowLeftActionPerformed(evt);
			}
		});
		
		labelArrowRight.setBackground(Color.WHITE);
		labelArrowRight.setIcon(imgArrowRightInactive);
		labelArrowRight.addMouseListener(new MouseAdapter() {  
			public void mouseReleased(MouseEvent evt){  
				actionListener.labelArrowRightActionPerformed(evt);
			}
		});
		
		labelArrowDown.setBackground(Color.WHITE);
		labelArrowDown.setIcon(imgArrowDownInactive);
		labelArrowDown.addMouseListener(new MouseAdapter() {  
			public void mouseReleased(MouseEvent evt){  
				actionListener.labelArrowDownActionPerformed(evt);
			}
		});
		
		topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		topPanel.add(createBlankArrow());
		topPanel.add(labelArrowUp);
		topPanel.add(createBlankArrow());
		topPanel.add(labelArrowLeft);
		topPanel.add(createBlankArrow());
		topPanel.add(labelArrowRight);
		topPanel.add(createBlankArrow());
		topPanel.add(labelArrowDown);
	}
	
	/**
	 * The status line to inform the user about events.
	 * 
	 * @param topPanel The panel on which the info box is fitted.
	 */
	private void setupStatusLine(JPanel topPanel){
		Style style;
		StyleContext context = new StyleContext();
		StyledDocument document = new DefaultStyledDocument(context);
		
		style = context.getStyle(StyleContext.DEFAULT_STYLE);
		StyleConstants.setAlignment(style, StyleConstants.ALIGN_LEFT);
		StyleConstants.setFontSize(style, 10);
		StyleConstants.setSpaceAbove(style, 1);
		StyleConstants.setSpaceBelow(style, 1);
		actionListener.setStatusStyle(style);

		statusLine = new JTextPane(document);
		statusLine.setOpaque(false);  
		/*
		JLabel labelStatusTitle = new JLabel();
		labelStatusTitle.setBackground(Color.white);
		labelStatusTitle.setText("RAFM Status: ");*/
		dimStatusLine = new Dimension(540, 65);
		setupComponent(statusLine, dimStatusLine);
		statusLine.setEditable(false);
		topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		//topPanel.add(labelStatusTitle);

		scrollPane = new JScrollPane(statusLine);
		setupComponent(scrollPane, dimStatusLine);
		actionListener.setStatusLine(statusLine);
		topPanel.add(scrollPane);
	}

	/**
	 * A blank arrow label to fill the arrow box.
	 * 
	 * @return A JLabel holding the blank box.
	 */
	private JLabel createBlankArrow(){
		JLabel arrow = new JLabel();
		arrow.setBackground(Color.WHITE);
		arrow.setIcon(new ImageIcon(getClass().getResource(ClientApplet.IMAGE_DIR + "blank25x25.jpg")));
		return arrow;
	}

	/**
	 * Fixing a components dimensions and colors.
	 * 
	 * @param comp The component to be setup.
	 * @param dim The dimensions to set for this component.
	 * @param col the background color of this component.
	 */
	public static void setupComponent(Component comp, Dimension dim, Color col){
		setupComponent(comp, dim);
		if(ENABLE_DEBUG) comp.setBackground(col);
		else comp.setBackground(Color.white);
	}
	
	/**
	 * Fixing a components dimensions.
	 * 
	 * @param comp The component to be setup.
	 * @param dim The dimensions to set for this component.
	 */
	public static void setupComponent(Component comp, Dimension dim){
		comp.setMinimumSize(dim);
		comp.setMaximumSize(dim);
		comp.setPreferredSize(dim);
		comp.setSize(dim);
	}
	
	private void shutDown(){
		arrBlinkingLabel.clear();
		arrBlinkingLabel = null;
		arrStateImg.clear();
		arrStateImg = null;
		/*tmpRemove.clear();
		tmpRemove = null;
		tmpAdd.clear();
//		tmpAdd = null;*/
	}
	
	private class ProgressUpdater extends ThreadHandler{
		private boolean iWait = false;
		private void doWait(boolean doWait){iWait = doWait;}
		@Override
		public void doTask() {
			if(iWait){
				synchronized(actionListener){
					try {actionListener.wait();
					} catch (InterruptedException e) {Debg.err("Impossible to wait");}
				}
			}
			if(actionListener != null) updateProgress(actionListener.getCurrentLine());
		}
		@Override
		public void shutDown() {
			stopThread();
		}
	}
	
	private class Sample {
		private int id;
		private String name;
		private String command;
		
		private Sample(int id, String name, String command){
			this.id = id;
			this.name = name;
			this.command = command;
		}
		
		private int getID(){
			return id;
		}
		
		private String getName(){
			return name;
		}
		
		private String getCommand(){
			return command;
		}
	}
	/*
	private class SimpleLine extends JPanel{
		private static final long serialVersionUID = 1L;
		
		private SimpleLine(int width, int height){
			this.setSize(new Dimension(width, height));
			setBackground(Color.orange);
		}
		
		protected void paintComponent(Graphics g) {
			g.setColor(Color.orange);
			g.drawLine(0,0,0,125);
		}

	}*/

	protected void checkConnection(){
		if(isServerConnected) setServerConnected(true);
		if(isRemExpConnected) setRemExpConnected(true);
	}
	
	protected void setServerConnected(boolean isConnected){
		isServerConnected = isConnected;
		setLabelConnected(labelServerDot, isConnected);
		if(!isConnected) setRemExpConnected(false);
	}
	
	private void setRemExpConnected(boolean isConnected){
		isRemExpConnected = isConnected;
		setLabelConnected(labelRemExpDot, isConnected);
		if(!isRemExpConnected){
			labelMovingDot.setIcon(imgDotRed);
			labelApproachedDot.setIcon(imgDotRed);
		}
	}
	
	private void setLabelConnected(JLabel label, boolean isConnected){
		if(isConnected){
			removeLabel(label);
		} else {
			addLabel(label);
			if(blinkerThread == null) blinkerThread = new Blinker();
		}
	}

	private void addLabel(JLabel lbl){
		if(!arrBlinkingLabel.contains(lbl))	arrBlinkingLabel.add(lbl);
	}
	
	private void removeLabel(JLabel lbl){
		arrBlinkingLabel.remove(lbl);
		lbl.setIcon(imgPlugGreen);
	}
	
	private class Blinker extends ThreadHandler{
		private Blinker(){
		    super.start(this.getClass().getSimpleName());
		}
		
		@Override
		public void doTask() {
			if(arrBlinkingLabel.size() == 0) this.shutDown();
			else {
				for(ImageIcon stateIco: arrStateImg){
					synchronized(arrBlinkingLabel){
						for(JLabel label: arrBlinkingLabel) label.setIcon(stateIco);
					}
					try{
						Thread.sleep(400);
					} catch (InterruptedException e) {}
				}
			}
			checkConnection();
		}
		
		@Override
		public void shutDown() {
			stopThread();
			blinkerThread = null;
		}
	
	}
}
