package nano.remexp.client.awt;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.MemoryImageSource;

/**
 * Copyright: Copyright (c) 2012
 * 
 * @author
 * @version 1.1
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
