package nano.remexp.client.layouts;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import nano.remexp.client.ClientGUI;
import nano.remexp.client.modules.GMBasicButtons;
import nano.remexp.client.modules.GMBoxSamples;
import nano.remexp.client.modules.GMBoxScanRanges;
import nano.remexp.client.modules.GMButtonCamAngle;
import nano.remexp.client.modules.GMButtonLogin;
import nano.remexp.client.modules.GMButtonSave;
import nano.remexp.client.modules.GMInfoLine;
import nano.remexp.client.modules.GMProgressBar;
import nano.remexp.client.modules.GMStatusBar;

/**
 * The GUI layout that has the basic controls.
 * 
 * @author Dominic Bosch
 * @version 1.1 29.08.2012 
 */

public class GLController extends GUILayout{
	public GLController(ClientGUI gui) {
		super(gui);
		panelControls = new JPanel();
		JPanel panelControlsTop = new JPanel();
		JPanel panelControlsLeft = new JPanel();
		ClientGUI.setupComponent(panelControls, new Dimension(530, 205), Color.yellow);
		panelControls.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		panelControls.setLayout(new FlowLayout(FlowLayout.CENTER, 1, 1));
		ClientGUI.setupComponent(panelControlsTop, new Dimension(520, 22), Color.red);
		ClientGUI.setupComponent(panelControlsLeft, new Dimension(95, 70), Color.green);
		panelControlsTop.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		panelControlsLeft.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));

		addModule(new GMProgressBar(), panelControls);
		addModule(new GMStatusBar(), panelControls);

		addModule(new GMBoxScanRanges(), panelControlsTop);
		addModule(new GMBoxSamples(), panelControlsTop);
		panelControls.add(panelControlsTop);

		addModule(new GMButtonCamAngle(), panelControlsLeft);
		addModule(new GMButtonSave(), panelControlsLeft);
		addModule(new GMButtonLogin(), panelControlsLeft);
		panelControls.add(panelControlsLeft);

		addModule(new GMBasicButtons(), panelControls);
		addModule(new GMInfoLine(), panelControls);
	}
}
