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

import java.awt.Color;

/**
 * 
 * @author Tibor Gyalog
 * @version 1.0 <17.7.01
 */

public class ColorScala {
	private int myColors[] = new int[256];

	public ColorScala() {
		for (int i = 0; i < 256; i++) myColors[i] = (255 << 24 | i << 16 | i << 8 | i);
	}

	public void setColors(Color bottom, Color top) {
		int r, g, b;
		float[] bcol = bottom.getRGBColorComponents(null);
		float[] tcol = top.getRGBColorComponents(null);
		float dr = (tcol[0] - bcol[0]) / 255;
		float dg = (tcol[1] - bcol[1]) / 255;
		float db = (tcol[2] - bcol[2]) / 255;
		for (int i = 0; i < 256; i++) {
			r = Math.round(255 * (bcol[0] + i * dr));
			g = Math.round(255 * (bcol[1] + i * dg));
			b = Math.round(255 * (bcol[2] + i * db));
			myColors[i] = (255 << 24 | r << 16 | g << 8 | b);
		}
	}

	public int getColor(int value) {
		if (value < 0) value = 0;
		if (value > 255) value = 255;
		return myColors[value];
	}
}
