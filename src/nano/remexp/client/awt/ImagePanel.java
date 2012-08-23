package nano.remexp.client.awt;

import java.awt.*;
import java.awt.event.*;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import nano.remexp.client.ClientGUI;

/**
 * A bar showing the actual color spectrum plus two buttons.
 * The first button is to open the analysis window, the second
 * button is to change the color spectrum.
 * 
 * Copyright: Copyright (c) 2012
 * 
 * @author
 * @version 1.1
 */

public class ImagePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	JButton buttonChangeColor = new JButton("Change Colors");
	JButton buttonAnalysis = new JButton("Analysis");
	NetObserver netObserver;
	VerlaufCanvas verlauf;

	/**
	 * The constructor initializes this JPanel.
	 * 
	 * @param observer 
	 */
	public ImagePanel(NetObserver observer) {
		setBackground(Color.white);
		ClientGUI.setupComponent(this, new Dimension(500, 25));
		ClientGUI.setupComponent(buttonAnalysis, new Dimension(80, 25));
		ClientGUI.setupComponent(buttonChangeColor, new Dimension(120, 25));
		netObserver = observer;
		setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		add(buttonAnalysis);
		verlauf = new VerlaufCanvas(netObserver.getColorScala());
		add(verlauf);
		add(buttonChangeColor);
		buttonChangeColor.addActionListener(new ColorListener());
		buttonAnalysis.addActionListener(new SaveDataListener());
	}

	private class ColorListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			ColorScala newCol = FarbverlaufDialog.getColorScala(new JFrame(),
					netObserver.getColorScala());
			netObserver.setColorScala(newCol);
			verlauf.setColorScala(newCol);
			verlauf.repaint();
		}
	}
	
	private class SaveDataListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			netObserver.saveData("New Image");
		}
	}
}
