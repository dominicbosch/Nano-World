package nano.remexp.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import nano.debugger.Debg;
import nano.remexp.Password;
import nano.remexp.client.awt.ImagePanel;
import nano.remexp.client.awt.LineSection;
import nano.remexp.client.awt.NetObserver;
import nano.remexp.client.layouts.GUILayout;
import nano.remexp.client.layouts.GLAdmin;
import nano.remexp.client.layouts.GLAdvanced;
import nano.remexp.client.layouts.GLController;
import nano.remexp.client.layouts.GLObserver;
import nano.remexp.net.NanoComm;


/**
 * This class generates the graphical user interface and handles events coming from
 * user inputs. Those are passed to the main applet class for further processing.
 * It also passes informations coming from the remote experiment broadcaster to the
 * GUI elements which can then react on them if necessary.
 * 
 * @author Dominic Bosch
 * @version 1.1 23.08.2012
 */
public class ClientGUI {
	private static final String IMAGE_DIR = "images/";
//FIXME check new client with emulated event and stream server down to scanning
	private static final boolean ENABLE_DEBUG = false;
	private Dimension dimGUI;
	public static final Dimension DIM_BUTTON = new Dimension(90, 23); // button dimension
	public static final Font FONT_BUTTON = new Font("Button", Font.PLAIN, 11); // button font
	
	private ClientApplet applet;
	private GUILayout clientControls;
	private ImagePanel panelImage;
	private JPanel panelWindow;
	private JPanel panelGraphs;
	private JPanel panelControlsPlug;
	private ParameterPopup popupWindow;
	private boolean isInitialized;

	/**
	 * Loads and returns an image from the image package.
	 * 
	 * @param name the file name of the image
	 * @return The ImageIcon file that can be built into the GUI.
	 */
	public static ImageIcon getImage(String name){
		return new ImageIcon(ClientGUI.class.getResource(IMAGE_DIR + name));
	}
	
	/**
	 * ClientGUI holds all the necessary bits and pieces to build up a fully functional GUI
	 * that allows to control a remote experiment.
	 * 
	 * @param boss The main applet object that needs to be informed about user inputs.
	 */
	protected ClientGUI(ClientApplet boss){
		isInitialized = false;
		popupWindow = new ParameterPopup();
		applet = boss;
		Debg.print("init GUI finished");
	}
	
	/**
	 * This method creates a JPanel which holds all parts of the GUI, i.e.: the logo, the scan image analysis tools,
	 * the graphs consisting of a line graph and a top view of the surface of the scanned material and last
	 * but not least the controls that allow the interaction between the user and the remote experiment.
	 * The client controls are modular and according to gained privileges there are more or less options
	 * visible for the client.
	 * 
	 * @param myImage The surface representation of the scanned material.
	 * @param myLiner The line graph representation of the scanned material.
	 * @return A JPanel holding the whole GUI.
	 */
	protected JPanel createWindow(NetObserver myImage, LineSection myLiner){
		panelWindow = new JPanel();
		panelImage = new ImagePanel(myImage);
		panelGraphs = setupGraphs(myImage, myLiner);
		clientControls = new GLObserver(this);
		dimGUI = new Dimension(530, 670);
		applet.resize(dimGUI);
		ClientGUI.setupComponent(panelWindow, dimGUI);
		panelWindow.setBackground(Color.white);
		panelWindow.setLayout(new FlowLayout(FlowLayout.CENTER, 1, 1));
		JLabel labelNano = new JLabel();
		labelNano.setIcon(getImage("nano_logo.jpg"));
		ClientGUI.setupComponent(labelNano, new Dimension(500, 96));
		
		panelControlsPlug = new JPanel();
		panelControlsPlug.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		int possibleHeight = (int)(dimGUI.getHeight()-labelNano.getHeight()-panelImage.getHeight()-panelGraphs.getHeight()-5);
		ClientGUI.setupComponent(panelControlsPlug, new Dimension(530, possibleHeight), Color.red);
		panelControlsPlug.add(clientControls.getPanel());
		panelWindow.add(labelNano);
		panelWindow.add(panelImage);
		panelWindow.add(panelGraphs);
		panelWindow.add(panelControlsPlug);
		return panelWindow;
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
		setupComponent(panelGraphs, new Dimension(530, 266), Color.green);

		panelGraphs.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		panelGraphs.setLayout(new FlowLayout(FlowLayout.CENTER, 3, 3));
		panelGraphs.add(myImage);
		panelGraphs.add(myLiner);
		return panelGraphs;
	}

