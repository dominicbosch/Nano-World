package nano.remexp.client.layouts;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import nano.remexp.client.ClientGUI;
import nano.remexp.client.modules.GMButtonLogin;
import nano.remexp.client.modules.GMButtonSave;
import nano.remexp.client.modules.GMInfoLine;
import nano.remexp.client.modules.GMLabelSamples;
import nano.remexp.client.modules.GMLabelScanRanges;
import nano.remexp.client.modules.GMProgressBar;
import nano.remexp.client.modules.GMStatusBar;

/**
 * The observer GUI layout that only has passive modules to display state information.
 * 
 * @author Dominic Bosch
 * @version 1.1 29.08.2012 
 */

public class GLObserver extends GUILayout{
	public GLObserver(ClientGUI gui) {
		super(gui);
		panelControls = new JPanel();
		ClientGUI.setupComponent(panelControls, new Dimension(530, 140), Color.yellow);
		panelControls.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		panelControls.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));

		addModule(new GMProgressBar(), panelControls);
		addModule(new GMStatusBar(), panelControls);

		addModule(new GMButtonLogin(), panelControls);
		addModule(new GMButtonSave(), panelControls);;
		addModule(new GMLabelScanRanges(), panelControls);;
		addModule(new GMLabelSamples(), panelControls);

		addModule(new GMInfoLine(), panelControls);
	}
}
