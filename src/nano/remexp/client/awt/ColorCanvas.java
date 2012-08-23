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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.MemoryImageSource;


/**
 * Copyright: Copyright (c) 2001 
 * 
 * @author
 * @version 1.0
 */

public class ColorCanvas extends Canvas {
	private static final long serialVersionUID = 1L;
	private static final int WIDTH = 100;
	private static final int HEIGHT = 100;
	private Image colorImage;
	private int[] data = null;
	private int MyColor;
	private ColorListener myListener = null;

	public ColorCanvas() {
		data = new int[2 * WIDTH * HEIGHT];
		MemoryImageSource colorMemoryMap = new MemoryImageSource(2 * WIDTH, HEIGHT, data, 0,
				2 * WIDTH);
		colorImage = createImage(colorMemoryMap);
		this.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent e) {this_mouseDragged(e);}
		});
		this.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {this_mouseClicked(e);}
			public void mouseReleased(MouseEvent e) {this_mouseReleased(e);}
		});
	}
	
	public Dimension getPreferredSize() {
		return new Dimension(2 * WIDTH, HEIGHT);
	}

	public Dimension getMinimumSize() {
		return new Dimension(2 * WIDTH, HEIGHT);
	}

	protected void addColorListener(ColorListener newListener) {
		myListener = newListener;
	}

	public void paint(Graphics g) {
		for (int i = 0; i < HEIGHT; i++) {
			for (int k = 0; k < WIDTH; k++) {
				data[2 * WIDTH * i + k] = Color.HSBtoRGB(
						(float) (i / (double) HEIGHT),
						(float) (k / (double) WIDTH), (float) (1.0));
			}
			for (int k = 0; k < WIDTH; k++) {
				data[2 * WIDTH * i + k + WIDTH] = Color.HSBtoRGB(
						(float) (i / (double) HEIGHT), (float) (1.0),
						(float) ((WIDTH - k) / (double) WIDTH));
			}
		}
		colorImage.flush();
		g.drawImage(colorImage, 0, 0, null);
	}

	private void this_mouseClicked(MouseEvent e) {
		if (e.getX() < WIDTH) {
			MyColor = Color.HSBtoRGB((float) (e.getY() / (double) HEIGHT),
					(float) (e.getX() / (double) WIDTH), (float) (1.0));
		} else {
			MyColor = Color.HSBtoRGB((float) (e.getY() / (double) HEIGHT),
					(float) (1.0),
					(float) ((2 * WIDTH - e.getX()) / (double) WIDTH));
		}
		myListener.colorEventPerformed(MyColor);
	}

	private void this_mouseReleased(MouseEvent e) {this_mouseClicked(e);}

	private void this_mouseDragged(MouseEvent e) {this_mouseClicked(e);}

}
