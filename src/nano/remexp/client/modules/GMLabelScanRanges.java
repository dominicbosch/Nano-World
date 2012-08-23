
package nano.remexp.client.modules;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import nano.remexp.Parser;
import nano.remexp.client.ClientGUI;
import nano.remexp.net.NanoComm;

/**
 * A GUI module that holds a label that displays the scan range.
 * 
 * @author Dominic Bosch
 * @version 1.1 29.08.2012 
 */
public class GMLabelScanRanges implements GUIModule{
	private JLabel labelScanRange;
	private Dimension size;

	@Override public JPanel init(ClientGUI chief){
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		size =  new Dimension(120, 23);
		ClientGUI.setupComponent(panel, size, Color.BLUE);

		labelScanRange = new JLabel();
		ClientGUI.setupComponent(labelScanRange, size);
		labelScanRange.setText("Scan Range: 10 um");

		panel.add(labelScanRange);
		return panel;
	}
	@Override public Dimension getSize() {return size;}
	@Override public void setState(String state){}
	@Override public void setInfo(String info){}
	@Override public void setParameter(String param){
			int cmd = Integer.parseInt(Parser.getValue(param, NanoComm.COMMAND_PARAM));
			try{
				int val = Integer.parseInt(Parser.getValue(param, "value"));
				if(cmd == NanoComm.PARAM_SCANRANGE){
					labelScanRange.setText("Scan Range: " + val + " um");
				}
			} catch (NumberFormatException e){}
	}
	
	@Override public void terminate() {
		size = null;
	}
}
