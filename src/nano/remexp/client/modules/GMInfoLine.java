package nano.remexp.client.modules;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import nano.remexp.Parser;
import nano.remexp.client.ClientGUI;
import nano.remexp.net.NanoComm;

/**
 * A GUI module that holds the info line which displays informations to the clients.
 * 
 * @author Dominic Bosch
 * @version 1.1 29.08.2012 
 */
public class GMInfoLine implements GUIModule{
	public ClientGUI gui = null;
	private Style style;
	private JTextPane statusLine;
	private JScrollPane scrollPane;
	private String messageHistory;
	private Dimension size;

	@Override public JPanel init(ClientGUI chief){
		gui = chief;
		JPanel panel = new JPanel();
		size =  new Dimension(520, 65);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		ClientGUI.setupComponent(panel, size, Color.magenta);
		StyleContext context = new StyleContext();
		StyledDocument document = new DefaultStyledDocument(context);
		
		style = context.getStyle(StyleContext.DEFAULT_STYLE);
		StyleConstants.setAlignment(style, StyleConstants.ALIGN_LEFT);
		StyleConstants.setFontSize(style, 10);
		StyleConstants.setSpaceAbove(style, 1);
		StyleConstants.setSpaceBelow(style, 1);

		statusLine = new JTextPane(document);
		statusLine.setOpaque(false); 
		ClientGUI.setupComponent(statusLine, size);
		statusLine.setEditable(false);

		scrollPane = new JScrollPane(statusLine);
		ClientGUI.setupComponent(scrollPane, size);
		panel.add(scrollPane);
		return panel;
	}
	@Override public Dimension getSize() {return size;}
	@Override public void setState(String state){}
	@Override public void setInfo(String info){
		try{
			int val = Integer.parseInt(Parser.getValue(info, NanoComm.COMMAND_INFO));
			switch(val){
			/*case NanoComm.INFO_REMEXP_CONNECTED:
				printInfo("Remote Experiment is connected");
				break;
			case NanoComm.INFO_REMEXP_DISCONNECTED:
				printInfo("Remote Experiment is not connected");
				break;
			case NanoComm.INFO_SERVER_CONNECTED:
				printInfo("Connected to server");
				break;
			case NanoComm.INFO_SERVER_DISCONNECTED:
				printInfo("Not connected to server");
				break;*/
			case NanoComm.INFO_MSG_TO_CLIENT:
				int idx = info.indexOf(" ");
				if(idx > -1 && idx < info.length() - 1) printInfo(info.substring(idx + 1));
			}
		} catch (NumberFormatException e){}
	}
	@Override public void setParameter(String param){}

	/**
	 * Prints informations gathered during the initialization.
	 */
	protected void printInitInfo(){
		printInfo(messageHistory);
		messageHistory = "";
	}
	
	/**
	 * Add information to the status box.
	 * 
	 * @param msg The message to add.
	 */
	protected void printInfo(String msg){
		if(statusLine == null) messageHistory = msg + "\n" + messageHistory;
		else {
			try {
				statusLine.getDocument().insertString(0, msg + "\n", style);
				statusLine.setCaretPosition(0);
			} catch (BadLocationException e) {}
		}
	}
	@Override public void terminate() {
		gui = null;
		size = null;
		style = null;
		statusLine = null;
		scrollPane = null;
		messageHistory = null;
	}
}

