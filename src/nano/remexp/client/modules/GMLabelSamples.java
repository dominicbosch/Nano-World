package nano.remexp.client.modules;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JPanel;

import nano.debugger.Debg;
import nano.remexp.Parser;
import nano.remexp.client.ClientGUI;
import nano.remexp.net.NanoComm;

/**
 * A GUI module that holds the sample combo box for the client to choose from.
 * 
 * @author Dominic Bosch
 * @version 1.1 29.08.2012 
 */
public class GMLabelSamples implements GUIModule{
	private Dimension size;
	private JLabel labelSample;
	private Vector<TinySample> samples;

	@Override public JPanel init(ClientGUI chief){
		JPanel panel = new JPanel();

		samples = new Vector<TinySample>();
		samples.add(new TinySample(-1, "Calibrate"));
		size =  new Dimension(120, 22);
		ClientGUI.setupComponent(panel, size, Color.gray);
		labelSample = new JLabel();
		ClientGUI.setupComponent(labelSample, size);
		labelSample.setText("Position: None");
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5,0));
		panel.add(labelSample);
		return panel;
	}
	@Override public Dimension getSize() {return size;}
	@Override public void setState(String state) {}
	@Override public void setInfo(String info){
		try{
			int val = Integer.parseInt(info.substring(NanoComm.COMMAND_INFO.length() + 1));
			switch(val){
			case NanoComm.INFO_SERVER_DISCONNECTED:
			case NanoComm.INFO_REMEXP_DISCONNECTED:
				labelSample.setText("Position: None");
			}
		} catch(NumberFormatException e){}
	}
	@Override public void setParameter(String param){
		try {
			int cmd = Integer.parseInt(Parser.getValue(param, NanoComm.COMMAND_PARAM));
			String val = Parser.getValue(param, "value");
			switch(cmd){
				case NanoComm.PARAM_STAGEPOSITION:
					int sid = Integer.parseInt(val);
					for(TinySample s: samples) if(s.id==sid) labelSample.setText("Position: " + s.name);
					break;
				case NanoComm.PARAM_SAMPLEINFO:
						String name = Parser.getValue(param, "name");
						samples.add(new TinySample(Integer.parseInt(val), name));
					break;
				case NanoComm.PARAM_SAMPLESCLEAR:
					samples.clear();
					samples.add(new TinySample(-1, "Calibrate"));
					break;
			}
		} catch(Exception e){}
	}
	@Override public void terminate() {
		size = null;
		
	}
	private class TinySample{
		int id;
		String name;
		private TinySample(int i, String n){
			id = i;
			name = n;
		}
	}
}
