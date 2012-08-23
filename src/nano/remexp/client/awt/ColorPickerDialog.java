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
 * Copyright: Copyright (c) 2012
 * 
 * @author
 * @version 1.1
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
