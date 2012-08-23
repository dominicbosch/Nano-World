package nano.remexp.client.layouts;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import nano.remexp.client.ClientGUI;
import nano.remexp.client.modules.GMBasicButtons;
import nano.remexp.client.modules.GMBoxSamples;
import nano.remexp.client.modules.GMBoxScanRanges;
import nano.remexp.client.modules.GMButtonCamAngle;
import nano.remexp.client.modules.GMButtonLogin;
import nano.remexp.client.modules.GMButtonSave;
import nano.remexp.client.modules.GMInfoLine;
import nano.remexp.client.modules.GMMovementArrows;
import nano.remexp.client.modules.GMProgressBar;
import nano.remexp.client.modules.GMStatusBar;

/**
 * The advanced GUI layout that has no input line like the administrator,
 * but has the movement arrows that allow some movements to certain deltas on the sample. 
 * 
 * @author Dominic Bosch
 * @version 1.1 29.08.2012 
 */

public class GLAdvanced extends GUILayout{
	public GLAdvanced(ClientGUI gui) {
		super(gui);
		panelControls = new JPanel();
		JPanel panelControlsTop = new JPanel();
		JPanel panelControlsRight = new JPanel();
		JPanel panelControlsLeft = new JPanel();
		ClientGUI.setupComponent(panelControls, new Dimension(530, 250), Color.yellow);
		panelControls.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		panelControls.setLayout(new FlowLayout(FlowLayout.CENTER, 1, 1));
		ClientGUI.setupComponent(panelControlsTop, new Dimension(520, 22), Color.red);
		ClientGUI.setupComponent(panelControlsLeft, new Dimension(259, 115), Color.green);
		ClientGUI.setupComponent(panelControlsRight, new Dimension(259, 115), Color.red);
		panelControlsTop.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		panelControlsLeft.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		panelControlsRight.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));

		addModule(new GMProgressBar(), panelControls);
		addModule(new GMStatusBar(), panelControls);

		addModule(new GMBoxScanRanges(), panelControlsTop);
		JLabel spacer = new JLabel();
		ClientGUI.setupComponent(spacer, new Dimension(55, 23));
		panelControlsTop.add(spacer);
		addModule(new GMBoxSamples(), panelControlsTop);
		panelControls.add(panelControlsTop);

		addModule(new GMBasicButtons(), panelControlsLeft);
		addModule(new GMButtonCamAngle(), panelControlsLeft);
		addModule(new GMButtonSave(), panelControlsLeft);
		addModule(new GMButtonLogin(), panelControlsLeft);
		spacer = new JLabel();
		ClientGUI.setupButton(spacer);
		panelControlsLeft.add(spacer);
		panelControls.add(panelControlsLeft);

		addModule(new GMMovementArrows(), panelControlsRight);
		panelControls.add(panelControlsRight);

		addModule(new GMInfoLine(), panelControls);
	}
}