	/**
	 * Returns the line that was last scanned by the remote experiment.
	 * 
	 * @return	the index of the last scanned line
	 */
	public int getCurrentLine(){
		return applet.getCurrentLine();
	}
	
	/**
	 * Sets the controls on the GUI. This is used to repaint the GUI if the
	 * privilege of the client changed and thus also the controls.
	 * 
	 * @param controls	the panel with the new controls
	 */
	protected void setControls(JPanel controls){
		panelControlsPlug.removeAll();
		panelControlsPlug.add(controls);
		panelControlsPlug.revalidate();
		panelControlsPlug.repaint();
		applet.checkConnectivity();
	}

	/**
	 * Returns information about whether the applet finished initialization.
	 * 
	 * @return	true if the applet is already initialized, false if not
	 */
	protected boolean isInitialized(){
		return isInitialized;
	}
	
	/**
	 * If the applet finished initialization it calls this method.
	 */
	protected void setInitialized(){
		isInitialized = true;
	}
	
	/**
	 * If the remote experiment broadcaster sends a privilege it might change
	 * the controls in the client GUI.
	 * 
	 * @param priv	the new privilege
	 */
	protected void setPrivilege(int priv){
		GUILayout newControls = null;
		if(isInitialized) switch(priv){
			case NanoComm.PRIV_ADMIN:
				Debg.print("received admin privilege"); 
				newControls = new GLAdmin(this);
				break;
			case NanoComm.PRIV_ADVANCED:
				Debg.print("received advanced privilege");
				newControls = new GLAdvanced(this);
				break;
			case NanoComm.PRIV_CONTROLLER:
				Debg.print("received controller privilege");
				newControls = new GLController(this);
				break;
			case NanoComm.PRIV_OBSERVER:
				Debg.print("received observer privilege");
				newControls = new GLObserver(this);
				break;
			default:
				Debg.err("Privilege not handled");
		}
		if(newControls!=null){
			clientControls.terminate(); 
			clientControls = newControls;
			setControls(clientControls.getPanel());
		}
	}
	
	/**
	 * Passes state information to the GUI controls.
	 * 
	 * @param state		the string holding state information
	 */
	protected void setState(String state){
		clientControls.setState(state);
	}
	
	/**
	 * Passes general information to the GUI controls.
	 * 
	 * @param info		the information string
	 */
	protected void setInfo(String info){
		clientControls.setInfo(info);
	}

	/**
	 * Passes parameter information to the GUI controls.
	 * 
	 * @param param		the string holding parameter information
	 */
	protected synchronized void setParameter(String param){
		clientControls.setParameter(param);
	}
	
	/**
	 * Redirect a string to the applet that sends it through the event socket.
	 * 
	 * @param msg	The message that holds command information
	 * 				and needs to be wrapped into the command protocol
	 */
	public void sendCommand(String msg){
		applet.sendCommand(msg);
	}

	/**
	 * Commands the applet to open the save image dialog 
	 */
	public void saveImage(){
		applet.saveImage();
	}
	
	/**
	 * Opens the login pop-up that allows the client to log into
	 * a special role with more control possibilities.
	 */
	public void openLoginDialog(){
		popupWindow.initLoginPopup();
	}

