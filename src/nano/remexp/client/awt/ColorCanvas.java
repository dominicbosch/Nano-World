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
 * Copyright: Copyright (c) 2012 
 * 
 * @version 1.1
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
