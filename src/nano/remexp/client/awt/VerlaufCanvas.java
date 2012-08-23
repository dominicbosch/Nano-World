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

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.MemoryImageSource;

/**
 * Copyright: Copyright (c) 2001
 * 
 * @author
 * @version 1.0
 */

public class VerlaufCanvas extends Canvas {
	private static final long serialVersionUID = 1L;
	private static final int WIDTH = 256;
	private static final int HEIGHT = 20;

	private ColorScala colorScala;
	private Image image;
	private int[] data;

	public Dimension getPreferredSize() {
		return new Dimension(WIDTH, HEIGHT);
	}

	public Dimension getMinimumSize() {
		return new Dimension(WIDTH, HEIGHT);
	}
	public VerlaufCanvas(ColorScala oldScala) {
		data = new int[WIDTH * HEIGHT];
		colorScala = oldScala;
		MemoryImageSource imagesource = new MemoryImageSource(WIDTH, HEIGHT, data, 0, WIDTH);
		image = createImage(imagesource);
	}

	public void setColorScala(ColorScala newScala) {
		colorScala = newScala;
	}

	public void paint(Graphics g) {
		for (int i = 0; i < WIDTH; i++) {
			for (int k = 0; k < HEIGHT; k++) {
				data[i + k * WIDTH] = colorScala.getColor(i);
			}
		}
		image.flush();
		g.drawImage(image, 0, 0, null);
	}

}
