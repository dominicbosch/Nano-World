package nano.remexp.client.awt;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import nano.remexp.StreamReceiver;
import nano.remexp.client.ClientGUI;

/**
 * 
 * @author Tibor Gyalog
 * @version 1.0 from 20. 8. 01
 */
public class LineSection extends Canvas implements StreamReceiver {
	private static final long serialVersionUID = 1L;
	Graphics myg;
	int x1, y1, x2, y2, dx = 4, height = 256, res = 4, width = 256,
			numpoints = 256, plotlines = 2;

	public LineSection() {
		ClientGUI.setupComponent(this, new Dimension(256, 256));
		myg = getGraphics();
		setBackground(Color.black);
		setForeground(Color.green);
		setFont(new Font("Arial", Font.BOLD, 20));
	}

	public void write(byte[] newLine) {
		NewLine(newLine);
	}

	public Dimension getPreferredSize() {
		return new Dimension(256, 256);
	}

	public Dimension getMinimumSize() {
		return new Dimension(256, 256);
	}

	public void NewLine(byte[] data) {
		height = getHeight();
		width = getWidth();
		numpoints = 256 / res;
		dx = width / numpoints;
		if (myg == null) {
			myg = getGraphics();
			setBackground(Color.black);
			setForeground(Color.green);
			setFont(new Font("Arial", Font.BOLD, 20));
		}
		if (myg != null) {
			myg.clearRect(0, 0, dx + 1, height);
			for (int xi = 2; xi < numpoints; xi++) {
				myg.clearRect(dx * (xi - 1) + 1, 0, dx, height);
				y1 = height / 2 - data[res * xi];
				y2 = height / 2 - data[res * (xi - 1)];
				x1 = dx * xi;
				x2 = dx * (xi - 1);
				myg.drawLine(x2, y2, x1, y1);
			}
		}
	}

}
