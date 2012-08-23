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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import nano.debugger.Debg;

/**
 * Copyright: Copyright (c) 2001
 * 
 * @author
 * @version 1.0
 */

public class ColorPickerDialog extends Dialog implements ColorListener {
	private static final long serialVersionUID = 1L;
	private JPanel panel1 = new JPanel();
	private BorderLayout borderLayout1 = new BorderLayout();
	private JPanel panel2 = new JPanel();
	private JPanel panel3 = new JPanel();
	private JButton buttonOK = new JButton();
	private JButton buttonAbort = new JButton();
	private ColorCanvas myColorCanvas = new ColorCanvas();
	private JTextField textField1 = new JTextField();
	private int myColor, oldColor;

	protected static int getColor(JFrame frame, int oldColor) {
		ColorPickerDialog myColorPickerDialog = new ColorPickerDialog(frame,
				oldColor);
		return myColorPickerDialog.myColor;
	}

	public ColorPickerDialog(JFrame frame, String title, boolean modal,
			int MyOldColor) {
		super(frame, title, modal);
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		try {
			oldColor = MyOldColor;
			jbInit();
			add(panel1);
			pack();
		} catch (Exception ex) {Debg.err("Color picking failed...");}
	}

	public ColorPickerDialog(JFrame frame, int MyOldColor) {
		this(frame, "", true, MyOldColor);
		setVisible(true);
	}

	void jbInit() throws Exception {
		myColorCanvas.addColorListener(this);
		myColor = oldColor;
		textField1.setBackground(new Color(myColor));
		panel1.setLayout(borderLayout1);
		buttonOK.setText("OK");
		buttonOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				buttonOKactionPerformed(e);
			}
		});
		buttonAbort.setText("Abort");
		buttonAbort.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				buttonAbort_actionPerformed(e);
			}
		});
		textField1.setColumns(1);
		panel1.add(panel2, BorderLayout.SOUTH);
		panel2.add(buttonOK, null);
		panel2.add(buttonAbort, null);
		panel2.add(textField1, null);
		panel1.add(panel3, BorderLayout.CENTER);
		panel3.add(myColorCanvas);
	}

	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {dispose();}
		super.processWindowEvent(e);
	}

	public void colorEventPerformed(int NewColor) {
		textField1.setBackground(new Color(NewColor));
		myColor = NewColor;
	}

	private void buttonAbort_actionPerformed(ActionEvent e) {
		dispose();
		myColor = oldColor;
	}

	private void buttonOKactionPerformed(ActionEvent e) {
		dispose();
	}
}