	/**
	 * Setting a components dimension to the standard button size.
	 * 
	 * @param comp The component to be setup.
	 */
	public static void setupButton(Component comp){
		comp.setFont(FONT_BUTTON);
		setupComponent(comp, DIM_BUTTON);
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
	
	/**
	 * A pop-up that has two labels, two input fields and one button.
	 * 
	 * @author Dominic Bosch
	 * @version 1.1 29.08.2012
	 */
	private class ParameterPopup extends JFrame{
		private static final long serialVersionUID = 1L;
		private JTextField fieldOne;
		private JTextField fieldTwo;
	    private JPanel contentPanel;
		
	    /**
	     * Instantiates the parameter pop-up
	     */
		private ParameterPopup(){
			setBackground(Color.white);
		    contentPanel = new JPanel();
			ClientGUI.setupComponent(contentPanel, new Dimension(305, 25));
            add(contentPanel);
		}

		/**
		 * Initializes the parameter pop-up as login pop-up.
		 */
		protected void initLoginPopup(){
			contentPanel.removeAll();
			JLabel labelText = new JLabel("Please enter your credentials: ");
			JLabel labelUser = new JLabel("Username: ");
			fieldOne = new JTextField();
			JLabel labelPass = new JLabel("Password: ");
			fieldTwo = new JPasswordField();
			JButton ok = new JButton("Ok");
			addEnterKeyToComponent(ok, ok);
			addEnterKeyToComponent(fieldOne, ok);
			addEnterKeyToComponent(fieldTwo, ok);
			ok.addActionListener(new ActionListener(){
				@Override public void actionPerformed(ActionEvent arg0) {login();}
			});
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(new ActionListener(){
				@Override public void actionPerformed(ActionEvent arg0) {hidePopup();}
			});
			contentPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
			ClientGUI.setupComponent(labelText, new Dimension(305, 25));
			ClientGUI.setupComponent(labelUser, new Dimension(100, 25));
			ClientGUI.setupComponent(fieldOne, new Dimension(200, 25));
			ClientGUI.setupComponent(labelPass, new Dimension(100, 25));
			ClientGUI.setupComponent(fieldTwo, new Dimension(200, 25));
			ClientGUI.setupComponent(ok, new Dimension(80, 25));
			ClientGUI.setupComponent(cancel, new Dimension(80, 25));
			ClientGUI.setupComponent(this, new Dimension(350, 175));
			contentPanel.setBackground(Color.white);
			contentPanel.add(labelText);
			contentPanel.add(labelUser);
			contentPanel.add(fieldOne);
			contentPanel.add(labelPass);
			contentPanel.add(fieldTwo);
			contentPanel.add(ok);
			contentPanel.add(cancel);
			fieldOne.requestFocusInWindow();
			setVisible(true);
		}
		
		/**
		 * This function assigns a button press action to a component if the focus is on that
		 * component and the enter button is pressed.
		 * 
		 * @param comp			the component that will listen for the enter key
		 * @param pressButton	the buttom that wll be pressed if the enter key
		 * 						is pressed during focus on the component
		 */
		private void  addEnterKeyToComponent(JComponent comp, JButton pressButton){
			String actionKey = pressButton.toString();
		    InputMap inputMap = comp.getInputMap();
			ActionMap actionMap = comp.getActionMap();

		    Action actionListener = new ButtonPresser(pressButton);
			actionMap.put(actionKey, actionListener);
		    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true), actionKey);
		    comp.setActionMap(actionMap);
		}
		
		/**
		 * This class holds a button that will be pressed if a 
		 * predefined component registered the enter key.
		 * 
		 * @author Dominic Bosch
		 * @version 1.1 29.08.2012
		 */
		class ButtonPresser extends AbstractAction{
			private static final long serialVersionUID = 1L;
			private JButton pressMe;
	    	public ButtonPresser(JButton b){pressMe = b;}
	    	public void actionPerformed(ActionEvent e) {pressMe.doClick();}
		}
		
		/**
		 * This function fetches the username and password from the form.
		 * It encrypts the password with the hash function of the @see Password class.
		 */
		private void login(){
			applet.sendCommand("login username=" + fieldOne.getText() 
					+ " password=" + Password.computeHashHex(fieldTwo.getText()));
			hidePopup();
		}

		/**
		 * Hides this pop-up again.
		 */
		private void hidePopup(){
			setVisible(false);
		}
	}

}
