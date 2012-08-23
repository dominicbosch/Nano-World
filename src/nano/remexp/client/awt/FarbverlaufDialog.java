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
import java.awt.Color;
import java.awt.Dialog;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Copyright: Copyright (c) 2001
 * 
 * @author
 * @version 1.0
 */

public class FarbverlaufDialog extends Dialog {
	private static final long serialVersionUID = 1L;
	private VerlaufCanvas canvas;
	private ColorScala scala, oldScala;
	private JFrame myframe;
	private JPanel panel1 = new JPanel();
	private JPanel panel2 = new JPanel();
	private JPanel panel3 = new JPanel();
	private JButton button1 = new JButton();
	private JButton button2 = new JButton();
	private JButton button3 = new JButton();
	private JButton button4 = new JButton();

	public FarbverlaufDialog(JFrame frame, String title, boolean modal,
			ColorScala oldColorScala) {
		super(frame, title, modal);
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		scala = new ColorScala();
		scala.setColors(new Color(oldColorScala.getColor(0)),
				new Color(oldColorScala.getColor(255)));
		oldScala = new ColorScala();
		oldScala.setColors(new Color(oldColorScala.getColor(0)),
				new Color(oldColorScala.getColor(255)));
		myframe = frame;
		try {
			jbInit();
			add(panel1);
			pack();
			setVisible(true);
		} catch (Exception ex) {}
	}

	public FarbverlaufDialog(JFrame frame, ColorScala OldScala) {
		this(frame, "", true, OldScala);
	}

	public static ColorScala getColorScala(JFrame frame, ColorScala oldScala) {
		FarbverlaufDialog TheDialog = new FarbverlaufDialog(frame, oldScala);
		return TheDialog.scala;
	}

	void jbInit() throws Exception {
		GridLayout gridLayout1 = new GridLayout();
		panel1.setLayout(gridLayout1);
		canvas = new VerlaufCanvas(scala);
		button1.setText("change");
		button1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				button1_actionPerformed(e);
			}
		});
		button2.setText("change");
		button2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				button2_actionPerformed(e);
			}
		});
		gridLayout1.setRows(2);
		gridLayout1.setColumns(1);
		button3.setText("Abort");
		button3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				button3_actionPerformed(e);
			}
		});
		button4.setText("OK");
		button4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				button4_actionPerformed(e);
			}
		});
		panel3.add(button1, null);

		panel3.add(canvas, null);
		panel3.add(button2, null);
		panel1.add(panel3, null);
		panel1.add(panel2, null);
		panel2.add(button4, null);
		panel2.add(button3, null);

	}

	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			cancel();
		}
		super.processWindowEvent(e);
	}

	void cancel() {
		scala.setColors(new Color(oldScala.getColor(0)),
				new Color(oldScala.getColor(255)));
		dispose();
	}

	void button1_actionPerformed(ActionEvent e) {
		scala.setColors(new Color(ColorPickerDialog.getColor(myframe,
						scala.getColor(0))), new Color(scala.getColor(255)));
		canvas.setColorScala(scala);
		canvas.repaint();
	}

	void button2_actionPerformed(ActionEvent e) {
		scala.setColors(new Color(scala.getColor(0)), new Color(
				ColorPickerDialog.getColor(myframe, scala.getColor(255))));
		canvas.setColorScala(scala);
		canvas.repaint();

	}

	void button3_actionPerformed(ActionEvent e) {cancel();}

	void button4_actionPerformed(ActionEvent e) {dispose();}
}
