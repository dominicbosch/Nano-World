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
 *
 *
 * Authors: Tibor Gyalog, Raoul Schneider, Dino Keller, 
 * Christian Wattinger, Martin Guggisberg <vexp@nano-world.net>
 * 
 * 
 */

package nano.remexp.client.awt;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import nano.remexp.StreamReceiver;

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
