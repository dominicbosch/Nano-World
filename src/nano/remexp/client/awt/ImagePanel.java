/* 
 * Copyright (c) 2002 by Tibor Gyalog, Raoul Schneider, Dino Keller, 
 * Christian Wattinger, Martin Guggisberg and The Regents of the University of 
 * Basel. All rights reserved.
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
 * Christian Wattinger, Martin Guggisberg <vexp@nano-world.net>
 * 
 * 
 */

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
 * Copyright: Copyright (c) 2001
 * 
 * @author
 * @version 1.0
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
